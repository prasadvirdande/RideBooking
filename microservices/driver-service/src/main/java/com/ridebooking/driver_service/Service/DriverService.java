package com.ridebooking.driver_service.Service;

import com.ridebooking.driver_service.DTO.*;
import org.springframework.data.geo.GeoResults;

import java.util.List;

public interface DriverService {
    List<DriverResponseDto> getAllDrivers();

    DriverResponseDto createDriver(DriverRequestDto driverRequestDto);

    DriverLocationResponseDto updateDriverLocation(DriverLocationUpdateDto driverLocationUpdateDto);

    GeoResults<?> findNearbyDrivers(
            Double latitude,
            Double longitude);


    DriverLoginResponseDTO loginDriver(DriverLoginRequestDto driverRequestDto);

    DriverResponseDto getDriverById(String driverId);

    void acceptRide(AcceptRideRequest driverId);
}
