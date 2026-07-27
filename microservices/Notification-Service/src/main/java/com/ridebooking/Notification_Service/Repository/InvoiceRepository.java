package com.ridebooking.Notification_Service.Repository;



import com.ridebooking.Notification_Service.Entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    Optional<Invoice> findByPaymentId(String paymentId);

    Optional<Invoice> findByRideId(String rideId);
}
