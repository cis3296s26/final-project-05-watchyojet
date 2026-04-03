package com.watchyojet.simulation;

import java.util.List;

import com.watchyojet.model.Aircraft;

public class MovementEngine {

    public void updatePositions(List<Aircraft> aircrafts) {

        for (Aircraft a : aircrafts) {

            double speed = a.getSpeed();      // units per cycle
            double heading = Math.toRadians(a.getHeading());

            double dx = speed * Math.cos(heading) * 0.001;
            double dy = speed * Math.sin(heading) * 0.001;

            a.setLat(a.getLat() + dx);
            a.setLon(a.getLon() + dy);
        }
    }
}