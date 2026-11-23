package com.ridebooking.Notification_Service.DTO.Event;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentReceiptEvent {

    private UUID paymentId;
    private UUID rideId;
    private UUID userId;
    private String email;
    private BigDecimal amount;

    private String paymentStatus;
    private String paymentMethod;

    private LocalDateTime paymentTime;
}
