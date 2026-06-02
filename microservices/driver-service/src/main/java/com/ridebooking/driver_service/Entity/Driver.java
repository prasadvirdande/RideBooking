package com.ridebooking.driver_service.Entity;

import com.ridebooking.driver_service.Enum.DriverStatus;
import com.ridebooking.driver_service.Enum.Role;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "drivers")
@Data
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

   private String DriverName;

   private String email;

   private String password;



    private String licenseNumber;

    private LocalDate licenseExpiry;

    private String VehicleNumber;

    @Enumerated(EnumType.STRING)
    private DriverStatus status;

    @Enumerated(EnumType.STRING)
    private Role role;

    private Boolean online;

    private Double rating;

    private Integer totalTrips;

}