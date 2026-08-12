package com.ridebooking.ride_service.Feign;

import com.ridebooking.ride_service.DTO.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserFeign {
  @GetMapping("/api/users/id/{userId}")
    UserDTO getUserById(@PathVariable String userId);
}
