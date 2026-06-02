package com.Auth_Service.DTO;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DriverRequest {

    private String email;
    private String password;
    private String DriverName;
    private String licenseNumber;
    private LocalDate licenseExpiry;
    private String VehicleNumber;
    private Boolean online;
    private Double rating;
    private Integer totalTrips;
}
