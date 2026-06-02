package com.ridebooking.user_service.Service;

import com.ridebooking.user_service.DTO.*;
import com.ridebooking.user_service.Entity.Roles;
import com.ridebooking.user_service.Entity.User;

import com.ridebooking.user_service.Feign.DriverClient;
import com.ridebooking.user_service.Repository.UserRepo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class ServiceImpl implements UserService {

    private final UserRepo userRepo;
    private final DriverClient driverClient;
    @Override
    public UserResponseDto getProfile() {

        User user = userRepo.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException("No users found"));

        return mapToDto(user);
    }

    @Override
    public UserResponseDto getUserById(UUID userId) {

        User user = userRepo.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found!"));

        return mapToDto(user);
    }

    @Override
    public List<UserResponseDto> getAllUsers() {

        return userRepo.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public UserResponseDto updateProfile(UpdateUserRequestDto request) {

        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("User not found!"));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhone());
        user.setEmail(request.getEmail());

        User updatedUser = userRepo.save(user);

        log.info("User updated successfully: {}", updatedUser.getEmail());

        return mapToDto(updatedUser);
    }

    @Override
    public void deleteUser(UUID userId) {

        User user = userRepo.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found!"));

        userRepo.delete(user);

        log.info("User deleted successfully: {}", userId);
    }

    @Override
    public List<UserResponseDto> searchUsers(String keyword) {

        return userRepo
                .findByFirstNameContainingIgnoreCase(keyword)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public UserResponseDto getByEmail(String email) {
        User user=userRepo.findByEmail(email).orElseThrow(
                () -> new RuntimeException("User not found!")
        );
        return mapToDto(user);
    }

    private UserResponseDto mapToDto(User user) {

        return UserResponseDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .password(user.getPassword())
                .phone(user.getPhoneNumber())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Override
    public UserResponseDto registerUser(
            CreateUserRequestDto request
    ) {

        User user = new User();

        user.setFirstName(
                request.getFirstName()
        );

        user.setLastName(
                request.getLastName()
        );

        user.setEmail(
                request.getEmail()
        );

        user.setPassword(
                request.getPassword()
        );

        user.setPhoneNumber(
                request.getPhoneNumber()
        );

        user.setRole(Roles.RIDER);

        user.setProfilePicture(
                request.getProfilePicture()
        );

        user.setDefaultAddress(
                request.getDefaultAddress()
        );

        User savedUser =
                userRepo.save(user);

        return mapToDto(savedUser);
    }

    @Override
    public Object searchRide(SearchRideDTO searchRideDTO) {
        return driverClient.getNearbyDrivers(
                searchRideDTO.getPickupLatitude(),
                searchRideDTO.getPickupLongitude()
        );
    }

    @Override
    public UserLoginResponseDTO loginUser(UserLoginRequestDTO userLoginDTO) {
        User user = userRepo.findByEmail(userLoginDTO.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("User not found!"));

        return new UserLoginResponseDTO(
               user.getEmail(),
                user.getPassword(),
                user.getRole().name()
        );
    }
}