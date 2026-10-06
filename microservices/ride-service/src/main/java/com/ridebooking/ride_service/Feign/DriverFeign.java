package com.ridebooking.ride_service.Feign;

import com.ridebooking.ride_service.DTO.AcceptRideRequest;
import com.ridebooking.ride_service.DTO.DriverDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "driver-service")
public interface DriverFeign {

    @GetMapping("/api/driver/id/{driverId}")
    DriverDTO getDriverById(
            @PathVariable("driverId") String driverId
    );

    @PostMapping("/api/driver/accept/ride")
    void acceptRide(
            @RequestBody AcceptRideRequest acceptRideRequest
    );

    @GetMapping("/api/driver/nearby")
    List<DriverDTO> getNearbyDrivers(
            @RequestParam("latitude") Double latitude,
            @RequestParam("longitude") Double longitude
    );
}