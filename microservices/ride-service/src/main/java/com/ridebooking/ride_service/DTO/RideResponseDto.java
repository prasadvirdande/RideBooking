package com.ridebooking.ride_service.DTO;

import com.ridebooking.ride_service.Enums.RideStatus;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class RideResponseDto {

    private UUID rideId;

    private UUID userId;

    private UUID driverId;

    private Double pickupLatitude;
    private Double pickupLongitude;

    private Double destinationLatitude;
    private Double destinationLongitude;

    private Double fare;

    private Double distance;

    private RideStatus status;

    private String message;
}