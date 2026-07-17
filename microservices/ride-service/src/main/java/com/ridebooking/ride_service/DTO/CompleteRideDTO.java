package com.ridebooking.ride_service.DTO;

import lombok.Data;

@Data
public class CompleteRideDTO {

    private String rideId;
    private Double dropLatitude;
    private Double dropLongitude;
}