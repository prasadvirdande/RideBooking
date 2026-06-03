package com.ridebooking.ride_service.DTO;

import lombok.Data;

@Data
public class RideRequest {

    private String driverId;

    private String userId;

    private Double pickupLatitude;
    private Double pickupLongitude;

    private Double dropLatitude;
    private Double dropLongitude;
}