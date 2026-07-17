package com.ridebooking.ride_service.DTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CompleteRideResponseDTO {
    private String rideId;

//    private String userId;
//
//    private String driverId;
//
//    private Double pickupLatitude;
//
//    private Double pickupLongitude;

    private Double destinationLatitude;

    private Double destinationLongitude;

    private Double distance;

    private Double fare;

    private String status;

    private String message;
}
