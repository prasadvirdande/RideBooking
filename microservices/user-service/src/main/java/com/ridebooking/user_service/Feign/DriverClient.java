package com.ridebooking.user_service.Feign;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "driver-service",
        url = "http://localhost:8083"
)
public interface DriverClient {

    @GetMapping("/api/driver/nearby")
    Object getNearbyDrivers(
            @RequestParam("latitude") String latitude,
            @RequestParam("longitude") String longitude
    );
}