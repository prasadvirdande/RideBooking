package com.ridebooking.user_service.DTO;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class UserLoginRequestDTO {
    private String email;
    private String password;
}
