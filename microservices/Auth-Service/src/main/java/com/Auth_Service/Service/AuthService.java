package com.Auth_Service.Service;

import com.Auth_Service.DTO.AuthRequest;
import com.Auth_Service.DTO.AuthResponse;
import com.Auth_Service.DTO.DriverRequest;
import com.Auth_Service.DTO.LoginRequest;

public interface AuthService {
    AuthResponse register(AuthRequest authRequest);

    AuthResponse login(LoginRequest authRequest);

    void logout(String token);

    AuthResponse Driverregister(DriverRequest authRequest);
}
