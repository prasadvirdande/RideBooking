package com.ridebooking.Notification_Service.Service.Kafka;

import com.ridebooking.Notification_Service.DTO.Event.PaymentReceiptEvent;
import com.ridebooking.Notification_Service.Service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentReceiptConsumer {

    private final EmailService emailService;

    @KafkaListener(
            topics = "payment-receipt-topic",
            groupId = "notification-service"
    )
    public void consumePaymentReceipt(PaymentReceiptEvent event) {

        log.info(
                "Payment receipt event received: {}",
                event.getPaymentId()
        );

        if (!"SUCCESS".equals(event.getPaymentStatus())) {
            log.warn("Payment unsuccessful. Receipt will not be sent.");
            return;
        }

        emailService.sendPaymentReceipt(
                event.getEmail(),
                event.getRideId().toString(),
                event.getAmount(),
                event.getPaymentStatus()
        );

        log.info(
                "Payment receipt email sent successfully: {}",
                event.getPaymentId()
        );
    }
}