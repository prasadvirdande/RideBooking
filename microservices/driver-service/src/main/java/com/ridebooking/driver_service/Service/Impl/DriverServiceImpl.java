package com.ridebooking.driver_service.Service.Impl;

import com.ridebooking.driver_service.DTO.*;
import com.ridebooking.driver_service.Entity.Driver;
import com.ridebooking.driver_service.Enum.DriverStatus;
import com.ridebooking.driver_service.Enum.Role;
import com.ridebooking.driver_service.Repository.Driverepo;
import com.ridebooking.driver_service.Service.DriverService;
import lombok.AllArgsConstructor;

import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service
@AllArgsConstructor
public class DriverServiceImpl implements DriverService {

    private final Driverepo driverepo;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public List<DriverResponseDto> getAllDrivers() {
        return driverepo.findAll().stream().map(this::mapToDriverResponseDto).toList();
    }

    @Override
    public DriverResponseDto createDriver(DriverRequestDto driverRequestDto) {
        if (driverepo.findByEmail(driverRequestDto.getEmail()).isPresent()) {
            throw new RuntimeException("Driver already exists");
        }
        Driver driver = new Driver();
        driver.setDriverName(driverRequestDto.getDriverName());
        driver.setLicenseNumber(driverRequestDto.getLicenseNumber());
        driver.setLicenseExpiry(driverRequestDto.getLicenseExpiry());
        driver.setVehicleNumber(driverRequestDto.getVehicleNumber());
        driver.setStatus(driverRequestDto.getStatus());
        driver.setPassword(driverRequestDto.getPassword());
        driver.setRole(Role.DRIVER);
        driver.setEmail(driverRequestDto.getEmail());
        driver.setOnline(driverRequestDto.getOnline());
        driver.setRating(driverRequestDto.getRating());
        driver.setTotalTrips(driverRequestDto.getTotalTrips());
        Driver savedDriver = driverepo.save(driver);
        return mapToDriverResponseDto(savedDriver);
    }

    private DriverResponseDto mapToDriverResponseDto(Driver driver) {
        return DriverResponseDto.builder().
                id(driver.getId()).
                email(driver.getEmail()).
                role(driver.getRole().name()).
                DriverName(driver.getDriverName()).
                VehicleNumber(driver.getVehicleNumber()).
                totalTrips(driver.getTotalTrips()).
                rating(driver.getRating()).
                build();
    }

    @Override
    public DriverLocationResponseDto updateDriverLocation(DriverLocationUpdateDto driverLocationUpdateDto) {
        Driver driver = driverepo.findById(driverLocationUpdateDto.getDriverId()).orElseThrow(() -> new RuntimeException("Driver not found"));
        System.out.println(
                "Updating location for driver ID = "
                        + driverLocationUpdateDto.getDriverId()
        );
        System.out.println(
                "Driver fetched from DB = "
                        + driver.getId()
        );
        redisTemplate.opsForGeo().add(
                "driver-location",
                new Point(driverLocationUpdateDto.getLongitude(), driverLocationUpdateDto.getLatitude()),
                driverLocationUpdateDto.getDriverId().toString()
        );
        driver.setOnline(true);
        driverepo.save(driver);
        return DriverLocationResponseDto.builder()
                .driverId(driverLocationUpdateDto.getDriverId())
                .latitude(driverLocationUpdateDto.getLatitude())
                .longitude(driverLocationUpdateDto.getLongitude())
                .message("Driver location updated successfully")
                .build();
    }

    @Override
    public GeoResults<RedisGeoCommands.GeoLocation<Object>>
    findNearbyDrivers(
            Double latitude,
            Double longitude) {

        return redisTemplate.opsForGeo().radius(
                "driver-location",
                new Circle(
                        new Point(longitude, latitude),
                        new Distance(5, Metrics.KILOMETERS)
                ),
                RedisGeoCommands.GeoRadiusCommandArgs
                        .newGeoRadiusArgs()
                        .includeCoordinates()
                        .includeDistance()
        );
    }

    @Override
    public DriverLoginResponseDTO loginDriver(DriverLoginRequestDto driverRequestDto) {
        Driver driver = driverepo.findByEmail(
                driverRequestDto.getEmail()
        ).orElseThrow(() ->
                new RuntimeException("Driver not found"));

        return new DriverLoginResponseDTO(
                driver.getEmail(),
                driver.getPassword(),
                driver.getRole().name()
        );
    }

    @Override
    public DriverResponseDto getDriverById(String driverId) {
        Driver driver = driverepo.findById(UUID.fromString(driverId)).orElseThrow(() -> new RuntimeException("Driver not found"));
        return mapToDriverResponseDto(driver);
    }

    @Override
    public void acceptRide(AcceptRideRequest driverId) {

        System.out.println("Driver ID received: " + driverId);

        Driver driver = driverepo.findById(
                UUID.fromString(driverId.getDriverId())
        ).orElseThrow(() -> new RuntimeException("Driver not found"));


        driver.setStatus(DriverStatus.BUSY);
        driverepo.save(driver);
    }


}
