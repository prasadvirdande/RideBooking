package com.ridebooking.driver_service.Controller;

import com.ridebooking.driver_service.DTO.*;
import com.ridebooking.driver_service.Service.DriverService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/api/driver")
public class DriverController {

    private final DriverService driverService;

    @GetMapping
    public ResponseEntity<List<DriverResponseDto>> getAllDrivers() {
        return ResponseEntity.ok(
                driverService.getAllDrivers()
        );
    }

    @PostMapping("/register")
    public ResponseEntity<DriverResponseDto> createDriver(
            @RequestBody DriverRequestDto driverRequestDto) {

        return ResponseEntity.ok(
                driverService.createDriver(driverRequestDto)
        );
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<DriverResponseDto> getDriverByEmail(
            @PathVariable String email) {

        return ResponseEntity.ok(
                driverService.getDriverByEmail(email)
        );
    }

    @PostMapping("/login")
    public ResponseEntity<DriverLoginResponseDTO> loginDriver(
            @RequestBody DriverLoginRequestDto driverRequestDto) {

        return ResponseEntity.ok(
                driverService.loginDriver(driverRequestDto)
        );
    }

    @PutMapping("/location")
    public ResponseEntity<DriverLocationResponseDto> updateDriverLocation(
            @RequestBody DriverLocationUpdateDto driverLocationUpdateDto) {

        return ResponseEntity.ok(
                driverService.updateDriverLocation(
                        driverLocationUpdateDto
                )
        );
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<DriverResponseDto> getDriverById(
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                driverService.findById(id)
        );
    }

    @GetMapping("/nearby")
    public ResponseEntity<?> getNearbyDrivers(
            @RequestParam("latitude") Double latitude,
            @RequestParam("longitude") Double longitude) {

        return ResponseEntity.ok(
                driverService.findNearbyDrivers(
                        latitude,
                        longitude
                )
        );
    }

    @PostMapping("/accept/ride")
    public ResponseEntity<String> acceptRide(
            @RequestBody AcceptRideRequest driverId) {

        driverService.acceptRide(driverId);

        return ResponseEntity.ok("Driver is Busy");
    }
}