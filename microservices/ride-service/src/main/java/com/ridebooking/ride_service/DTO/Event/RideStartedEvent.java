package com.ridebooking.ride_service.DTO.Event;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class RideStartedEvent {

    private String rideId;
    private String driverId;
}
