package com.ridebooking.ride_service.Entity;

import com.ridebooking.ride_service.Enums.RideStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Data
@Table(name = "rides")
public class Ride {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID driverId;

    private UUID userId;

    @Enumerated(EnumType.STRING)
    private RideStatus rideStatus;

    private Double pickupLatitude;
    private Double pickupLongitude;

    private Double destinationLatitude;
    private Double destinationLongitude;

    private BigDecimal fare;

    private BigDecimal distance;
}