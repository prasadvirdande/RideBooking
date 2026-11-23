package com.ridebooking.Payment_services.Service.Kafka;

import com.ridebooking.Payment_services.DTO.Event.PaymentReceiptEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationProducer {

    private static final String TOPIC = "payment-receipt-topic";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishPaymentReceipt(PaymentReceiptEvent event) {

        kafkaTemplate.send(
                TOPIC,
                event.getPaymentId().toString(),
                event
        ).whenComplete((result, exception) -> {

            if (exception != null) {
                log.error("Failed to publish payment receipt event", exception);
                return;
            }

            log.info(
                    "Payment receipt event published | Payment ID: {} | Offset: {}",
                    event.getPaymentId(),
                    result.getRecordMetadata().offset()
            );
        });
    }
}
