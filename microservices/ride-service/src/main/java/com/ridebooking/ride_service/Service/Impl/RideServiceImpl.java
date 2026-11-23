package com.ridebooking.ride_service.Service.Impl;

import com.ridebooking.ride_service.DTO.*;
import com.ridebooking.ride_service.DTO.Event.RideAcceptedRideEvent;
import com.ridebooking.ride_service.DTO.Event.RideStartedEvent;
import com.ridebooking.ride_service.Entity.Ride;
import com.ridebooking.ride_service.Enums.RideStatus;
import com.ridebooking.ride_service.Exception.DriverNotAvailable;
import com.ridebooking.ride_service.Exception.RIdeNotAccepted;
import com.ridebooking.ride_service.Exception.UserOrRIdeNotFOund;
import com.ridebooking.ride_service.Feign.DriverFeign;
import com.ridebooking.ride_service.Feign.PaymentClient;
import com.ridebooking.ride_service.Feign.UserFeign;
import com.ridebooking.ride_service.Repository.RideRepo;
import com.ridebooking.ride_service.Service.Kafka.RideProducer;
import com.ridebooking.ride_service.Service.RideCalculationService;
import com.ridebooking.ride_service.Service.RideService;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
public class RideServiceImpl implements RideService {

    private final RideRepo rideRepository;
    private final UserFeign userFeign;
    private final DriverFeign driverFeign;
    private final RideProducer rideProducer;
    private final RedisTemplate<String, String> redisTemplate;
    private final PaymentClient paymentClient;
    private final RideCalculationService rideCalculationService;


    public RideServiceImpl(
            RideRepo rideRepository,
            UserFeign userFeign,
            DriverFeign driverFeign,
            RideProducer rideProducer,
            RedisTemplate<String, String> redisTemplate,
            PaymentClient paymentClient,
            RideCalculationService rideCalculationService) {

        this.rideRepository = rideRepository;
        this.userFeign = userFeign;
        this.driverFeign = driverFeign;
        this.rideProducer = rideProducer;
        this.redisTemplate = redisTemplate;
        this.paymentClient = paymentClient;
        this.rideCalculationService = rideCalculationService;
    }


    // CREATE RIDE
    @Override
    public RideResponseDto createRide(RideRequest rideRequest) {

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
                rideCalculationService.calculateDistance(
                        rideRequest.getPickupLatitude(),
                        rideRequest.getPickupLongitude(),
                        rideRequest.getDropLatitude(),
                        rideRequest.getDropLongitude()
                );

        double fare =
                rideCalculationService.calculateFare(distance);

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


    // ACCEPT RIDE
    @Override
    @Transactional
    public AcceptedRideResponseDTO updateRide(
            UUID rideId,
            AcceptRideRequest rideRequest) {

        System.out.println("========== ACCEPT RIDE START ==========");

        System.out.println("Ride ID from URL: " + rideId);
        System.out.println("Driver ID from request: " + rideRequest.getDriverId());

        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));

        System.out.println("Ride found: " + ride.getId());
        System.out.println("Current ride status: " + ride.getRideStatus());
        System.out.println("Driver assigned to ride: " + ride.getDriverId());

        if (ride.getRideStatus() != RideStatus.REQUESTED) {

            throw new RIdeNotAccepted(
                    "Ride is not in REQUESTED status. Current status: "
                            + ride.getRideStatus()
            );
        }

        if (!ride.getDriverId().toString()
                .equals(rideRequest.getDriverId())) {

            throw new RuntimeException(
                    "Driver is not the one who requested the ride"
            );
        }

        System.out.println("Changing status REQUESTED -> ACCEPTED");

        ride.setRideStatus(RideStatus.ACCEPTED);

        Ride saveRide = rideRepository.save(ride);

        System.out.println("Status after save: "
                + saveRide.getRideStatus());

        String otp = String.valueOf(
                1000 + new Random().nextInt(9000)
        );

        redisTemplate.opsForValue().set(
                "ride:otp:" + saveRide.getId(),
                otp,
                Duration.ofMinutes(60)
        );

        RideAcceptedRideEvent event =
                new RideAcceptedRideEvent(
                        saveRide.getId().toString(),
                        saveRide.getDriverId().toString()
                );

        rideProducer.publishRideAccepted(event);

        System.out.println("========== ACCEPT RIDE END ==========");

        return AcceptedRideResponseDTO.builder()
                .rideId(saveRide.getId().toString())
                .driverId(saveRide.getDriverId().toString())
                .otp(otp)
                .status(saveRide.getRideStatus().name())
                .build();
    }


    // VERIFY OTP
    @Override
    public void verifyOtp(VerifyOtpDTO verifyOtpDTO) {

        System.out.println("otp:" + verifyOtpDTO.getOtp());

        String storedOtp =
                redisTemplate.opsForValue().get(
                        "ride:otp:" + verifyOtpDTO.getRideId()
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

        if (ride.getRideStatus() != RideStatus.ACCEPTED) {

            throw new RuntimeException(
                    "Ride is not in ACCEPTED status"
            );
        }

        redisTemplate.delete(
                "ride:otp:" + verifyOtpDTO.getRideId()
        );

        System.out.println(
                "OTP verified for ride: " + ride.getId()
        );
    }


    // SEARCH RIDE
    @Override
    public SearchRideResponseDTO searchRide(SearchRideRequest request) {

        List<DriverDTO> nearbyDrivers =
                driverFeign.getNearbyDrivers(
                        request.getPickupLatitude(),
                        request.getPickupLongitude()
                );

        if (nearbyDrivers.isEmpty()) {

            throw new DriverNotAvailable(
                    "Driver Not Available At Moment For This Location"
            );
        }

        double distance =
                rideCalculationService.calculateDistance(
                        request.getPickupLatitude(),
                        request.getPickupLongitude(),
                        request.getDropLatitude(),
                        request.getDropLongitude()
                );

        double fare =
                rideCalculationService.calculateFare(distance);

        int estimatedTime =
                rideCalculationService.calculateEstimatedTime(distance);

        return SearchRideResponseDTO.builder()
                .estimatedDistance(distance)
                .estimatedFare(fare)
                .estimatedTime(estimatedTime)
                .nearbyDrivers(nearbyDrivers)
                .message("Nearby drivers found successfully")
                .build();
    }


    // START RIDE
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

        if (!ride.getDriverId().toString()
                .equals(startRideDTO.getDriverId())) {

            throw new RuntimeException(
                    "Driver is not assigned to this ride."
            );
        }

        if (ride.getRideStatus() == RideStatus.COMPLETED) {

            throw new RuntimeException(
                    "Ride is already completed."
            );
        }

        if (ride.getRideStatus() == RideStatus.INPROGRESS) {

            System.out.println("Ride already started.");
            return;
        }

        if (ride.getRideStatus() != RideStatus.ACCEPTED) {

            throw new RuntimeException(
                    "Ride must be ACCEPTED before starting."
            );
        }

        ride.setRideStatus(RideStatus.INPROGRESS);

        Ride savedRide = rideRepository.save(ride);

        System.out.println(
                "Status after save : " + savedRide.getRideStatus()
        );

        RideStartedEvent event = new RideStartedEvent(
                savedRide.getId().toString(),
                savedRide.getDriverId().toString()
        );

        rideProducer.starRide(event);

        System.out.println("========== START END ==========");
    }


    // COMPLETE RIDE / INITIATE PAYMENT
// COMPLETE RIDE / INITIATE PAYMENT
    @Override
    public CompleteRideResponseDTO completeride(
            CompleteRideDTO completeRideDTO) {

        // 1. Fetch ride
        Ride ride = rideRepository.findById(
                UUID.fromString(completeRideDTO.getRideId())
        ).orElseThrow(() ->
                new RuntimeException("Ride not found"));

        // 2. If payment is already pending, fetch or create payment
        if (ride.getRideStatus() == RideStatus.PAYMENT_PENDING) {

            PaymentResponseDTO paymentResponse =
                    getOrCreatePayment(ride);

            return buildCompleteRideResponse(
                    ride,
                    paymentResponse,
                    "Ride has already ended. Please complete the payment."
            );
        }

        // 3. If ride is already completed
        if (ride.getRideStatus() == RideStatus.COMPLETED) {

            return CompleteRideResponseDTO.builder()
                    .rideId(ride.getId().toString())
                    .distance(ride.getDistance().doubleValue())
                    .fare(ride.getFare().doubleValue())
                    .status(ride.getRideStatus().name())
                    .message("Ride is already completed.")
                    .build();
        }

        // 4. Ride must be INPROGRESS
        if (ride.getRideStatus() != RideStatus.INPROGRESS) {

            throw new RuntimeException(
                    "Ride must be INPROGRESS before ending."
            );
        }

        // 5. Calculate distance
        double distance = rideCalculationService.calculateDistance(
                ride.getPickupLatitude(),
                ride.getPickupLongitude(),
                completeRideDTO.getDropLatitude(),
                completeRideDTO.getDropLongitude()
        );

        // 6. Calculate fare
        double fare = rideCalculationService.calculateFare(distance);

        // 7. Update destination
        ride.setDestinationLatitude(
                completeRideDTO.getDropLatitude()
        );

        ride.setDestinationLongitude(
                completeRideDTO.getDropLongitude()
        );

        // 8. Update distance and fare
        ride.setDistance(BigDecimal.valueOf(distance));
        ride.setFare(BigDecimal.valueOf(fare));

        // 9. Change status to PAYMENT_PENDING
        ride.setRideStatus(RideStatus.PAYMENT_PENDING);

        // 10. Save ride
        Ride savedRide = rideRepository.save(ride);

        // 11. Create or retrieve payment
        PaymentResponseDTO paymentResponse =
                getOrCreatePayment(savedRide);

        // 12. Return response
        return buildCompleteRideResponse(
                savedRide,
                paymentResponse,
                "Ride ended successfully. Please complete the payment."
        );
    }


    // GET EXISTING PAYMENT OR CREATE NEW PAYMENT
    private PaymentResponseDTO getOrCreatePayment(Ride ride) {

        try {

            // First check whether payment already exists
            PaymentResponseDTO existingPayment =
                    paymentClient.getPaymentByRideId(ride.getId());

            if (existingPayment != null) {

                log.info(
                        "Existing payment found for ride: {}",
                        ride.getId()
                );

                return existingPayment;
            }

        } catch (FeignException.NotFound ex) {

            log.info(
                    "No payment found for ride: {}. Creating payment.",
                    ride.getId()
            );

        } catch (FeignException ex) {

            log.error(
                    "Payment Service communication failed. Status: {}",
                    ex.status(),
                    ex
            );

            throw new RuntimeException(
                    "Unable to communicate with Payment Service."
            );
        }

        // Create payment request
        PaymentRequestDTO paymentRequest =
                PaymentRequestDTO.builder()
                        .rideId(ride.getId())
                        .userId(ride.getUserId())
                        .driverId(ride.getDriverId())
                        .amount(ride.getFare())
                        .build();

        // Use ride ID as idempotency key
        String idempotencyKey = ride.getId().toString();

        try {

            PaymentResponseDTO paymentResponse =
                    paymentClient.createPayment(
                            paymentRequest,
                            idempotencyKey
                    );

            if (paymentResponse == null ||
                    paymentResponse.getPaymentLink() == null) {

                throw new RuntimeException(
                        "Unable to generate payment link."
                );
            }

            log.info(
                    "Payment created successfully for ride: {}",
                    ride.getId()
            );

            return paymentResponse;

        } catch (FeignException ex) {

            log.error(
                    "Payment creation failed. HTTP status: {}",
                    ex.status(),
                    ex
            );

            throw new RuntimeException(
                    "Payment creation failed. Please retry."
            );
        }
    }


    // BUILD COMPLETE RIDE RESPONSE
    private CompleteRideResponseDTO buildCompleteRideResponse(
            Ride ride,
            PaymentResponseDTO paymentResponse,
            String message) {

        return CompleteRideResponseDTO.builder()
                .rideId(ride.getId().toString())
                .distance(ride.getDistance().doubleValue())
                .fare(ride.getFare().doubleValue())
                .status(ride.getRideStatus().name())
                .payment(paymentResponse)
                .message(message)
                .build();
    }
    // COMPLETE RIDE AFTER PAYMENT SUCCESS
    @Override
    @Transactional
    public void completeRideAfterPayment(UUID rideId) {

        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() ->
                        new RuntimeException("Ride not found"));

        if (ride.getRideStatus() == RideStatus.COMPLETED) {
            return;
        }

        if (ride.getRideStatus() != RideStatus.PAYMENT_PENDING) {

            throw new RuntimeException(
                    "Ride is not waiting for payment."
            );
        }

        ride.setRideStatus(RideStatus.COMPLETED);
        ride.setActiveUserId(null);

        rideRepository.save(ride);

        System.out.println(
                "Ride completed successfully : " + ride.getId()
        );
    }


    // GET ACTIVE RIDE
    @Override
    public RideResponseDto getActiveRide(UUID userId) {

        Ride ride =
                rideRepository.findActiveRideByUserId(userId)
                        .orElseThrow(() ->
                                new RuntimeException("No active ride found"));

        return RideResponseDto.builder()
                .rideId(ride.getId())
                .userId(ride.getUserId())
                .driverId(ride.getDriverId())
                .pickupLatitude(ride.getPickupLatitude())
                .pickupLongitude(ride.getPickupLongitude())
                .destinationLatitude(ride.getDestinationLatitude())
                .destinationLongitude(ride.getDestinationLongitude())
                .fare(ride.getFare().doubleValue())
                .distance(ride.getDistance().doubleValue())
                .status(ride.getRideStatus())
                .message("Active ride found")
                .build();
    }


    // GET LAST COMPLETED RIDE
    @Override
    public RideResponseDto getLastCompletedRide(UUID userId) {

        Ride ride = rideRepository
                .findFirstByUserIdAndRideStatusOrderByCreatedAtDescIdDesc(
                        userId,
                        RideStatus.COMPLETED
                )
                .orElseThrow(() ->
                        new RuntimeException("No completed ride found"));

        return RideResponseDto.builder()
                .rideId(ride.getId())
                .userId(ride.getUserId())
                .driverId(ride.getDriverId())
                .pickupLatitude(ride.getPickupLatitude())
                .pickupLongitude(ride.getPickupLongitude())
                .destinationLatitude(ride.getDestinationLatitude())
                .destinationLongitude(ride.getDestinationLongitude())
                .fare(ride.getFare().doubleValue())
                .distance(ride.getDistance().doubleValue())
                .status(ride.getRideStatus())
                .message("RIDE IS COMPLETED")
                .build();
    }
}