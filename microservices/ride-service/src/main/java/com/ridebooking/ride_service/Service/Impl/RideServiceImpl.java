package com.ridebooking.ride_service.Service.Impl;

import com.ridebooking.ride_service.DTO.*;
import com.ridebooking.ride_service.DTO.Event.RideAcceptedRideEvent;
import com.ridebooking.ride_service.DTO.Event.RideStartedEvent;
import com.ridebooking.ride_service.Entity.Ride;
import com.ridebooking.ride_service.Enums.RideStatus;
import com.ridebooking.ride_service.Exception.RIdeNotAccepted;
import com.ridebooking.ride_service.Exception.UserOrRIdeNotFOund;
import com.ridebooking.ride_service.Feign.DriverFeign;
import com.ridebooking.ride_service.Feign.PaymentClient;
import com.ridebooking.ride_service.Feign.UserFeign;
import com.ridebooking.ride_service.Repository.RideRepo;
import com.ridebooking.ride_service.Service.Kafka.RideProducer;
import com.ridebooking.ride_service.Service.RideService;
import jakarta.transaction.Transactional;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
public class RideServiceImpl implements RideService {

    private final RideRepo rideRepository;
    private final UserFeign userFeign;
    private final DriverFeign driverFeign;
    private final RideProducer rideProducer;
    private final RedisTemplate<String, String> redisTemplate;
    private final PaymentClient paymentClient;

    public RideServiceImpl(RideRepo rideRepository, UserFeign userFeign, DriverFeign driverFeign, RideProducer rideProducer, RedisTemplate<String, String> redisTemplate, PaymentClient paymentClient) {
        this.rideRepository = rideRepository;
        this.userFeign = userFeign;
        this.driverFeign = driverFeign;
        this.rideProducer = rideProducer;
        this.redisTemplate = redisTemplate;
        this.paymentClient = paymentClient;
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
            throw new UserOrRIdeNotFOund(
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
            throw new RIdeNotAccepted("Ride is not in REQUESTED status");
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
        //driverFeign.acceptRide(rideRequest);
        RideAcceptedRideEvent rideAcceptedRideEvent = new RideAcceptedRideEvent(
                saveRide.getId().toString(),
                saveRide.getDriverId().toString()

        );
        rideProducer.publishRideAccepted(rideAcceptedRideEvent);

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

    @Override
    public SearchRideResponseDTO searchRide(SearchRideRequest request) {

        List<DriverDTO> nearbyDrivers =
                driverFeign.getNearbyDrivers(
                        request.getPickupLatitude(),
                        request.getPickupLongitude()
                );

        if (nearbyDrivers.isEmpty()) {
            throw new RuntimeException("No nearby drivers found");
        }

        double distance = calculateDistance(
                request.getPickupLatitude(),
                request.getPickupLongitude(),
                request.getDropLatitude(),
                request.getDropLongitude()
        );

        double fare = calculateFare(distance);

        int estimatedTime =
                (int) Math.ceil((distance / 30.0) * 60);

        return SearchRideResponseDTO.builder()
                .estimatedDistance(distance)
                .estimatedFare(fare)
                .estimatedTime(estimatedTime)
                .nearbyDrivers(nearbyDrivers)
                .message("Nearby drivers found successfully")
                .build();
    }

    @Override
    @Transactional
    public void startRide(StartRideDTO startRideDTO) {

        Ride ride = rideRepository.findById(
                UUID.fromString(startRideDTO.getRideId())
        ).orElseThrow(() ->
                new RuntimeException("Ride not found"));

        System.out.println("========== START RIDE ==========");
        System.out.println("Ride ID      : " + ride.getId());
        System.out.println("DB Status    : " + ride.getRideStatus());
        System.out.println("Driver(DB)   : " + ride.getDriverId());
        System.out.println("Driver(REQ)  : " + startRideDTO.getDriverId());

        if (!ride.getDriverId().toString().equals(startRideDTO.getDriverId())) {
            throw new RuntimeException("Driver is not assigned to this ride.");
        }

        if (ride.getRideStatus() == RideStatus.COMPLETED) {
            throw new RuntimeException("Ride is already completed.");
        }

        if (ride.getRideStatus() == RideStatus.INPROGRESS) {
            System.out.println("Ride already started.");
            return;
        }

        if (ride.getRideStatus() != RideStatus.ACCEPTED) {
            throw new RuntimeException("Ride must be ACCEPTED before starting.");
        }

        ride.setRideStatus(RideStatus.INPROGRESS);

        Ride savedRide = rideRepository.save(ride);

        System.out.println("Status after save : " + savedRide.getRideStatus());

        RideStartedEvent event = new RideStartedEvent(
                savedRide.getDriverId().toString(),
                savedRide.getId().toString()
        );

        rideProducer.starRide(event);

        System.out.println("========== START END ==========");
    }

    @Override
    @Transactional
    public CompleteRideResponseDTO completeride(CompleteRideDTO completeRideDTO) {

        Ride ride = rideRepository.findById(
                UUID.fromString(completeRideDTO.getRideId())
        ).orElseThrow(() -> new RuntimeException("Ride not found"));

        // Already waiting for payment
        if (ride.getRideStatus() == RideStatus.PAYMENT_PENDING) {

            // Fetch existing payment instead of creating a new one
            PaymentResponseDTO paymentResponse =
                    paymentClient.getPaymentByRideId(ride.getId());

            return CompleteRideResponseDTO.builder()
                    .rideId(ride.getId().toString())
                    .distance(ride.getDistance().doubleValue())
                    .fare(ride.getFare().doubleValue())
                    .status(ride.getRideStatus().name())
                    .payment(paymentResponse)
                    .message("Ride has already ended. Waiting for payment.")
                    .build();
        }

        // Already completed
        if (ride.getRideStatus() == RideStatus.COMPLETED) {

            return CompleteRideResponseDTO.builder()
                    .rideId(ride.getId().toString())
                    .distance(ride.getDistance().doubleValue())
                    .fare(ride.getFare().doubleValue())
                    .status(ride.getRideStatus().name())
                    .message("Ride is already completed.")
                    .build();
        }

        // Ride must be running
        if (ride.getRideStatus() != RideStatus.INPROGRESS) {
            throw new RuntimeException(
                    "Ride must be INPROGRESS before ending."
            );
        }

        // ---------------- DEBUG ----------------

        System.out.println("========== COMPLETE RIDE ==========");
        System.out.println("Pickup Latitude  : " + ride.getPickupLatitude());
        System.out.println("Pickup Longitude : " + ride.getPickupLongitude());
        System.out.println("Drop Latitude    : " + completeRideDTO.getDropLatitude());
        System.out.println("Drop Longitude   : " + completeRideDTO.getDropLongitude());

        // ---------------------------------------

        ride.setDestinationLatitude(completeRideDTO.getDropLatitude());
        ride.setDestinationLongitude(completeRideDTO.getDropLongitude());

        double distance = calculateDistance(
                ride.getPickupLatitude(),
                ride.getPickupLongitude(),
                completeRideDTO.getDropLatitude(),
                completeRideDTO.getDropLongitude()
        );
        System.out.println("Distance : " + distance);

        double fare = calculateFare(distance);

        System.out.println("Fare : " + fare);
        ride.setDistance(BigDecimal.valueOf(distance));
        ride.setFare(BigDecimal.valueOf(fare));

        // Wait for payment
        ride.setRideStatus(RideStatus.PAYMENT_PENDING);

        Ride savedRide = rideRepository.save(ride);

        PaymentRequestDTO paymentRequest = PaymentRequestDTO.builder()
                .rideId(savedRide.getId())
                .userId(savedRide.getUserId())
                .driverId(savedRide.getDriverId())
                .amount(savedRide.getFare())
                .build();

        PaymentResponseDTO paymentResponse =
                paymentClient.createPayment(paymentRequest);

        if (paymentResponse == null ||
                paymentResponse.getPaymentLink() == null) {

            ride.setRideStatus(RideStatus.INPROGRESS);
            rideRepository.save(ride);

            throw new RuntimeException("Unable to generate payment link.");
        }

        return CompleteRideResponseDTO.builder()
                .rideId(savedRide.getId().toString())
                .distance(savedRide.getDistance().doubleValue())
                .fare(savedRide.getFare().doubleValue())
                .status(savedRide.getRideStatus().name())
                .payment(paymentResponse)
                .message("Ride ended successfully. Please complete the payment.")
                .build();
    }

    @Override
    @Transactional
    public void completeRideAfterPayment(UUID rideId) {

        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));

        if (ride.getRideStatus() == RideStatus.COMPLETED) {
            return;
        }

        if (ride.getRideStatus() != RideStatus.PAYMENT_PENDING) {
            throw new RuntimeException("Ride is not waiting for payment.");
        }

        ride.setRideStatus(RideStatus.COMPLETED);

        rideRepository.save(ride);

        System.out.println("Ride completed successfully : " + ride.getId());
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

