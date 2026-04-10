package com.watchyojet.engine;

import java.util.ArrayList;
import java.util.List;

import com.watchyojet.WYJAppController;
import com.watchyojet.model.Aircraft;
import com.watchyojet.model.Conflict;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

public class ConflictDetector {


    private static final double MIN_DISTANCE = 5.0;

    public List<Conflict> detectConflicts(List<Aircraft> aircrafts) {

        List<Conflict> conflicts = new ArrayList<>();

        for (int i = 0; i < aircrafts.size(); i++) {
            for (int j = i + 1; j < aircrafts.size(); j++) {

                Aircraft a1 = aircrafts.get(i);
                Aircraft a2 = aircrafts.get(j);

                double dist = distance(a1, a2);

                double altitudeDiff = Math.abs(a1.getAltitude() - a2.getAltitude());
                if (dist < MIN_DISTANCE && altitudeDiff < 1000) 
                    {
                    conflicts.add(new Conflict(a1, a2));
                     System.out.println("⚠️ Conflict detected between "+ a1.getCallsign() + " and " + a2.getCallsign());
                     WYJAppController.getInstance().log("⚠ Conflict detected between "+ a1.getCallsign() + " and " + a2.getCallsign());
                }
            }
        }

        return conflicts;
    }

    private double distance(Aircraft a1, Aircraft a2) {
        double dx = a1.getLat() - a2.getLat();
        double dy = a1.getLon() - a2.getLon();

        return Math.sqrt(dx * dx + dy * dy);
    }
}