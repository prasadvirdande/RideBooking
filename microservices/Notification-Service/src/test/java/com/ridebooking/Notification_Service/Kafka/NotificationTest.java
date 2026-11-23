package com.ridebooking.Notification_Service.Kafka;

import com.ridebooking.Notification_Service.DTO.Event.PaymentReceiptEvent;
import com.ridebooking.Notification_Service.Service.EmailService;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import org.junit.jupiter.api.Test;

import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringJUnitConfig(
        classes = PaymentReceiptConsumerTest.TestConfig.class
)
@Testcontainers
class PaymentReceiptConsumerTest {

    private static final String TOPIC = "payment-receipt-topic";

    @Container
    static final KafkaContainer kafka =
            new KafkaContainer(
                    DockerImageName.parse("confluentinc/cp-kafka:7.6.1")
            );

    @DynamicPropertySource
    static void configureKafka(DynamicPropertyRegistry registry) {

        registry.add(
                "spring.kafka.bootstrap-servers",
                kafka::getBootstrapServers
        );
    }

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private EmailService emailService;

    @Test
    void shouldConsumePaymentReceiptAndInvokeEmailService() throws Exception {

        // 1. Create dummy payment details

        UUID paymentId = UUID.randomUUID();
        UUID rideId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        String email = "test@example.com";

        BigDecimal amount = new BigDecimal("500.00");

        PaymentReceiptEvent event = new PaymentReceiptEvent(
                paymentId,

                rideId,
                userId,
                email,
                amount,
                "SUCCESS",
                "UPI",
                LocalDateTime.now()
        );

        // 2. Publish event to Kafka

        kafkaTemplate.send(
                TOPIC,
                paymentId.toString(),
                event
        ).get();

        // 3. Verify actual consumer invoked EmailService

        verify(emailService, timeout(15000))
                .sendPaymentReceipt(
                        email,
                        rideId.toString(),
                        amount,
                        paymentId.toString()
                );
    }

    @Configuration
    @EnableKafka
    @Import(com.ridebooking.Notification_Service.Service.Kafka.PaymentReceiptConsumer.class)
    static class TestConfig {

        @Bean
        EmailService emailService() {
            return Mockito.mock(EmailService.class);
        }

        @Bean
        KafkaAdmin kafkaAdmin(
                @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers
        ) {

            Map<String, Object> properties = new HashMap<>();

            properties.put(
                    org.apache.kafka.clients.admin.AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG,
                    bootstrapServers
            );

            return new KafkaAdmin(properties);
        }

        @Bean
        NewTopic paymentReceiptTopic() {

            return new NewTopic(TOPIC, 1, (short) 1);
        }

        @Bean
        ProducerFactory<String, Object> producerFactory(
                @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers
        ) {

            Map<String, Object> properties = new HashMap<>();

            properties.put(
                    ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                    bootstrapServers
            );

            return new DefaultKafkaProducerFactory<>(
                    properties,
                    new StringSerializer(),
                    new JsonSerializer<>()
            );
        }

        @Bean
        KafkaTemplate<String, Object> kafkaTemplate(
                ProducerFactory<String, Object> producerFactory
        ) {

            return new KafkaTemplate<>(producerFactory);
        }

        @Bean
        ConsumerFactory<String, PaymentReceiptEvent> consumerFactory(
                @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers
        ) {

            Map<String, Object> properties = new HashMap<>();

            properties.put(
                    ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                    bootstrapServers
            );

            properties.put(
                    ConsumerConfig.GROUP_ID_CONFIG,
                    "notification-consumer-test"
            );

            properties.put(
                    ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                    "earliest"
            );

            properties.put(
                    ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
                    false
            );

            JsonDeserializer<PaymentReceiptEvent> deserializer =
                    new JsonDeserializer<>(
                            PaymentReceiptEvent.class,
                            false
                    );

            deserializer.addTrustedPackages(
                    "com.ridebooking.Notification_Service.DTO.Event"
            );

            return new DefaultKafkaConsumerFactory<>(
                    properties,
                    new StringDeserializer(),
                    deserializer
            );
        }

        @Bean
        ConcurrentKafkaListenerContainerFactory<String, PaymentReceiptEvent>
        kafkaListenerContainerFactory(
                ConsumerFactory<String, PaymentReceiptEvent> consumerFactory
        ) {

            ConcurrentKafkaListenerContainerFactory<String, PaymentReceiptEvent>
                    factory =
                    new ConcurrentKafkaListenerContainerFactory<>();

            factory.setConsumerFactory(consumerFactory);

            return factory;
        }
    }
}