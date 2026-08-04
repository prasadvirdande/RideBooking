package com.ridebooking.driver_service.Exception;

public class DriverNotFound  extends  RuntimeException{

    public DriverNotFound(String message) {
        super(message);
    }
}
