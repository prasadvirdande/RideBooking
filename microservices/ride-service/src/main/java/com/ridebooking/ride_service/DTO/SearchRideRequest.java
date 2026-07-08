package com.ridebooking.ride_service.DTO;

import lombok.Data;

@Data
public class SearchRideRequest {

    private Double pickupLatitude;
    private Double pickupLongitude;

    private Double dropLatitude;
    private Double dropLongitude;
}
