package com.Auth_Service.Feign;

import com.Auth_Service.DTO.AuthRequest;
import com.Auth_Service.DTO.LoginRequest;
import com.Auth_Service.DTO.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "USER-SERVICE",
        url = "http://localhost:8082"
)
public interface UserClient {

    @PostMapping("/api/users/register")
    UserDto createUser(
            @RequestBody AuthRequest request
    );

    @PostMapping("/api/users/login")
    UserDto loginUser(
            @RequestBody LoginRequest request
    );

    @GetMapping("/api/users/email/{email}")
    UserDto getByEmail(
            @PathVariable("email") String email
    );
}