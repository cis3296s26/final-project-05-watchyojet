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

                existing.setLat(live.getLat());
                existing.setLon(live.getLon());
                existing.setHeading(live.getHeading());
                existing.setSpeed(live.getSpeed());

                // don't overwrite altitude — ATC may have already issued a resolution
                updatedMap.put(callsign, existing);

            } else {
                updatedMap.put(callsign, live);
            }
        }

        aircraftMap = updatedMap;
    }

    public List<Aircraft> getAircrafts() {
        return new ArrayList<>(aircraftMap.values());
    }

    public void reset(List<Aircraft> fresh) {
        Map<String, Aircraft> newMap = new ConcurrentHashMap<>();
        for (Aircraft a : fresh) newMap.put(a.getCallsign(), a);
        aircraftMap = newMap;
    }
}
