package com.ridebooking.Payment_services.DTO;

import lombok.Data;

import java.util.UUID;

@Data
public class RideResponseDto {

    private UUID rideId;
    private UUID userId;
    private UUID driverId;

    private Double fare;

}