
package com.Auth_Service.Service.Impl;

import com.Auth_Service.DTO.*;
import com.Auth_Service.ExceptionHandling.InvalidCredentialsException;
import com.Auth_Service.ExceptionHandling.Usernotfound;
import com.Auth_Service.Feign.DriverClient;
import com.Auth_Service.Feign.UserClient;

import com.Auth_Service.Security.Jwtservice;
import com.Auth_Service.Service.AuthService;
import lombok.RequiredArgsConstructor;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor
public class AuthserviceImpl implements AuthService {

    private final UserClient userClient;
    private final DriverClient driverClient;
    private final PasswordEncoder passwordEncoder;
    private final Jwtservice jwtservice;
    private final StringRedisTemplate redisTemplate;

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
            throw new Usernotfound("Driver registration failed");
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
    public AuthResponse loginDriver(LoginRequest loginRequest) {
        UserDto driver = driverClient.loginDriver(loginRequest);
        boolean valid=passwordEncoder.matches(loginRequest.getPassword(),driver.getPassword());
        if(!valid)throw new InvalidCredentialsException("Invalid credentials For Driver");
        AuthResponse response = new AuthResponse();
        response.setId(driver.getId());
        response.setMessage("Login Successful");
        response.setToken(jwtservice.generateToken(driver.getEmail(), Set.of(driver.getRole())));
        return response;
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        long start = System.currentTimeMillis();

        long t1 = System.currentTimeMillis();
        System.out.println("Before Feign = " + System.currentTimeMillis());

        UserDto user = userClient.loginUser(request);

        System.out.println("After Feign = " + System.currentTimeMillis());
        System.out.println("User Service Call = "
                + (System.currentTimeMillis() - t1) + " ms");

        long t2 = System.currentTimeMillis();
        boolean valid = passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        );
        System.out.println("BCrypt Match = "
                + (System.currentTimeMillis() - t2) + " ms");

        if (!valid) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        long t3 = System.currentTimeMillis();
        String token = jwtservice.generateToken(
                user.getEmail(),
                Set.of(user.getRole())
        );
        System.out.println("JWT Generation = "
                + (System.currentTimeMillis() - t3) + " ms");

        System.out.println("TOTAL = "
                + (System.currentTimeMillis() - start) + " ms");

        AuthResponse response = new AuthResponse();
        response.setMessage("Login Successful");
        response.setId(user.getId());
        response.setToken(token);

        return response;
    }

    @Override
    public void logout(String token) {

        long expirationTime =
                jwtservice.getExpirationTime(token);

        long remainingTime =
                expirationTime - System.currentTimeMillis();

        if (remainingTime > 0) {

            redisTemplate.opsForValue().set(
                    "blacklist:" + token,
                    "true",
                    remainingTime,
                    TimeUnit.MILLISECONDS
            );
        }

        System.out.println("Logout Successful");
    }
}