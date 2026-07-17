package com.ridebooking.driver_service.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideStartedEvent {

    private  String rideId;
    private String driverId;
}
