package com.ridebooking.driver_service.DTO;

import com.ridebooking.driver_service.Enum.DriverStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DriverRequestDto {
    private String DriverName;
    private String licenseNumber;
    private String email;
    private String password;
    private LocalDate licenseExpiry;
    private String VehicleNumber;
    private DriverStatus status;
    private Boolean online;
    private Double rating;
    private Integer totalTrips;
}

