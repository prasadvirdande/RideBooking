package com.ridebooking.ride_service.DTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AcceptedRideResponseDTO {
    private String rideId;
    private String driverId;
    private String otp;
    private String status;
}
