package com.watchyojet.simulation;

import com.watchyojet.model.Aircraft;

public class MovementEngine {

    public void update(Aircraft a) {
        double distance = a.getSpeed() * 0.001;

        double newLat = a.getLat() + distance * Math.cos(Math.toRadians(a.getHeading()));
        double newLon = a.getLon() + distance * Math.sin(Math.toRadians(a.getHeading()));

        a.setLat(newLat);
        a.setLon(newLon);
    }
}