package com.ridebooking.driver_service.DTO;

import lombok.Data;

import java.util.UUID;

@Data
public class DriverLocationUpdateDto {
    private UUID driverId;
    private Double longitude;
    private Double latitude;
}