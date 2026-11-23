package com.ridebooking.Payment_services.DTO.Event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentCompletedEvent {

    private UUID paymentId;
    private UUID rideId;
    private UUID userId;
    private UUID driverId;
    private BigDecimal amount;
    private String paymentStatus;
}
