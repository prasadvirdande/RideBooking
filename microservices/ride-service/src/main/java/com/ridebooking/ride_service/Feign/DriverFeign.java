package com.ridebooking.ride_service.Feign;
import com.ridebooking.ride_service.DTO.AcceptRideRequest;
import com.ridebooking.ride_service.DTO.DriverDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "driver-service", url = "http://localhost:8083")
public interface DriverFeign {

    @GetMapping("/api/driver/id/{driverId}")
    DriverDTO getDriverById(@PathVariable String driverId);

    @PostMapping("/api/driver/accept/ride")
    void acceptRide(@RequestBody AcceptRideRequest acceptRideRequest);
}
