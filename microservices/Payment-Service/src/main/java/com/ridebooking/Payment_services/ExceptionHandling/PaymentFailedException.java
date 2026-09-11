package com.ridebooking.Payment_services.ExceptionHandling;


public class PaymentFailedException extends RuntimeException {
    public PaymentFailedException(String message) {
        super(message);
    }
}

