package com.ridebooking.ride_service.Service;


import com.ridebooking.ride_service.DTO.*;

import java.util.UUID;

public interface RideService {
    RideResponseDto createRide(RideRequest rideRequest);

    AcceptedRideResponseDTO updateRide(UUID rideId, AcceptRideRequest rideRequest);

    void verifyOtp(VerifyOtpDTO verifyOtpDTO);
}
