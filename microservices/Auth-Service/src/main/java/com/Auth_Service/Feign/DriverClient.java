package com.Auth_Service.Feign;

import com.Auth_Service.DTO.DriverRequest;
import com.Auth_Service.DTO.LoginRequest;
import com.Auth_Service.DTO.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "driver-service", url = "http://localhost:8083")
public interface DriverClient {
    @PostMapping("/api/driver/register")
    UserDto createDriver(@RequestBody DriverRequest driverRequest);

    @PostMapping("/api/driver/login")
    UserDto loginDriver(
            @RequestBody LoginRequest request
    );


}
