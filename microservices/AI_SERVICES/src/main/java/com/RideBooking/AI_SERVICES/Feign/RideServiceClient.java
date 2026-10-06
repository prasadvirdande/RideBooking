package com.RideBooking.AI_SERVICES.Feign;

import com.RideBooking.AI_SERVICES.DTO.DriverLocationDTO;
import com.RideBooking.AI_SERVICES.DTO.RideResponseDto;
import com.RideBooking.AI_SERVICES.DTO.SearchRideRequest;
import com.RideBooking.AI_SERVICES.DTO.SearchRideResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "RIDE-SERVICE")
public interface RideServiceClient {

    @GetMapping("/api/ride/active/{userId}")
    RideResponseDto getActiveRide(
            @PathVariable("userId") UUID userId
    );

    @GetMapping("/api/ride/completed/{userId}")
    RideResponseDto getCompletedRides(
            @PathVariable("userId") UUID userId
    );

    @GetMapping("/api/driver/{driverId}")
    DriverLocationDTO getDriverLocation(
            @PathVariable("driverId") UUID driverId
    );

    @GetMapping("/api/driver/nearby")
    List<DriverLocationDTO> getNearbyDrivers(
            @RequestParam("latitude") Double latitude,
            @RequestParam("longitude") Double longitude
    );

    @PostMapping("/api/ride/search")
    SearchRideResponseDTO searchRide(
            @RequestBody SearchRideRequest request
    );
}