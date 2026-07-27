package com.ridebooking.Notification_Service.Controller;

import com.ridebooking.Notification_Service.DTO.PaymentReceiptRequest;
import com.ridebooking.Notification_Service.Service.EmailService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/payment-receipt")
    public ResponseEntity<String> sendPaymentReceipt(
         @RequestBody PaymentReceiptRequest request) {

        emailService.sendPaymentReceipt(
                request.getEmail(),
                request.getRideId(),
                request.getAmount(),
                request.getPaymentId()
        );

        return ResponseEntity.ok("Payment receipt sent successfully.");
    }
}
