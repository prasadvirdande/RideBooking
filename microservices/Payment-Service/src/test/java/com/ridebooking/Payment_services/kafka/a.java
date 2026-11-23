package com.ridebooking.Payment_services.kafka;

import com.ridebooking.Payment_services.DTO.Event.PaymentReceiptEvent;

import com.ridebooking.Payment_services.Service.Kafka.NotificationProducer;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import org.apache.kafka.common.serialization.StringDeserializer;

import org.junit.jupiter.api.Test;

import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class NotificationProducerTest {

    private static final String TOPIC = "payment-receipt-topic";

    @Container
    static final KafkaContainer kafka =
            new KafkaContainer(
                    DockerImageName.parse("confluentinc/cp-kafka:7.6.1")
            );

    @Test
    void shouldPublishPaymentReceiptEventToKafka() throws Exception {

        // 1. Configure Kafka producer

        Map<String, Object> producerProperties = new HashMap<>();

        producerProperties.put(
                "bootstrap.servers",
                kafka.getBootstrapServers()
        );

        producerProperties.put(
                "key.serializer",
                org.apache.kafka.common.serialization.StringSerializer.class
        );

        producerProperties.put(
                "value.serializer",
                JsonSerializer.class
        );

        DefaultKafkaProducerFactory<String, Object> producerFactory =
                new DefaultKafkaProducerFactory<>(producerProperties);

        KafkaTemplate<String, Object> kafkaTemplate =
                new KafkaTemplate<>(producerFactory);

        // 2. Create actual NotificationProducer

        NotificationProducer notificationProducer =
                new NotificationProducer(kafkaTemplate);

        // 3. Configure Kafka consumer for verification

        Map<String, Object> consumerProperties = new HashMap<>();

        consumerProperties.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                kafka.getBootstrapServers()
        );

        consumerProperties.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "payment-producer-test"
        );

        consumerProperties.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest"
        );

        consumerProperties.put(
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
                false
        );

        JsonDeserializer<PaymentReceiptEvent> jsonDeserializer =
                new JsonDeserializer<>(PaymentReceiptEvent.class, false);

        jsonDeserializer.addTrustedPackages(
                "com.ridebooking.Payment_services.DTO.Event"
        );

        Consumer<String, PaymentReceiptEvent> consumer =
                new KafkaConsumer<>(
                        consumerProperties,
                        new StringDeserializer(),
                        jsonDeserializer
                );

        try {

            consumer.subscribe(java.util.List.of(TOPIC));

            // Assign partitions before publishing
            consumer.poll(Duration.ofSeconds(5));

            // 4. Create dummy payment event

            UUID paymentId = UUID.randomUUID();
            UUID rideId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            PaymentReceiptEvent event = new PaymentReceiptEvent(
                    paymentId,
                    rideId,
                    userId,
                    "test@example.com",
                    new BigDecimal("500.00"),
                    "SUCCESS",
                    "UPI",
                    LocalDateTime.now()
            );

            // 5. Publish using actual producer

            notificationProducer.publishPaymentReceipt(event);

            // 6. Read the event from Kafka

            ConsumerRecord<String, PaymentReceiptEvent> receivedRecord = null;

            long deadline = System.currentTimeMillis() + 15000;

            while (System.currentTimeMillis() < deadline
                    && receivedRecord == null) {

                ConsumerRecords<String, PaymentReceiptEvent> records =
                        consumer.poll(Duration.ofMillis(500));

                for (ConsumerRecord<String, PaymentReceiptEvent> record : records) {
                    receivedRecord = record;
                    break;
                }
            }

            // 7. Assertions

            assertNotNull(receivedRecord, "Kafka did not receive the event");

            PaymentReceiptEvent receivedEvent = receivedRecord.value();

            assertEquals(paymentId, receivedEvent.getPaymentId());
            assertEquals(rideId, receivedEvent.getRideId());
            assertEquals(userId, receivedEvent.getUserId());

            assertEquals("test@example.com", receivedEvent.getEmail());

            assertEquals(
                    new BigDecimal("500.00"),
                    receivedEvent.getAmount()
            );

            assertEquals("SUCCESS", receivedEvent.getPaymentStatus());
            assertEquals("UPI", receivedEvent.getPaymentMethod());

            assertEquals(
                    paymentId.toString(),
                    receivedRecord.key()
            );

        } finally {

            consumer.close();
            kafkaTemplate.destroy();
        }
    }
}
