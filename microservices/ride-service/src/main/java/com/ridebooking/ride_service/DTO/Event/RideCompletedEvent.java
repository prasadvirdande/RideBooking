package com.ridebooking.ride_service.DTO.Event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideCompletedEvent {

    private UUID eventId;
    private UUID rideId;
    private UUID userId;
    private UUID driverId;
    private UUID paymentId;
    private BigDecimal fare;
    private Instant occurredAt;
    private int eventVersion;
}