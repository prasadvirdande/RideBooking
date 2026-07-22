package com.ridebooking.ride_service.DTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CompleteRideResponseDTO {
    private String rideId;


    private Double destinationLatitude;

    private Double destinationLongitude;

    private Double distance;

    private Double fare;

    private PaymentResponseDTO payment;

    private String status;

    private String message;
}
