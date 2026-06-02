package com.ridebooking.user_service.DTO;

import lombok.Data;

@Data
public class SearchRideDTO {
    private String pickupLatitude;
    private String pickupLongitude;
    private String destinationLatitude;
    private String destinationLongitude;
}
