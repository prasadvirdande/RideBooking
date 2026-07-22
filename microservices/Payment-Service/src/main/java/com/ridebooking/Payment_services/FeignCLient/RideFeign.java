package com.ridebooking.Payment_services.FeignCLient;

import com.ridebooking.Payment_services.DTO.RideResponseDto;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "RIDE-SERVICE", url = "http://localhost:8080")
public interface RideFeign {

    @GetMapping("/api/rides/id/{rideId}")
    RideResponseDto getRideById(
            @PathVariable UUID rideId
    );
}