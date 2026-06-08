package com.ridebooking.ride_service.Controller;


import com.ridebooking.ride_service.DTO.*;
import com.ridebooking.ride_service.Service.RideService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/ride")
public class RideController {

    private final RideService rideService;

    public RideController(RideService rideService) {
        this.rideService = rideService;
    }

    @PostMapping
    public ResponseEntity<RideResponseDto> createRide(@RequestBody RideRequest rideRequest) {
        return ResponseEntity.ok(rideService.createRide(rideRequest));
    }

    @PutMapping("/id/{rideId}")
    public ResponseEntity<AcceptedRideResponseDTO> updateRide(@PathVariable UUID rideId, @RequestBody AcceptRideRequest rideRequest) {
        return ResponseEntity.ok(rideService.updateRide(rideId, rideRequest));
    }
    @PostMapping("/verify/otp")
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtpDTO verifyOtpDTO) {
        rideService.verifyOtp(verifyOtpDTO);
        return ResponseEntity.ok("OTP verified successfully");
    }
}
