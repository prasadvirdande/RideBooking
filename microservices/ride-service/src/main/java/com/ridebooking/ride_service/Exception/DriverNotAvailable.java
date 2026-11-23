package com.ridebooking.ride_service.Exception;

public class DriverNotAvailable extends RuntimeException {
    public DriverNotAvailable(String message) {
        super(message);
    }
}
