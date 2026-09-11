package com.ridebooking.Payment_services.Controller;

import com.ridebooking.Payment_services.DTO.PaymentRequestDTO;
import com.ridebooking.Payment_services.DTO.PaymentResponseDTO;
import com.ridebooking.Payment_services.Service.PaymentServiceINter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentServiceINter paymentServiceINter;
    public PaymentController(
            PaymentServiceINter paymentServiceINter1) {
        this.paymentServiceINter = paymentServiceINter1;
    }

    @GetMapping("/ride/{rideId}")
    public ResponseEntity<PaymentResponseDTO> getPaymentByRideId(
            @PathVariable UUID rideId) {

        return ResponseEntity.ok(
                paymentServiceINter.getPaymentByRideId(rideId)
        );
    }

    @PostMapping("/create")
    public ResponseEntity<PaymentResponseDTO> createPayment(
            @RequestBody PaymentRequestDTO request,  @RequestHeader("Idempotency-Key") String idempotencyKey ) {

        return ResponseEntity.ok(paymentServiceINter.createPayment(request,idempotencyKey));
    }
    @PostMapping("/webhook")
    public ResponseEntity<String> razorpayWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature) {
        System.out.println("PAYMENT SUCCESS API HIT");
        paymentServiceINter.handleWebhook(payload, signature);

        return ResponseEntity.ok("Webhook received");
    }

}
