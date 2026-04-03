package com.watchyojet.engine;

import com.watchyojet.model.Aircraft;

import java.util.ArrayList;
import java.util.List;

public class TrajectoryPredictor {


    public List<Aircraft> predict(List<Aircraft> aircrafts, double seconds){
        List<Aircraft> futureAircrafts = new ArrayList<>(aircrafts);
        for (Aircraft a : futureAircrafts) {

            double speed = a.getSpeed();      // units per cycle
            double heading = Math.toRadians(a.getHeading());

            double dx = speed * Math.cos(heading) * seconds;
            double dy = speed * Math.sin(heading) * seconds;

            a.setLat(a.getLat() + dx);
            a.setLon(a.getLon() + dy);
        }

        return futureAircrafts;
    }
}
