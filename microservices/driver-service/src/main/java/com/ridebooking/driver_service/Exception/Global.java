package com.ridebooking.driver_service.Exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class Global {

    @ExceptionHandler(DriverNotFound.class)
    public ResponseEntity<String> handleDriverNotFound(DriverNotFound ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidCredential.class)
   public ResponseEntity<String> handleInvalidCredential(InvalidCredential ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }

    public ResponseEntity<Map<String,Object>> handleException(Exception ex, HttpStatus status, String message){
        Map<String,Object> map = new HashMap<>();
        map.put("status", status.value());
        map.put("error", message);
        map.put("message", ex.getMessage());
        map.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(status).body(map);
    }




}
