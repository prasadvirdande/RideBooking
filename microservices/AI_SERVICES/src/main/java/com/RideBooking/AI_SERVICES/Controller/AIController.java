package com.RideBooking.AI_SERVICES.Controller;


import com.RideBooking.AI_SERVICES.DTO.RideResponseDto;
import com.RideBooking.AI_SERVICES.Feign.RideServiceClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final RideServiceClient rideServiceClient;

    public AIController(RideServiceClient rideServiceClient) {
        this.rideServiceClient = rideServiceClient;
    }

    @GetMapping("/ride/{userId}")
    public RideResponseDto getRide(@PathVariable UUID userId) {
        return rideServiceClient.getActiveRide(userId);
    }

    @GetMapping("/ride/completed/{userId}")
    public RideResponseDto getCompletedRides(@PathVariable UUID userId) {
        return rideServiceClient.getCompletedRides(userId);
    }
}
