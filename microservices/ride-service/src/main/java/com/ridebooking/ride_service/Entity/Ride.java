package com.ridebooking.ride_service.Entity;

import com.ridebooking.ride_service.Enums.RideStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(
        name = "rides",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_rides_active_user",
                        columnNames = "active_user_id"
                ),
                @UniqueConstraint(
                        name = "uk_rides_completed_payment",
                        columnNames = "completed_payment_id"
                )
        }
)
public class Ride {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Version
    private Long version;

    @Column(nullable = false)
    private UUID driverId;

    @Column(nullable = false)
    private UUID userId;

    @Column(name = "active_user_id")
    private UUID activeUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RideStatus rideStatus;

    private Double pickupLatitude;
    private Double pickupLongitude;

    private Double destinationLatitude;
    private Double destinationLongitude;

    @Column(precision = 12, scale = 2)
    private BigDecimal fare;

    @Column(precision = 12, scale = 3)
    private BigDecimal distance;

    @Column(name = "completed_payment_id")
    private UUID completedPaymentId;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        updatedAt = now;

        if (activeUserId == null
                && rideStatus != RideStatus.COMPLETED
                && rideStatus != RideStatus.CANCELLED) {

            activeUserId = userId;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}