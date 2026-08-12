package com.ridebooking.ride_service.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class Global {

    @ExceptionHandler(RideNotFOundException.class)
    public ResponseEntity<Map<String, Object>> handleRideNotFoundException(RideNotFOundException ex) {
        return handleException(ex, HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(RIdeNotAccepted.class)
    public ResponseEntity<Map<String, Object>> handleRideNotAcceptedException(RIdeNotAccepted ex) {
        return handleException(ex, HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(UserOrRIdeNotFOund.class)
    public ResponseEntity<Map<String, Object>> handleUserOrRideNotFoundException(UserOrRIdeNotFOund ex) {
        return handleException(ex, HttpStatus.NOT_FOUND, ex.getMessage());
    }

    public ResponseEntity<Map<String, Object>> handleException(Exception ex,
                                                               HttpStatus status,
                                                               String message) {

        Map<String, Object> body = new HashMap<>();
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("timestamp", LocalDateTime.now());

        return ResponseEntity.status(status).body(body);
    }
}