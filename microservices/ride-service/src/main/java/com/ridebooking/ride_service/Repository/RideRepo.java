package com.ridebooking.ride_service.Repository;

import com.ridebooking.ride_service.Entity.Ride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RideRepo extends JpaRepository<Ride, UUID> {

    Optional<Ride> findById(UUID id);
}
