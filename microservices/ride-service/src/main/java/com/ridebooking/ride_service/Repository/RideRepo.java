package com.ridebooking.ride_service.Repository;

import com.ridebooking.ride_service.Entity.Ride;
import com.ridebooking.ride_service.Enums.RideStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RideRepo extends JpaRepository<Ride, UUID> {

    Optional<Ride> findById(UUID id);

    @Query("""
    SELECT r
    FROM Ride r
    WHERE r.userId = :userId
    AND r.rideStatus IN (
        com.ridebooking.ride_service.Enums.RideStatus.REQUESTED,
        com.ridebooking.ride_service.Enums.RideStatus.ACCEPTED,
        com.ridebooking.ride_service.Enums.RideStatus.INPROGRESS,
        com.ridebooking.ride_service.Enums.RideStatus.PAYMENT_PENDING
    )
""")
    Optional<Ride> findActiveRideByUserId(@Param("userId") UUID userId);


    Optional<Ride> findFirstByUserIdAndRideStatusOrderByIdDesc(
            UUID userId,
            RideStatus rideStatus
    );
}
