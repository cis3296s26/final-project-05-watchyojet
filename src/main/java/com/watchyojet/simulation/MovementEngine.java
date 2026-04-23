package com.watchyojet.simulation;

import java.util.List;

import com.watchyojet.model.Aircraft;

public class MovementEngine {

    private static final double CYCLE_SECONDS = 2.0;

    public void updatePositions(List<Aircraft> aircrafts) {

        for (Aircraft a : aircrafts) {

            double speed = a.getSpeed(); // knots
            double heading = Math.toRadians(a.getHeading());

            // Distance traveled in this 2-second cycle (nautical miles)
            double distanceNm = speed * (CYCLE_SECONDS / 3600.0);

            // Convert NM to degrees: 1 degree lat = 60 NM; lon scaled by cos(lat)
            double dLat = distanceNm * Math.cos(heading) / 60.0;
            double dLon = distanceNm * Math.sin(heading) / (60.0 * Math.cos(Math.toRadians(a.getLat())));

            a.setLat(a.getLat() + dLat);
            a.setLon(a.getLon() + dLon);
        }
    }
}