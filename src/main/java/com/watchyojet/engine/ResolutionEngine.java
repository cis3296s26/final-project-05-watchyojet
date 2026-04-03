package com.watchyojet.engine;

import java.util.List;

import com.watchyojet.model.Aircraft;
import com.watchyojet.model.AircraftCategory;
import com.watchyojet.model.Conflict;
import com.watchyojet.model.Resolution;

public class ResolutionEngine {

    private static final double ALTITUDE_STEP = 1000;
    private static final double ALTITUDE_BUFFER = 500;
    private double snapToFlightLevel(double altitude)
    {
    return Math.round(altitude / 1000) * 1000;
    }

    public Resolution resolveConflict(Conflict c, List<Aircraft> allAircraft) {

        Aircraft a1 = c.getA1();
        Aircraft a2 = c.getA2();

        Aircraft a;

        // PRIORITY LOGIC
        if (a1.getCategory() == AircraftCategory.EMERGENCY) {
            a = a2;
        } else if (a2.getCategory() == AircraftCategory.EMERGENCY) {
            a = a1;
        } else if (a1.getCategory() == AircraftCategory.MILITARY) {
            a = a2;
        } else {
            a = a1;
        }

        double currentAlt = a.getAltitude();

        double maxAlt = a.getType().getMaxAltitude();
        double climbRate = a.getType().getClimbRate();


       double[] options = {
        currentAlt + ALTITUDE_STEP,
        currentAlt - ALTITUDE_STEP,
        currentAlt + 2 * ALTITUDE_STEP,
        currentAlt - 2 * ALTITUDE_STEP
        };

        for (double alt : options) {
            alt = snapToFlightLevel(alt); 
            // constraint checks
            if (alt > maxAlt) continue;
            if (alt < 0) continue;

            if (isAltitudeFree(alt, allAircraft, a)) {
                System.out.println("✈️ " + a.getCallsign() +
                        " moves to altitude " + alt);
                return new Resolution(a, alt);
            }
        }

        // fallback
        System.out.println("⚠️ No safe altitude found, forcing minimal climb");
        return new Resolution(a, Math.min(currentAlt + ALTITUDE_STEP, maxAlt));   
     }

    private boolean isAltitudeFree(double targetAlt,
                                   List<Aircraft> aircrafts,
                                   Aircraft self) {

        for (Aircraft other : aircrafts) {

            if (other == self) continue;

            double diff = Math.abs(other.getAltitude() - targetAlt);

            if (diff < ALTITUDE_BUFFER) {
                return false;
            }
        }
        return true;
    }
}