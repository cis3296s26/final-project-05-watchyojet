package com.watchyojet.manager;

import java.util.ArrayList;
import java.util.List;

import com.watchyojet.model.Aircraft;

// Springboot REST endpoints
import org.springframework.stereotype.Component;

@Component
public class AircraftManager {
    private List<Aircraft> aircrafts = new ArrayList<>();

    public void addAircraft(Aircraft a) {
        aircrafts.add(a);
    }

    // Called by GET and POST /api/aircraft
    public List<Aircraft> getAircrafts() {
        return aircrafts;
    }

    // Removes aircraft from simulation using its "callsign"
    // True if found, False if not found
    // Called by DELETE /api/aircraft/{callsign}
    public boolean removeAircraft(String callsign) {
        return aircrafts.removeIf(a -> a.getCallsign().equalsIgnoreCase(callsign));
    }
}