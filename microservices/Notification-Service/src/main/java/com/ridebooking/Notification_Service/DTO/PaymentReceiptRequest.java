package com.ridebooking.Notification_Service.DTO;


import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentReceiptRequest {

    private String email;
    private String rideId;
    private String paymentId;
    private BigDecimal amount;
}
