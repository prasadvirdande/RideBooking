package com.ridebooking.ride_service.DTO;

import lombok.Data;

@Data
public class VerifyOtpDTO {
    private String rideId;
    private String otp;
}
