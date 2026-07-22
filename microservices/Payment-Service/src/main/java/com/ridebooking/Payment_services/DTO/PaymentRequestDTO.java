package com.ridebooking.Payment_services.DTO;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class PaymentRequestDTO {
    private UUID rideId;

    private UUID userId;

    private UUID driverId;

    private BigDecimal amount;
}
