package com.ridebooking.driver_service.DTO;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class DriverLocationResponseDto {

    private UUID driverId;
    private Double latitude;
    private Double longitude;
    private String message;
}