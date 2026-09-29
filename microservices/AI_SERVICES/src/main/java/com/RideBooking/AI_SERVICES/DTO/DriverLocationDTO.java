package com.RideBooking.AI_SERVICES.DTO;

import lombok.Data;

import java.util.UUID;

@Data
public class DriverLocationDTO {

    private UUID driverId;
    private Double latitude;
    private Double longitude;
}
