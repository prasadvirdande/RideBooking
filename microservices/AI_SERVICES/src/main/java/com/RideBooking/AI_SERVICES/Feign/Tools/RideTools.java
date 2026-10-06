package com.RideBooking.AI_SERVICES.Feign.Tools;

import com.RideBooking.AI_SERVICES.DTO.DriverLocationDTO;
import com.RideBooking.AI_SERVICES.DTO.RideResponseDto;
import com.RideBooking.AI_SERVICES.DTO.SearchRideRequest;
import com.RideBooking.AI_SERVICES.DTO.SearchRideResponseDTO;
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

    @Tool(description = "Get the current location of a driver")
    public DriverLocationDTO getDriverLocation(UUID driverId) {

        return rideServiceClient.getDriverLocation(driverId);
    }

    @Tool(description = "Find available drivers near a location")
    public Object getNearbyDrivers(
            Double latitude,
            Double longitude) {

        return rideServiceClient.getNearbyDrivers(
                latitude,
                longitude
        );
    }

    @Tool(description = """
    Search for a ride using the pickup and drop coordinates.
    Returns actual distance, fare, estimated time and nearby drivers.
    """)
    public SearchRideResponseDTO searchRide(
            Double pickupLatitude,
            Double pickupLongitude,
            Double dropLatitude,
            Double dropLongitude) {

        System.out.println(" SEARCH RIDE TOOL CALLED");

        System.out.println("Pickup Lat = " + pickupLatitude);
        System.out.println("Pickup Lon = " + pickupLongitude);
        System.out.println("Drop Lat   = " + dropLatitude);
        System.out.println("Drop Lon   = " + dropLongitude);

        SearchRideRequest request = new SearchRideRequest();

        request.setPickupLatitude(pickupLatitude);
        request.setPickupLongitude(pickupLongitude);
        request.setDropLatitude(dropLatitude);
        request.setDropLongitude(dropLongitude);

        return rideServiceClient.searchRide(request);
    }

}