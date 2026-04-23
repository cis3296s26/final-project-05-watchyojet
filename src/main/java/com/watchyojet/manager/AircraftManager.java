package com.watchyojet.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.watchyojet.model.Aircraft;

public class AircraftManager {

    private Map<String, Aircraft> aircraftMap = new ConcurrentHashMap<>();

    public void syncWithLiveData(List<Aircraft> liveData) {

        Map<String, Aircraft> updatedMap = new ConcurrentHashMap<>();

        for (Aircraft live : liveData) {

            String callsign = live.getCallsign();

            if (aircraftMap.containsKey(callsign)) {

                Aircraft existing = aircraftMap.get(callsign);

                // Update position and movement from live data every refresh cycle.
                existing.setLat(live.getLat());
                existing.setLon(live.getLon());
                existing.setHeading(live.getHeading());
                existing.setSpeed(live.getSpeed()); // fix: keep speed current for accurate CPA

                // Altitude is intentionally NOT overwritten: the simulation may have
                // issued a resolution that changed the aircraft's assigned altitude.
                // Overwriting with raw transponder data would undo ATC instructions.

                updatedMap.put(callsign, existing);

            } else {
                // New aircraft entering the airspace — use all live values including altitude.
                updatedMap.put(callsign, live);
            }
        }

        aircraftMap = updatedMap;
    }

    public List<Aircraft> getAircrafts() {
        return new ArrayList<>(aircraftMap.values());
    }
}
