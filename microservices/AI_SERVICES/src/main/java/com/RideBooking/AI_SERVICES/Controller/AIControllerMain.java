package com.RideBooking.AI_SERVICES.Controller;

import com.RideBooking.AI_SERVICES.DTO.AIRideSearchRequest;
import com.RideBooking.AI_SERVICES.DTO.SearchRideRequest;
import com.RideBooking.AI_SERVICES.DTO.SearchRideResponseDTO;
import com.RideBooking.AI_SERVICES.Feign.Tools.RideTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AIControllerMain {

    private final ChatClient chatClient;
    private final RideTools rideTools;

    public AIControllerMain(
            ChatClient.Builder builder,
            RideTools rideTools) {

        this.chatClient = builder.build();
        this.rideTools = rideTools;
    }

    @PostMapping("/ride")
    public String rideChat(
            @RequestBody AIRideSearchRequest message) {

        return chatClient
                .prompt()
                .system("""
                        You are an AI assistant for a ride booking application.

                        You help users with:
                        - Searching for rides
                        - Getting fare estimates
                        - Checking active rides
                        - Checking completed rides
                        - Checking driver locations
                        - Finding nearby drivers

                        When the user wants to search for a ride,
                        use the searchRide tool.

                        The user's message may contain:
                        - pickup latitude
                        - pickup longitude
                        - drop latitude
                        - drop longitude

                        Extract the four coordinate values from the user's
                        message and pass them to the searchRide tool.

                        Never invent coordinates.

                        Never invent fare, distance, travel time,
                        driver IDs, or ride information.

                        IMPORTANT:
                        When the searchRide tool returns nearbyDrivers,
                        ALWAYS include the actual driver ID returned by
                        the tool.

                        Never replace a driver ID with null.
                        Never omit the driver ID.

                        Return the search result as JSON using this structure:

                        {
                            "estimatedDistance": 0.0,
                            "estimatedFare": 0.0,
                            "estimatedTime": 0,
                            "nearbyDrivers": [
                                {
                                    "id": "driver-uuid"
                                }
                            ],
                            "message": "..."
                        }

                        Use the exact values returned by the tool.

                        All fares are in Indian Rupees (INR).

                        Searching for a ride and booking a ride are different
                        operations. Do not book a ride unless the user
                        explicitly confirms that they want to book it.
                        """)
                .user(message.getMessage())
                .tools(rideTools)
                .call()
                .content();
    }

    @PostMapping("/search-ride")
    public ResponseEntity<SearchRideResponseDTO> searchRide(
            @RequestBody SearchRideRequest request) {

        System.out.println("========== SEARCH REQUEST ==========");
        System.out.println("pickupLatitude  = " + request.getPickupLatitude());
        System.out.println("pickupLongitude = " + request.getPickupLongitude());
        System.out.println("dropLatitude    = " + request.getDropLatitude());
        System.out.println("dropLongitude   = " + request.getDropLongitude());
        System.out.println("===================================");

        return ResponseEntity.ok(
                rideTools.searchRide(
                        request.getPickupLatitude(),
                        request.getPickupLongitude(),
                        request.getDropLatitude(),
                        request.getDropLongitude()
                )
        );
    }
}