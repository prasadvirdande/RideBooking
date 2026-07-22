package com.ridebooking.ride_service.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponseDTO {

    private UUID paymentId;

    private UUID rideId;

    private UUID userId;

    private UUID driverId;

    private BigDecimal amount;

    private String paymentStatus;

    private String paymentMethod;

    private String gatewayName;

    private String gatewayOrderId;

    private String gatewayPaymentId;

    private String paymentLink;

    private String qrCode;

    private String transactionId;

    private LocalDateTime paymentTime;

    private LocalDateTime createdAt;

}