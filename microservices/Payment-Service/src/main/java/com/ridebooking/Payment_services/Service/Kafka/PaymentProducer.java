package com.ridebooking.Payment_services.Service.Kafka;

import com.ridebooking.Payment_services.DTO.Event.PaymentCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentProducer {

    private static final String TOPIC = "payment-completed-topic";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishPaymentCompleted(PaymentCompletedEvent event) {

        kafkaTemplate.send(
                TOPIC,
                event.getRideId().toString(),
                event
        ).whenComplete((result, exception) -> {

            if (exception != null) {
                log.error("Failed to publish payment event", exception);
                return;
            }

            log.info(
                    "Payment event published. Ride ID: {}, Partition: {}, Offset: {}",
                    event.getRideId(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset()
            );
        });
    }
}
