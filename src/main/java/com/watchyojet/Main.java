package com.watchyojet;

import java.util.ArrayList;
import java.util.List;

import com.watchyojet.engine.ATCEngine;
import com.watchyojet.model.Aircraft;
import com.watchyojet.model.AircraftType;

public class Main {
    public static void main(String[] args) {

        List<Aircraft> aircrafts = new ArrayList<>();

        aircrafts.add(new Aircraft("A1", 39.8730, -75.2437, 30000, 800, 45, AircraftType.A320));
        aircrafts.add(new Aircraft("A2", 39.9730, -75.2437, 30000, 800, 225, AircraftType.F16));
        aircrafts.add(new Aircraft("A3", 40.9730, -75.2437, 30000, 800, 90, AircraftType.AIR_AMBULANCE));
        //

        ATCEngine engine = new ATCEngine();

        while (true) {

            engine.runCycle(aircrafts);

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}