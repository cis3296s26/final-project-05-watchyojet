package com.watchyojet.engine;

import java.util.List;

import com.watchyojet.model.Aircraft;
import com.watchyojet.model.AircraftCategory;
import com.watchyojet.model.Conflict;
import com.watchyojet.model.Resolution;

public class ResolutionEngine {

    private static final double ALTITUDE_STEP = 1000;
    // ALTITUDE_BUFFER must equal SAFE_VERTICAL_SEPARATION so that any accepted
    // slot is guaranteed to be ≥1000 ft from every other aircraft.
    private static final double ALTITUDE_BUFFER = 1000;
    private static final double SAFE_VERTICAL_SEPARATION = 1000;
    private static final double MIN_ALTITUDE = 3000;

    private double snapToFlightLevel(double altitude) {
        return Math.round(altitude / 1000) * 1000;
    }

    // Select which aircraft to move based on priority: EMERGENCY > MILITARY > default
    private Aircraft selectAircraftToMove(Conflict c) {
        Aircraft a1 = c.getA1();
        Aircraft a2 = c.getA2();
        if (a1.getCategory() == AircraftCategory.EMERGENCY) return a2;
        if (a2.getCategory() == AircraftCategory.EMERGENCY) return a1;
        if (a1.getCategory() == AircraftCategory.MILITARY)  return a2;
        if (a2.getCategory() == AircraftCategory.MILITARY)  return a1;
        return a1;
    }

    public Resolution resolveConflict(Conflict c, List<Aircraft> allAircraft) {
        Aircraft a1 = c.getA1();
        Aircraft a2 = c.getA2();

        double separation = Math.abs(a1.getAltitude() - a2.getAltitude());
        if (separation >= SAFE_VERTICAL_SEPARATION) return null;

        Aircraft toMove = selectAircraftToMove(c);
        return resolveForAircraft(toMove, allAircraft);
    }

    // Exposed so ATCEngine can attempt the other aircraft when the primary is
    // already resolved this cycle.
    public Resolution resolveForAircraft(Aircraft a, List<Aircraft> allAircraft) {

        double currentAlt = a.getAltitude();
        double maxAlt     = a.getType().getMaxAltitude();

        // Search ±1000 through ±15000 ft in 1000-ft steps, closest first
        double[] options = new double[30];
        for (int k = 0; k < 15; k++) {
            options[k * 2]     = currentAlt + (k + 1) * ALTITUDE_STEP;
            options[k * 2 + 1] = currentAlt - (k + 1) * ALTITUDE_STEP;
        }

        for (double alt : options) {
            alt = snapToFlightLevel(alt);

            if (alt > maxAlt) continue;
            if (alt <= 0)     continue;

            String phase = a.getFlightPhase();
            if (phase.equals("CRUISE") || phase.equals("CLIMB_DESCENT")) {
                if (alt < MIN_ALTITUDE) continue;
            }

            if (!isAltitudeFree(alt, allAircraft, a)) continue;

            return new Resolution(a, alt);
        }

        return null;
    }

    private static final double PROXIMITY_NM = 8.0;

    private boolean isAltitudeFree(double targetAlt, List<Aircraft> aircrafts, Aircraft self) {
        for (Aircraft other : aircrafts) {
            if (other == self) continue;
            if (distanceNM(self.getLat(), self.getLon(),
                           other.getLat(), other.getLon()) > PROXIMITY_NM) continue;
            if (Math.abs(other.getAltitude() - targetAlt) < ALTITUDE_BUFFER) return false;
        }
        return true;
    }

    private double distanceNM(double lat1, double lon1, double lat2, double lon2) {
        double dLat = (lat2 - lat1) * 60.0;
        double dLon = (lon2 - lon1) * 60.0 * Math.cos(Math.toRadians(lat1));
        return Math.sqrt(dLat * dLat + dLon * dLon);
    }
}
