package com.ridebooking.ride_service.Service.Kafka.Consumer;

import com.ridebooking.ride_service.DTO.Event.PaymentCompletedEvent;


import com.ridebooking.ride_service.Service.Impl.RideServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentConsumer {

    private final RideServiceImpl rideService;

    @KafkaListener(
            topics = "payment-completed-topic",
            groupId = "ride-service"
    )
    public void consumePaymentCompleted(PaymentCompletedEvent event) {

        log.info("Payment completed event received: {}", event);

        if (!"SUCCESS".equals(event.getPaymentStatus())) {
            return;
        }

        rideService.completeRideAfterPayment(event.getRideId());
    }
}