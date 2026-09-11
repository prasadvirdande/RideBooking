package com.ridebooking.Payment_services.ExceptionHandling;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GLobalLevel {

    @ExceptionHandler(PaymentFailedException.class)
    public ResponseEntity<String> paymentFailed(PaymentFailedException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
    @ExceptionHandler(PaymentNotFOund.class)
    public ResponseEntity<String> paymentNotFound(PaymentNotFOund ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    public  ResponseEntity<Map<String, Object>> handleException(Exception ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }


}
