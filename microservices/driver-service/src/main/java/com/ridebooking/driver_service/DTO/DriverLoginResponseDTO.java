package com.ridebooking.driver_service.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DriverLoginResponseDTO {

    private String email;
    private String password;
    private String role;
}