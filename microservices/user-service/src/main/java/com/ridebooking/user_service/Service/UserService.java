package com.ridebooking.user_service.Service;

import com.ridebooking.user_service.DTO.*;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserResponseDto getProfile();

    UserResponseDto getUserById(UUID userId);

    List<UserResponseDto> getAllUsers();

    UserResponseDto updateProfile(UpdateUserRequestDto request);

    void deleteUser(UUID userId);

    List<UserResponseDto> searchUsers(String keyword);

    UserResponseDto getByEmail(String email);

    UserResponseDto registerUser(CreateUserRequestDto request);

    Object searchRide(SearchRideDTO searchRideDTO);
    UserLoginResponseDTO loginUser(UserLoginRequestDTO userLoginDTO);
}
