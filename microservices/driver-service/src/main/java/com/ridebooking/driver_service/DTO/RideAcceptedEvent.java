package com.ridebooking.driver_service.DTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideAcceptedEvent {

    private String rideId;
    private String driverId;
}
