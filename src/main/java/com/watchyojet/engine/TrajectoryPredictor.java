package com.watchyojet.engine;

import com.watchyojet.model.Aircraft;

public class TrajectoryPredictor {

    // Predicts the future position of an aircraft after a given time in seconds
    public static double[] predictPosition(Aircraft a, double timeSeconds) {

        double lat = a.getLat();
        double lon = a.getLon();

        double speed = a.getSpeed(); // knots
        double heading = Math.toRadians(a.getHeading());

        // Distance traveled in nautical miles
        double distanceNm = speed * (timeSeconds / 3600.0);

        // Convert to lat/lon movement
        double dLat = distanceNm * Math.cos(heading) / 60.0;
        double dLon = distanceNm * Math.sin(heading) / (60.0 * Math.cos(Math.toRadians(lat)));

        double newLat = lat + dLat;
        double newLon = lon + dLon;

        return new double[]{newLat, newLon};
    }
}