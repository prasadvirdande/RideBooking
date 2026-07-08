package com.ridebooking.ride_service.DTO;


import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SearchRideResponseDTO {

    private Double estimatedDistance;
    private Double estimatedFare;
    private Integer estimatedTime;

    private List<DriverDTO> nearbyDrivers;

    private String message;
}
