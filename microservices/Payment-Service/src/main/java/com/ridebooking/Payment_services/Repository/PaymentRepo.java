package com.ridebooking.Payment_services.Repository;

import com.ridebooking.Payment_services.Entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PaymentRepo  extends JpaRepository<Payment,UUID> {

}
