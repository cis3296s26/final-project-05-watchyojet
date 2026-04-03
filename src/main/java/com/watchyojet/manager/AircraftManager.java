package com.watchyojet.manager;

import java.util.ArrayList;
import java.util.List;

import com.watchyojet.model.Aircraft;

public class AircraftManager {
    private List<Aircraft> aircrafts = new ArrayList<>();

    public void addAircraft(Aircraft a) {
        aircrafts.add(a);
    }

    public List<Aircraft> getAircrafts() {
        return aircrafts;
    }
}