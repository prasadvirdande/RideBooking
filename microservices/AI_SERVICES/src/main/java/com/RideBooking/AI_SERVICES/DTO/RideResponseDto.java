package com.RideBooking.AI_SERVICES.DTO;


import lombok.Data;



import java.util.UUID;


@Data
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

    private String status;
    private String message;
}