package com.ridebooking.driver_service.Repository;

import com.ridebooking.driver_service.Entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface Driverepo extends JpaRepository<Driver, UUID> {

    Optional<Driver> findByEmail(String email);
}
