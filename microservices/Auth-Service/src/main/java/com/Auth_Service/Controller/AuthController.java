package com.Auth_Service.Controller;

import com.Auth_Service.DTO.AuthRequest;
import com.Auth_Service.DTO.AuthResponse;
import com.Auth_Service.DTO.DriverRequest;
import com.Auth_Service.DTO.LoginRequest;
import com.Auth_Service.Service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerUser(
            @RequestBody AuthRequest authRequest) {

        AuthResponse response = authService.register(authRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/driver/register")
    public ResponseEntity<AuthResponse> registerUser(
            @RequestBody DriverRequest authRequest) {

        AuthResponse response = authService.Driverregister(authRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(
            @RequestBody LoginRequest authRequest) {

        AuthResponse response = authService.login(authRequest);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logoutUser(HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("JWT Token is missing");
        }

        String token = authHeader.substring(7);

        authService.logout(token);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body("Logout successful");
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {

        return ResponseEntity.ok("Auth Service is running");
    }
}