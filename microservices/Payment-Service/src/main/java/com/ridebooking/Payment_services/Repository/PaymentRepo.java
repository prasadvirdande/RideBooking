package com.ridebooking.Payment_services.Repository;

import com.ridebooking.Payment_services.Entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepo  extends JpaRepository<Payment,UUID> {

    @Query(value = "select * from payments where ride_id=:rideId",nativeQuery = true)
    Optional<Payment> findByRideId(UUID rideId);

    Optional<Payment> findByGatewayOrderId(String paymentLinkId);
}
