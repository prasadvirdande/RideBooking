package com.ridebooking.driver_service.Service;

import com.ridebooking.driver_service.DTO.RideAcceptedEvent;
import com.ridebooking.driver_service.DTO.RideStartedEvent;
import com.ridebooking.driver_service.Entity.Driver;
import com.ridebooking.driver_service.Enum.DriverStatus;
import com.ridebooking.driver_service.Repository.Driverepo;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RideAcceptedConsumer {

    private final Driverepo driverepo;

    @KafkaListener(
            topics = "ride-accepted-topic",
            groupId = "driver-group"
    )
    public void consume(
            RideAcceptedEvent event
    ) {


        Driver driver =
                driverepo.findById(
                        UUID.fromString(
                                event.getDriverId()
                        )
                ).orElseThrow();

        driver.setStatus(
                DriverStatus.BUSY
        );

        driverepo.save(driver);
    }

    public void consume1(RideStartedEvent event1) {
      System.out.println("Ride Started");
        Driver driver =
                driverepo.findById(
                        UUID.fromString(
                                event1.getDriverId()
                        )
                ).orElseThrow();

        driver.setStatus(
                DriverStatus.INPROGRESS
        );

        driverepo.save(driver);
    }

    }

