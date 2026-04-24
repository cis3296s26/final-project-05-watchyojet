package com.watchyojet.engine;

import com.watchyojet.model.Aircraft;

public class TrajectoryPredictor {

    public static double[] predictPosition(Aircraft a, double timeSeconds) {
        return predictPosition(a, timeSeconds, a.getHeading());
    }

    public static double[] predictPosition(Aircraft a, double timeSeconds, double overrideHeadingDeg) {
        double lat = a.getLat();
        double lon = a.getLon();
        double heading = Math.toRadians(overrideHeadingDeg);
        double distanceNm = a.getSpeed() * (timeSeconds / 3600.0);
        double dLat = distanceNm * Math.cos(heading) / 60.0;
        double dLon = distanceNm * Math.sin(heading) / (60.0 * Math.cos(Math.toRadians(lat)));
        return new double[]{lat + dLat, lon + dLon};
    }
}