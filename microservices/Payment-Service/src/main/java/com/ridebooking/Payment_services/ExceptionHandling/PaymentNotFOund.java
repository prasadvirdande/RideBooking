package com.ridebooking.Payment_services.ExceptionHandling;

public class PaymentNotFOund extends RuntimeException {
    public PaymentNotFOund(String message) {
        super(message);
    }
}
