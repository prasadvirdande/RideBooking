package com.ridebooking.ride_service.Controller;


import com.ridebooking.ride_service.DTO.*;
import com.ridebooking.ride_service.Service.RideService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ride")
public class RideController {

    private final RideService rideService;


    public RideController(RideService rideService) {
        this.rideService = rideService;
    }


    @PostMapping("/search")
    public ResponseEntity<SearchRideResponseDTO> searchRide(
            @RequestBody SearchRideRequest request) {

        return ResponseEntity.ok(
                rideService.searchRide(request)
        );
    }


    @PostMapping
    public ResponseEntity<RideResponseDto> createRide(@RequestBody RideRequest rideRequest) {
        return ResponseEntity.ok(rideService.createRide(rideRequest));
    }

    @PutMapping("/id/{rideId}")
    public ResponseEntity<AcceptedRideResponseDTO> updateRide(@PathVariable UUID rideId, @RequestBody AcceptRideRequest rideRequest) {
        System.out.println("UPDATE RIDE API HIT");
        return ResponseEntity.ok(rideService.updateRide(rideId, rideRequest));
    }
    @PostMapping("/verify/otp")
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtpDTO verifyOtpDTO) {
        rideService.verifyOtp(verifyOtpDTO);
        return ResponseEntity.ok("OTP verified successfully");
    }
    @PutMapping("/start")
    public ResponseEntity<String> startRide(@RequestBody StartRideDTO startRideDTO){
        rideService.startRide(startRideDTO);
        return ResponseEntity.ok("Ride started");

    }
    @PostMapping("/complete")
    public  ResponseEntity<CompleteRideResponseDTO> completeRide(@RequestBody  CompleteRideDTO completeRideDTO){
        return ResponseEntity.ok(rideService.completeride(completeRideDTO));
    }
    @PutMapping("/payment-success/{rideId}")
    public ResponseEntity<String> paymentSuccess(
            @PathVariable UUID rideId) {

        System.out.println("PAYMENT SUCCESS API HIT   ");

       rideService.completeRideAfterPayment(rideId);


        return ResponseEntity.ok("SUCCESSSSSSSSS");
    }
}
