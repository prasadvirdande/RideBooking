package com.ridebooking.driver_service.DTO;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class DriverResponseDto {
    private UUID id;
    private String email;
    private String role;
    private String DriverName;
    private String VehicleNumber;
    private Integer totalTrips;
    private Double rating;

}
