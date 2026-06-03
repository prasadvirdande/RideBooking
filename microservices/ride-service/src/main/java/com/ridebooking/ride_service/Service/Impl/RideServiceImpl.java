package com.ridebooking.ride_service.Service.Impl;

import com.ridebooking.ride_service.DTO.DriverDTO;
import com.ridebooking.ride_service.DTO.RideRequest;
import com.ridebooking.ride_service.DTO.RideResponseDto;
import com.ridebooking.ride_service.DTO.UserDTO;
import com.ridebooking.ride_service.Entity.Ride;
import com.ridebooking.ride_service.Enums.RideStatus;
import com.ridebooking.ride_service.Feign.DriverFeign;
import com.ridebooking.ride_service.Feign.UserFeign;
import com.ridebooking.ride_service.Repository.RideRepo;
import com.ridebooking.ride_service.Service.RideService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class RideServiceImpl implements RideService {

    private final RideRepo rideRepository;
    private final UserFeign userFeign;
    private final DriverFeign driverFeign;

    public RideServiceImpl(RideRepo rideRepository, UserFeign userFeign, DriverFeign driverFeign) {
        this.rideRepository = rideRepository;
        this.userFeign = userFeign;
        this.driverFeign = driverFeign;
    }

    @Override
    public RideResponseDto createRide(
            RideRequest rideRequest) {

        UserDTO userDTO = userFeign.getUserById(rideRequest.getUserId());
        DriverDTO driverDTO = driverFeign.getDriverById(rideRequest.getDriverId());

        if(userDTO == null || driverDTO == null) {
            throw new RuntimeException("User or Driver not found");
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
                .pickupLatitude(
                        Double.valueOf(savedRide.getPickupLatitude())
                )
                .pickupLongitude(
                        Double.valueOf(savedRide.getPickupLongitude())
                )
                .destinationLatitude(
                        Double.valueOf(savedRide.getDestinationLatitude())
                )
                .destinationLongitude(
                        Double.valueOf(savedRide.getDestinationLongitude())
                )
                .fare(Double.valueOf(String.valueOf(savedRide.getFare())))
                .status(savedRide.getRideStatus())
                .message(
                        "Ride requested successfully"
                )
                .build();
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
