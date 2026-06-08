
package com.Auth_Service.Service.Impl;

import com.Auth_Service.DTO.*;
import com.Auth_Service.Feign.DriverClient;
import com.Auth_Service.Feign.UserClient;
import com.Auth_Service.Security.Jwtservice;
import com.Auth_Service.Service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthserviceImpl implements AuthService {

    private final UserClient userClient;
    private final DriverClient driverClient;
    private final PasswordEncoder passwordEncoder;
    private final Jwtservice jwtservice;

    @Override
    public AuthResponse register(AuthRequest authRequest) {

        authRequest.setPassword(
                passwordEncoder.encode(authRequest.getPassword())
        );

        UserDto user = userClient.createUser(authRequest);

        if (user == null) {
            throw new RuntimeException("User registration failed");
        }

        String token = jwtservice.generateToken(
                user.getEmail(),
                Collections.singleton(user.getRole())
        );

        AuthResponse response = new AuthResponse();
        response.setMessage("Registration Successful");
        response.setToken(token);

        return response;
    }

    @Override
    public AuthResponse Driverregister(DriverRequest driverRequest) {

        driverRequest.setPassword(
                passwordEncoder.encode(driverRequest.getPassword())
        );

        UserDto user = driverClient.createDriver(driverRequest);

        if (user == null) {
            throw new RuntimeException("Driver registration failed");
        }

        String token = jwtservice.generateToken(
                user.getEmail(),
                Collections.singleton(user.getRole())
        );

        AuthResponse response = new AuthResponse();
        response.setMessage("Driver Registration Successful");
        response.setToken(token);

        return response;
    }
    @Override
    public AuthResponse login(LoginRequest request) {

        UserDto account = null;


        try {
            account = userClient.loginUser(
                    request
            );
        } catch (Exception ignored) {
        }

        if (account == null) {
            try {
                account = driverClient.loginDriver(request);
            } catch (Exception ignored) {
            }
        }

        if (account == null) {
            throw new RuntimeException("Account not found");
        }
        System.out.println("================================");
        System.out.println("Email: " + account.getEmail());
        System.out.println("Raw Password: " + request.getPassword());
        System.out.println("Stored Password: " + account.getPassword());
        System.out.println("================================");

        if (!passwordEncoder.matches(
                request.getPassword(),
                account.getPassword()
        )) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtservice.generateToken(
                account.getEmail(),
                Set.of(account.getRole())
        );

        AuthResponse response = new AuthResponse();
        response.setMessage("Login Successful");
        response.setToken(token);

        return response;
    }
    @Override
    public void logout(String token) {
        System.out.println("Logout Successful");
    }
}