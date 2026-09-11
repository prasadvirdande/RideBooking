package com.ridebooking.Payment_services.Repository;

import com.ridebooking.Payment_services.Entity.Enums.IdempotencyStatus;
import com.ridebooking.Payment_services.Entity.Idempotency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IdempotencyREpo extends JpaRepository<Idempotency, UUID> {

    Optional<Idempotency> findByStatus(IdempotencyStatus idempotencyStatus);
}
