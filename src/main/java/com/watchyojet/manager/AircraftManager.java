package com.watchyojet.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.watchyojet.model.Aircraft;

public class AircraftManager {

    private Map<String, Aircraft> aircraftMap = new HashMap<>();

    public void syncWithLiveData(List<Aircraft> liveData) {

        Map<String, Aircraft> updatedMap = new HashMap<>();

        for (Aircraft livePlane : liveData) {

            String callsign = livePlane.getCallsign();

            if (aircraftMap.containsKey(callsign)) {

                Aircraft existing = aircraftMap.get(callsign);

                // Update dynamic properties from live data
                existing.setLat(livePlane.getLat());
                existing.setLon(livePlane.getLon());
                existing.setHeading(livePlane.getHeading());

                // Preserve simulated altitude (do not overwrite)

                updatedMap.put(callsign, existing);

            } else {

                // New aircraft entering the airspace
                updatedMap.put(callsign, livePlane);
            }
        }

        // Replace old map with updated one
        aircraftMap = updatedMap;
    }

    public List<Aircraft> getAircrafts() {
        return new ArrayList<>(aircraftMap.values());
    }
}