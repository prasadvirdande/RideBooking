package com.ridebooking.driver_service.DTO;

import lombok.Data;

@Data
public class DriverLoginRequestDto {

    private String email;
    private String password;
}
