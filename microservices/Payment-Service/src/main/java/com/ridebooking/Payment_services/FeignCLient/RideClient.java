package com.ridebooking.Payment_services.FeignCLient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.UUID;

@FeignClient(
        name = "ride-service",
        url = "http://localhost:8080"
)
public interface RideClient {

    @PutMapping("/api/ride/payment-success/{rideId}")
    void paymentSuccess(@PathVariable UUID rideId);

}