package com.ridebooking.Payment_services.FeignCLient;

import com.ridebooking.Payment_services.DTO.PaymentReceiptRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "Notification-Service", url = "http://localhost:9091")
public interface NotificationFeign {


        @PostMapping("/api/email/payment-receipt")
        ResponseEntity<String> sendPaymentReceipt(
                @RequestBody PaymentReceiptRequest request
        );
    }


