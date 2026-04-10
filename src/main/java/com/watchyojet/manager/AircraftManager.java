package com.watchyojet.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.watchyojet.model.Aircraft;

public class AircraftManager {
    // Using a map prevents duplicates
    private Map<String, Aircraft> aircraftMap = new HashMap<>();

    public void syncWithLiveData(List<Aircraft> liveData) {
        List<String> activeCallsigns = new ArrayList<>();

        for (Aircraft livePlane : liveData) {
            activeCallsigns.add(livePlane.getCallsign());
            
            // We just replace the object in the map so we always have the freshest coordinates/speed
            aircraftMap.put(livePlane.getCallsign(), livePlane);
        }

        // Remove any planes from our system that werentt in the latest API ping (they landed or flew out of bounds)
        aircraftMap.keySet().retainAll(activeCallsigns);
    }

    public List<Aircraft> getAircrafts() {
        return new ArrayList<>(aircraftMap.values());
    }
}