package com.ridebooking.Payment_services.FeignCLient;


import com.ridebooking.Payment_services.DTO.UserResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "User-Service", url = "http://localhost:8082")
public interface Feign {

    @GetMapping("/api/users/id/{userId}")
    UserResponseDto getUserById(@PathVariable("userId") UUID userId);
}
