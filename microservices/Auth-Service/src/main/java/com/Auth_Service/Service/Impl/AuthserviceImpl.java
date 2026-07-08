
package com.Auth_Service.Service.Impl;

import com.Auth_Service.DTO.*;
import com.Auth_Service.Feign.DriverClient;
import com.Auth_Service.Feign.UserClient;
import com.Auth_Service.Security.AuthUserDetailsService;
import com.Auth_Service.Security.Jwtservice;
import com.Auth_Service.Service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthserviceImpl implements AuthService {

    private final UserClient userClient;
    private final DriverClient driverClient;
    private final PasswordEncoder passwordEncoder;
    private final Jwtservice jwtservice;
    private final AuthenticationManager authenticationManager;
    private final AuthUserDetailsService authUserDetailsService;
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
    public AuthResponse loginDriver(LoginRequest loginRequest) {
        UserDto driver = driverClient.loginDriver(loginRequest);
        boolean valid=passwordEncoder.matches(loginRequest.getPassword(),driver.getPassword());
        if(!valid)throw new RuntimeException("Invalid credentials For Driver");
        AuthResponse response = new AuthResponse();
        response.setMessage("Login Successful");
        response.setToken(jwtservice.generateToken(driver.getEmail(), Set.of(driver.getRole())));
        return response;
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        long start = System.currentTimeMillis();

        long t1 = System.currentTimeMillis();
        UserDto user = userClient.loginUser(request);
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
            throw new RuntimeException("Invalid credentials");
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
//    @Override
//    public AuthResponse login(LoginRequest request) {
//
//        UserDto account = null;
//
//
//        try {
//            account = userClient.loginUser(
//                    request
//            );
//        } catch (Exception ignored) {
//        }
//
//        if (account == null) {
//            try {
//                account = driverClient.loginDriver(request);
//            } catch (Exception ignored) {
//            }
//        }
//
//        if (account == null) {
//            throw new RuntimeException("Account not found");
//        }
//        System.out.println("================================");
//        System.out.println("Email: " + account.getEmail());
//        System.out.println("Raw Password: " + request.getPassword());
//        System.out.println("Stored Password: " + account.getPassword());
//        System.out.println("================================");
//
//        if (!passwordEncoder.matches(
//                request.getPassword(),
//                account.getPassword()
//        )) {
//            throw new RuntimeException("Invalid credentials");
//        }
//
//        String token = jwtservice.generateToken(
//                account.getEmail(),
//                Set.of(account.getRole())
//        );
//
//        AuthResponse response = new AuthResponse();
//        response.setMessage("Login Successful");
//        response.setToken(token);
//
//        return response;
//    }

//    @Override
//    public AuthResponse login(LoginRequest request) {
//
//        System.out.println("LOGIN SERVICE HIT");
//
//        authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(
//                        request.getEmail(),
//                        request.getPassword()
//                )
//        );
//
//        UserDetails userDetails =
//                authUserDetailsService.loadUserByUsername(
//                        request.getEmail()
//                );
//
//        String token = jwtservice.generateToken(
//                userDetails.getUsername(),
//                userDetails.getAuthorities()
//                        .stream()
//                        .map(GrantedAuthority::getAuthority)
//                        .collect(Collectors.toSet())
//        );
//
//        AuthResponse response = new AuthResponse();
//        response.setMessage("Login Successful");
//        response.setToken(token);
//
//        return response;
//    }
    @Override
    public void logout(String token) {
        System.out.println("Logout Successful");
    }
}