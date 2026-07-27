package com.ridebooking.Notification_Service.Entity;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "invoice")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID invoiceId;

    @Column(nullable = false, unique = true)
    private String invoiceNumber;

    private String rideId;

    private String paymentId;

    private String userEmail;

    private BigDecimal amount;

    private LocalDateTime generatedAt;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] pdf;
}
