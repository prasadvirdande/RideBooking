package com.ridebooking.user_service.Service;

import com.ridebooking.user_service.DTO.*;
import com.ridebooking.user_service.Entity.Roles;
import com.ridebooking.user_service.Entity.User;

import com.ridebooking.user_service.Feign.DriverClient;
import com.ridebooking.user_service.Repository.UserRepo;
import com.ridebooking.user_service.sharding.ShardContext;
import com.ridebooking.user_service.sharding.ShardResolver;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.x509.Time;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
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


        long start = System.currentTimeMillis();
        System.out.println("Start time: " + start);
        long end = System.currentTimeMillis();
        System.out.println("End time: " + end);
        User updatedUser = userRepo.save(user);

        long duration = end - start;
        System.out.println("Duration: " + duration + "ms");

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

        String shard = ShardResolver.getShard(user.getEmail());

        ShardContext.setShard(shard);


        User savedUser =
                userRepo.save(user);

        ShardContext.clear();

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

        long totalStart = System.nanoTime();

        // Repository timing
        long repoStart = System.nanoTime();

        String shard = ShardResolver.getShard(userLoginDTO.getEmail());

        ShardContext.setShard(shard);

        Optional<User> optionalUser =
                userRepo.findByEmail(userLoginDTO.getEmail());

        ShardContext.clear();

        long repoEnd = System.nanoTime();

        System.out.println("Repository = "
                + ((repoEnd - repoStart) / 1_000_000.0)
                + " ms");

        // orElseThrow timing
        long orElseStart = System.nanoTime();

        User user = optionalUser.orElseThrow(
                () -> new RuntimeException("User not found!")
        );

        long orElseEnd = System.nanoTime();

        System.out.println("orElseThrow = "
                + ((orElseEnd - orElseStart) / 1_000_000.0)
                + " ms");

        // DTO creation timing
        long dtoStart = System.nanoTime();

        UserLoginResponseDTO response = new UserLoginResponseDTO(
                user.getId().toString(),
                user.getEmail(),
                user.getPassword(),
                user.getRole().name()
        );

        long dtoEnd = System.nanoTime();

        System.out.println("DTO Creation = "
                + ((dtoEnd - dtoStart) / 1_000_000.0)
                + " ms");

        System.out.println("TOTAL SERVICE = "
                + ((dtoEnd - totalStart) / 1_000_000.0)
                + " ms");

        return response;
    }
}