package com.ridebooking.driver_service.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DriverLoginResponseDTO {


    private String id;
    private String email;
    private String password;
    private String role;


}