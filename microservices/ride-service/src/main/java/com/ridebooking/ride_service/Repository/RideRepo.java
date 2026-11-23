package com.ridebooking.ride_service.Repository;

import com.ridebooking.ride_service.Entity.Ride;
import com.ridebooking.ride_service.Enums.RideStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RideRepo extends JpaRepository<Ride, UUID> {

    Optional<Ride> findByActiveUserId(UUID userId);

    Optional<Ride> findFirstByUserIdAndRideStatusOrderByCreatedAtDescIdDesc(
            UUID userId,
            RideStatus rideStatus
    );

    default Optional<Ride> findActiveRideByUserId(UUID userId) {
        return findByActiveUserId(userId);
    }

}

