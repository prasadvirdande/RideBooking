package com.ridebooking.ride_service.Service;


import com.ridebooking.ride_service.DTO.RideRequest;
import com.ridebooking.ride_service.DTO.RideResponseDto;

public interface RideService {
    RideResponseDto createRide(RideRequest rideRequest);
}
