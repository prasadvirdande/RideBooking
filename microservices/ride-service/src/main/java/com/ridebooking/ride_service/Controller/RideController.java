package com.ridebooking.ride_service.Controller;


import com.ridebooking.ride_service.DTO.RideRequest;
import com.ridebooking.ride_service.DTO.RideResponseDto;
import com.ridebooking.ride_service.Service.RideService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
