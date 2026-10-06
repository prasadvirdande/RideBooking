package com.RideBooking.AI_SERVICES.DTO;

import lombok.Data;

import java.util.List;

@Data
public class SearchRideResponseDTO {

    private Double estimatedDistance;
    private Double estimatedFare;
    private Integer estimatedTime;

    private List<DriverDTO> nearbyDrivers;

    private String message;
}