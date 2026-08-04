package com.Auth_Service.ExceptionHandling;

public class UnauthorizedAccessException extends RuntimeException{

    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
