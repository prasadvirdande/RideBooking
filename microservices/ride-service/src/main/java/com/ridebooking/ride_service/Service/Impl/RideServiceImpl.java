package com.ridebooking.ride_service.Service.Impl;

import com.ridebooking.ride_service.DTO.*;
import com.ridebooking.ride_service.Entity.Ride;
import com.ridebooking.ride_service.Enums.RideStatus;
import com.ridebooking.ride_service.Feign.DriverFeign;
import com.ridebooking.ride_service.Feign.UserFeign;
import com.ridebooking.ride_service.Repository.RideRepo;
import com.ridebooking.ride_service.Service.RideService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Random;
import java.util.UUID;

@Service
public class RideServiceImpl implements RideService {

    private final RideRepo rideRepository;
    private final UserFeign userFeign;
    private final DriverFeign driverFeign;
    private final RideProducer rideProducer;
    private final RedisTemplate<String, String> redisTemplate;

    public RideServiceImpl(RideRepo rideRepository, UserFeign userFeign, DriverFeign driverFeign, RedisTemplate<String, String> redisTemplate) {
        this.rideRepository = rideRepository;
        this.userFeign = userFeign;
        this.driverFeign = driverFeign;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public RideResponseDto createRide(
            RideRequest rideRequest) {

        UserDTO userDTO =
                userFeign.getUserById(
                        rideRequest.getUserId()
                );

        DriverDTO driverDTO =
                driverFeign.getDriverById(
                        rideRequest.getDriverId()
                );

        if (userDTO == null || driverDTO == null) {
            throw new RuntimeException(
                    "User or Driver not found"
            );
        }

        double distance =
                calculateDistance(
                        rideRequest.getPickupLatitude(),
                        rideRequest.getPickupLongitude(),
                        rideRequest.getDropLatitude(),
                        rideRequest.getDropLongitude()
                );

        double fare =
                calculateFare(distance);

        Ride ride = new Ride();

        ride.setUserId(
                UUID.fromString(
                        rideRequest.getUserId()
                )
        );

        ride.setDriverId(
                UUID.fromString(
                        rideRequest.getDriverId()
                )
        );

        ride.setPickupLatitude(
                rideRequest.getPickupLatitude()
        );

        ride.setPickupLongitude(
                rideRequest.getPickupLongitude()
        );

        ride.setDestinationLatitude(
                rideRequest.getDropLatitude()
        );

        ride.setDestinationLongitude(
                rideRequest.getDropLongitude()
        );

        ride.setDistance(
                BigDecimal.valueOf(distance)
        );

        ride.setFare(
                BigDecimal.valueOf(fare)
        );

        ride.setRideStatus(
                RideStatus.REQUESTED
        );

        Ride savedRide =
                rideRepository.save(ride);

        return RideResponseDto.builder()
                .rideId(savedRide.getId())
                .userId(savedRide.getUserId())
                .driverId(savedRide.getDriverId())
                .pickupLatitude(savedRide.getPickupLatitude())
                .pickupLongitude(savedRide.getPickupLongitude())
                .destinationLatitude(savedRide.getDestinationLatitude())
                .destinationLongitude(savedRide.getDestinationLongitude())
                .fare(savedRide.getFare().doubleValue())
                .distance(savedRide.getDistance().doubleValue())
                .status(savedRide.getRideStatus())
                .message("Ride requested successfully")
                .build();
    }

    @Override
    public AcceptedRideResponseDTO updateRide(UUID rideId, AcceptRideRequest rideRequest) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));

        System.out.println("Ride ID received: " + ride.getRideStatus());
        System.out.println("RIDE STATUS:"+ ride.getId());

        if (ride.getRideStatus() != RideStatus.REQUESTED) {
            throw new RuntimeException("Ride is not in REQUESTED status");
        }
        System.out.println("Ride Status = " + ride.getRideStatus());

        if (!ride.getDriverId().toString().equals(rideRequest.getDriverId())) {
            throw new RuntimeException("Driver is not the one who requested the ride");
        }
        ride.setRideStatus(RideStatus.ACCEPTED);
        Ride saveRide= rideRepository.save(ride);
        String otp =
                String.valueOf(
                        1000 + new Random().nextInt(9000)
                );


        redisTemplate.opsForValue().set(
                "ride:otp:" + saveRide.getId(),
                otp,
                Duration.ofMinutes(60)
        );
        driverFeign.acceptRide(rideRequest);

        return AcceptedRideResponseDTO.builder()
                .rideId(saveRide.getId().toString())
                .driverId(saveRide.getDriverId().toString())
                .otp(otp)
                .status(saveRide.getRideStatus().name())
                .build();
    }

    @Override
    public void verifyOtp(
            VerifyOtpDTO verifyOtpDTO) {

        System.out.println("otp:"+ verifyOtpDTO.getOtp());

        String storedOtp =
                redisTemplate.opsForValue().get(
                        "ride:otp:" +
                                verifyOtpDTO.getRideId()
                );

        if (storedOtp == null) {
            throw new RuntimeException(
                    "OTP expired or not found"
            );
        }

        if (!storedOtp.equals(
                verifyOtpDTO.getOtp()
        )) {
            throw new RuntimeException(
                    "Invalid OTP"
            );
        }

        Ride ride =
                rideRepository.findById(
                        UUID.fromString(
                                verifyOtpDTO.getRideId()
                        )
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Ride not found"
                        ));

        if (ride.getRideStatus()
                != RideStatus.ACCEPTED) {

            throw new RuntimeException(
                    "Ride is not in ACCEPTED status"
            );
        }

        redisTemplate.delete(
                "ride:otp:" +
                        verifyOtpDTO.getRideId()
        );

        System.out.println(
                "OTP verified for ride: "
                        + ride.getId()
        );
    }

    private double calculateDistance(
            Double lat1,
            Double lon1,
            Double lat2,
            Double lon2) {

        final int EARTH_RADIUS = 6371;

        double latDistance =
                Math.toRadians(lat2 - lat1);

        double lonDistance =
                Math.toRadians(lon2 - lon1);

        double a =
                Math.sin(latDistance / 2)
                        * Math.sin(latDistance / 2)
                        + Math.cos(Math.toRadians(lat1))
                        * Math.cos(Math.toRadians(lat2))
                        * Math.sin(lonDistance / 2)
                        * Math.sin(lonDistance / 2);

        double c =
                2 * Math.atan2(
                        Math.sqrt(a),
                        Math.sqrt(1 - a)
                );

        return EARTH_RADIUS * c;
    }

    private double calculateFare(double distanceKm) {

        double baseFare = 40;

        double perKmFare = 12;

        return baseFare +
                (distanceKm * perKmFare);
    }
}

