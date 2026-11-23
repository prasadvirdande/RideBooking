package com.ridebooking.Notification_Service.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "invoice",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_invoice_payment_id",
                        columnNames = "payment_id"
                )
        }
)
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

    @Column(nullable = false, unique = true)
    private String paymentId;

    private String userEmail;

    private BigDecimal amount;

    private LocalDateTime generatedAt;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] pdf;

    @Column(nullable = false)
    @Builder.Default
    private String emailStatus = "PENDING";

    private LocalDateTime emailSentAt;
}