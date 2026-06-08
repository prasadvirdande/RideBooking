package com.ridebooking.ride_service.DTO;


import lombok.Data;

@Data
public class AcceptRideRequest {

    private String rideId;
    private String driverId;
}
