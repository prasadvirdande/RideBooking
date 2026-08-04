package com.ridebooking.driver_service.Exception;

public class InvalidCredential extends  RuntimeException {
    public InvalidCredential(String message) {
        super(message);
    }
}
