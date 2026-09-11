package com.ridebooking.Payment_services.Service;

import com.ridebooking.Payment_services.DTO.PaymentRequestDTO;
import com.ridebooking.Payment_services.DTO.PaymentResponseDTO;

import java.util.UUID;

public interface PaymentServiceINter {
    PaymentResponseDTO createPayment(PaymentRequestDTO request, String idempotencyKey);

    PaymentResponseDTO getPaymentByRideId(UUID rideId);

    void handleWebhook(String payload, String signature);
}
