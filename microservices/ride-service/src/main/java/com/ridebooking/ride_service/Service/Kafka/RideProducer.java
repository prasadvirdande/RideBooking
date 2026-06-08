package com.ridebooking.ride_service.Service.Kafka;

import com.ridebooking.ride_service.DTO.Event.RideAcceptedRideEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RideProducer {

    private final KafkaTemplate<String,Object> kafkaTemplate;

    public void publishRideAccepted(
   RideAcceptedRideEvent event
    ) {

        kafkaTemplate.send(
                "ride-accepted-topic",
                event
        );

        System.out.println(
                "EVENT PUBLISHED : " + event
        );
    }
}