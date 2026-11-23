package com.ridebooking.Payment_services.DTO;

import com.ridebooking.Payment_services.Entity.Enums.PaymentMethods;
import com.ridebooking.Payment_services.Entity.Enums.PaymentStatus;

import lombok.*;

import java.math.BigDecimal;
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

        private PaymentStatus paymentStatus;

        private String paymentLink;

        private String currency;



    }

