package com.RideBooking.AI_SERVICES.Controller;

import com.RideBooking.AI_SERVICES.Feign.Tools.RideTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/ai")
public class AIControllerMain {

    private final ChatClient chatClient;
    private final RideTools rideTools;

    public AIControllerMain(ChatClient.Builder builder, RideTools rideTools) {
        this.chatClient = builder.build();
        this.rideTools = rideTools;
    }

    @PostMapping("/ride")
    public String rideChat(
            @RequestParam UUID userId,
            @RequestBody String message) {

        return chatClient
                .prompt()
                .system("""
                        You are an AI assistant for a ride booking application.
                        You help users with questions about their rides,
                        drivers, fares, payments and ride status.
                        All fares are in Indian Rupees (INR).
                        Always display fares using the ₹ symbol.

                        Be concise and helpful.
                        """)
                .user("""
                        User ID: %s

                        User question:
                        %s
                        """.formatted(userId, message))
                .tools(rideTools)
                .call()
                .content();
    }
}


