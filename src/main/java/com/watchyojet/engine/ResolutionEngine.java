package com.watchyojet.engine;

import com.watchyojet.model.*;

import java.util.List;

public class ResolutionEngine {

    private static final double ALTITUDE_STEP = 1000;
    private static final double ALTITUDE_BUFFER = 500; // separation buffer

    public Resolution resolveConflict(Conflict c, List<Aircraft> allAircraft) {

        Aircraft a = c.getA1();

        double currentAlt = a.getAltitude();

        // try multiple altitude options
        double[] options = {
                currentAlt + ALTITUDE_STEP,
                currentAlt - ALTITUDE_STEP,
                currentAlt + 2 * ALTITUDE_STEP,
                currentAlt - 2 * ALTITUDE_STEP
        };

        for (double alt : options) {
            if (isAltitudeFree(alt, allAircraft, a)) {
                System.out.println("✈️ " + a.getCallsign() +
                        " moves to altitude " + alt);
                return new Resolution(a, alt);
            }
        }

        // fallback (if everything is somehow occupied)
        System.out.println("⚠️ No safe altitude found, forcing climb");
        return new Resolution(a, currentAlt + ALTITUDE_STEP);
    }

    private boolean isAltitudeFree(double targetAlt,
                                   List<Aircraft> aircrafts,
                                   Aircraft self) {

        for (Aircraft other : aircrafts) {

            if (other == self) continue;

            double diff = Math.abs(other.getAltitude() - targetAlt);

            if (diff < ALTITUDE_BUFFER) {
                return false; // too close vertically
            }
        }
        return true;
    }
}