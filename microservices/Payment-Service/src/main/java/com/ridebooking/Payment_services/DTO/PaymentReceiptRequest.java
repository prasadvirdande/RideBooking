package com.ridebooking.Payment_services.DTO;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PaymentReceiptRequest {

    private String email;
    private String rideId;
    private String paymentId;
    private BigDecimal amount;
}