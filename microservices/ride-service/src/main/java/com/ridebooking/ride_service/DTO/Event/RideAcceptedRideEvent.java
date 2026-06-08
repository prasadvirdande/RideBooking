package com.ridebooking.ride_service.DTO.Event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideAcceptedRideEvent {

    private String rideId;
    private String driverId;
}
