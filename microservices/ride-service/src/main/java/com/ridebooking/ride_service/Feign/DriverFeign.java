package com.ridebooking.ride_service.Feign;
import com.ridebooking.ride_service.DTO.DriverDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "driver-service", url = "http://localhost:8083")
public interface DriverFeign {

    @GetMapping("/api/driver/id/{driverId}")
    DriverDTO getDriverById(@PathVariable String driverId);
}
