package com.watchyojet.engine;

import java.util.List;

import com.watchyojet.model.Aircraft;
import com.watchyojet.model.AircraftCategory;
import com.watchyojet.model.Conflict;
import com.watchyojet.model.Resolution;

public class ResolutionEngine {

    private static final double ALTITUDE_STEP = 1000;
    private static final double ALTITUDE_BUFFER = 500;
    private static final double SAFE_VERTICAL_SEPARATION = 1000;
    private static final double MIN_ALTITUDE = 3000;

    private double snapToFlightLevel(double altitude) {
        return Math.round(altitude / 1000) * 1000;
    }

    public Resolution resolveConflict(Conflict c, List<Aircraft> allAircraft) {

        Aircraft a1 = c.getA1();
        Aircraft a2 = c.getA2();

        double separation = Math.abs(a1.getAltitude() - a2.getAltitude());

        // Already safe → do nothing
        if (separation >= SAFE_VERTICAL_SEPARATION) {
            return null;
        }

        // Decide which aircraft to move
        Aircraft a = a1;

        if (a1.getCategory() == AircraftCategory.EMERGENCY) {
            a = a2;
        } else if (a2.getCategory() == AircraftCategory.EMERGENCY) {
            a = a1;
        } else if (a1.getCategory() == AircraftCategory.MILITARY) {
            a = a2;
        }

        double currentAlt = a.getAltitude();
        double maxAlt = a.getType().getMaxAltitude();

        // Search ±1000 through ±8000 ft in 1000-ft steps, closest first
        double[] options = new double[16];
        for (int k = 0; k < 8; k++) {
            options[k * 2]     = currentAlt + (k + 1) * ALTITUDE_STEP;
            options[k * 2 + 1] = currentAlt - (k + 1) * ALTITUDE_STEP;
        }

        for (double alt : options) {

            alt = snapToFlightLevel(alt);

            // Basic constraints
            if (alt > maxAlt) continue;
            if (alt <= 0) continue;

            // Flight phase logic
            String phase = a.getFlightPhase();

            // Enroute aircraft must stay above safe minimum
            if (phase.equals("CRUISE") || phase.equals("CLIMB_DESCENT")) {
                if (alt < MIN_ALTITUDE) continue;
            }

            // Check if altitude is free
            if (!isAltitudeFree(alt, allAircraft, a)) continue;

            // ✅ RESOLUTION FOUND
            System.out.println("[RESOLUTION]");
            System.out.println("→ Aircraft: " + a.getCallsign());
            System.out.println("→ New altitude: " + alt + " ft");

            return new Resolution(a, alt);
        }

        // ❌ No valid altitude found
        System.out.println("⚠ No safe altitude found for " + a.getCallsign());
        return null;
    }

    private static final double PROXIMITY_NM = 60.0;

    private boolean isAltitudeFree(double targetAlt, List<Aircraft> aircrafts, Aircraft self) {

        for (Aircraft other : aircrafts) {

            if (other == self) continue;

            // Only check aircraft that are geographically close enough to matter
            if (distanceNM(self.getLat(), self.getLon(),
                           other.getLat(), other.getLon()) > PROXIMITY_NM) continue;

            if (Math.abs(other.getAltitude() - targetAlt) < ALTITUDE_BUFFER) {
                return false;
            }
        }

        return true;
    }

    private double distanceNM(double lat1, double lon1, double lat2, double lon2) {
        double dLat = (lat2 - lat1) * 60.0;
        double dLon = (lon2 - lon1) * 60.0 * Math.cos(Math.toRadians(lat1));
        return Math.sqrt(dLat * dLat + dLon * dLon);
    }
}