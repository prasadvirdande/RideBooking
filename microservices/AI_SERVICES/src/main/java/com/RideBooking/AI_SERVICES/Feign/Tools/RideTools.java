
package com.RideBooking.AI_SERVICES.Feign.Tools;

import com.RideBooking.AI_SERVICES.DTO.DriverLocationDTO;
import com.RideBooking.AI_SERVICES.DTO.RideResponseDto;
import com.RideBooking.AI_SERVICES.Feign.RideServiceClient;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RideTools {

    private final RideServiceClient rideServiceClient;

    public RideTools(RideServiceClient rideServiceClient) {
        this.rideServiceClient = rideServiceClient;
    }

    @Tool(description = "Get the user's current active ride")
    public RideResponseDto getActiveRide(UUID userId) {

        return rideServiceClient.getActiveRide(userId);
    }

    @Tool(description = "Get the user's most recently completed ride")
    public RideResponseDto getCompletedRide(UUID userId) {

        return rideServiceClient.getCompletedRides(userId);
    }
    @Tool(description = "Get Driver Location")
    public DriverLocationDTO getDriverLocation(UUID driverId) {

        return rideServiceClient.getDriverLocation(driverId);
    }
    @Tool(description = "Find available drivers near a location")
    public Object getNearbyDrivers(Double latitude, Double longitude) {
        return rideServiceClient.getNearbyDrivers(latitude, longitude);
    }
}

