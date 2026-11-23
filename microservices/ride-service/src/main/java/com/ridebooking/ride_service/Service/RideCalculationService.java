package com.ridebooking.ride_service.Service;

import org.springframework.stereotype.Service;

@Service
public class RideCalculationService {

    private static final int EARTH_RADIUS_KM = 6371;

    private static final double BASE_FARE = 40.0;
    private static final double PER_KM_FARE = 12.0;
    private static final double AVERAGE_SPEED_KMH = 30.0;

    public double calculateDistance(
            Double latitude1,
            Double longitude1,
            Double latitude2,
            Double longitude2) {

        validateCoordinates(latitude1, longitude1);
        validateCoordinates(latitude2, longitude2);

        double latDistance =
                Math.toRadians(latitude2 - latitude1);

        double lonDistance =
                Math.toRadians(longitude2 - longitude1);

        double a =
                Math.pow(Math.sin(latDistance / 2), 2)
                        + Math.cos(Math.toRadians(latitude1))
                        * Math.cos(Math.toRadians(latitude2))
                        * Math.pow(Math.sin(lonDistance / 2), 2);

        a = Math.max(0.0, Math.min(1.0, a));

        double c = 2 * Math.atan2(
                Math.sqrt(a),
                Math.sqrt(1.0 - a)
        );

        return EARTH_RADIUS_KM * c;
    }

    public double calculateFare(double distanceKm) {

        if (!Double.isFinite(distanceKm) || distanceKm < 0) {
            throw new IllegalArgumentException("Invalid distance.");
        }

        return BASE_FARE + distanceKm * PER_KM_FARE;
    }

    public int calculateEstimatedTime(double distanceKm) {

        if (!Double.isFinite(distanceKm) || distanceKm < 0) {
            throw new IllegalArgumentException("Invalid distance.");
        }

        return (int) Math.ceil(
                (distanceKm / AVERAGE_SPEED_KMH) * 60
        );
    }

    private void validateCoordinates(Double latitude, Double longitude) {

        if (latitude == null || longitude == null
                || !Double.isFinite(latitude)
                || !Double.isFinite(longitude)) {

            throw new IllegalArgumentException(
                    "Coordinates must be present and finite."
            );
        }

        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Invalid latitude.");
        }

        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Invalid longitude.");
        }
    }
}