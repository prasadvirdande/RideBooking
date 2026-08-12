package com.ridebooking.ride_service.Feign;

import com.ridebooking.ride_service.DTO.PaymentRequestDTO;
import com.ridebooking.ride_service.DTO.PaymentResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.UUID;

@FeignClient(name = "PAYMENT-SERVICE")
public interface PaymentClient {

    @PostMapping("/api/payment/create")
    PaymentResponseDTO createPayment(PaymentRequestDTO request);
    @GetMapping("/api/payment/ride/{rideId}")
    PaymentResponseDTO getPaymentByRideId(@PathVariable("rideId") UUID rideId);




}