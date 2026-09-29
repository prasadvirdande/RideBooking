package com.RideBooking.AI_SERVICES.Feign;

import com.RideBooking.AI_SERVICES.DTO.DriverLocationDTO;
import com.RideBooking.AI_SERVICES.DTO.RideResponseDto;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

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

   @GetMapping("/api/driver/")
   DriverLocationDTO getDriverLocation(
            @PathVariable("driverId") UUID driverId
    );

   @GetMapping("/api/driver/nearby/{latitude}/{longitude}")
   List<DriverLocationDTO> getNearbyDrivers(
            @PathVariable("latitude") Double latitude,
            @PathVariable("longitude") Double longitude
    );
}
