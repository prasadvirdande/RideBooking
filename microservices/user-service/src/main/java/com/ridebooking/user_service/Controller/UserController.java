package com.ridebooking.user_service.Controller;// UserController.java




import com.ridebooking.user_service.DTO.*;
import com.ridebooking.user_service.Service.UserService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserResponseDto> getProfile() {

        UserResponseDto response =
                userService.getProfile();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{email}")
    public ResponseEntity<UserResponseDto> getByEmail(@PathVariable("email") String email) {
        UserResponseDto responseDto=userService.getByEmail(email);

        return ResponseEntity.ok(responseDto);
    }
    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> registerUser(
            @RequestBody CreateUserRequestDto request
    ) {

        UserResponseDto response =
                userService.registerUser(request);

        return ResponseEntity.ok(response);
    }


    @GetMapping("/id/{userId}")
    public ResponseEntity<UserResponseDto> getUserById(
            @PathVariable UUID userId
    ) {

        UserResponseDto response =
                userService.getUserById(userId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<UserLoginResponseDTO> loginUser(@RequestBody UserLoginRequestDTO userLoginDTO){
        UserLoginResponseDTO response = userService.loginUser(userLoginDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {

        List<UserResponseDto> response =
                userService.getAllUsers();

        return ResponseEntity.ok(response);
    }


    @PutMapping("/profile")
    public ResponseEntity<UserResponseDto> updateProfile(
            @RequestBody UpdateUserRequestDto request
    ) {

        UserResponseDto response =
                userService.updateProfile(request);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteUser(
            @PathVariable UUID userId
    ) {

        userService.deleteUser(userId);

        return ResponseEntity.ok(
                "User deleted successfully"
        );
    }


    @GetMapping("/search")
    public ResponseEntity<List<UserResponseDto>> searchUsers(
            @RequestParam String keyword
    ) {

        List<UserResponseDto> response =
                userService.searchUsers(keyword);

        return ResponseEntity.ok(response);
    }
    @GetMapping("/search/ride")
    ResponseEntity<?> searchRide(@RequestBody SearchRideDTO searchRideDTO) {
        return ResponseEntity.ok(userService.searchRide(searchRideDTO));
    }

}