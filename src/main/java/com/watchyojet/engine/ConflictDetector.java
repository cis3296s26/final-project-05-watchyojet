package com.watchyojet.engine;

import java.util.ArrayList;
import java.util.List;

import com.watchyojet.WYJAppController;
import com.watchyojet.model.Aircraft;
import com.watchyojet.model.Conflict;

public class ConflictDetector {

    private static final double MIN_DISTANCE = 5.0; // nautical miles
    private static final double MAX_LOOKAHEAD_SECONDS = 300.0; // 5 minutes

    public List<Conflict> detectConflicts(List<Aircraft> aircrafts) {

        List<Conflict> conflicts = new ArrayList<>();

        for (int i = 0; i < aircrafts.size(); i++) {
            for (int j = i + 1; j < aircrafts.size(); j++) {

                Aircraft a1 = aircrafts.get(i);
                Aircraft a2 = aircrafts.get(j);

                double altitudeDiff = Math.abs(
                        Math.round(a1.getAltitude()) - Math.round(a2.getAltitude())
                );

                double tCPA = timeToCPA(a1, a2);
                double cpaDistance = distanceAtCPA(a1, a2);

                // Conflict condition
                if (tCPA > 0 && tCPA < MAX_LOOKAHEAD_SECONDS
                        && cpaDistance < MIN_DISTANCE
                        && altitudeDiff < 1000) {

                    conflicts.add(new Conflict(a1, a2));

                    String severity = classifySeverity(cpaDistance, altitudeDiff);

                    System.out.println("\n[CONFLICT DETECTED]");
                    System.out.println(a1.getCallsign() + " ↔ " + a2.getCallsign());
                    System.out.println("→ tCPA: " + String.format("%.0f", tCPA) + " sec");
                    System.out.println("→ dCPA: " + String.format("%.2f", cpaDistance) + " NM");
                    System.out.println("→ Altitude diff: " + String.format("%.0f", altitudeDiff) + " ft");
                    System.out.println("→ Severity: " + severity);

                    if (WYJAppController.getInstance() != null) {
                        WYJAppController.getInstance().log(
                                "Conflict predicted between "
                                        + a1.getCallsign() + " and " + a2.getCallsign()
                        );
                    }
                }
            }
        }

        return conflicts;
    }

    // ---------------- CPA CALCULATIONS ----------------

    private double timeToCPA(Aircraft a1, Aircraft a2) {

        double avgLat = (a1.getLat() + a2.getLat()) / 2.0;
        double cosLat = Math.cos(Math.toRadians(avgLat));

        // Positions in NM: lat * 60, lon * 60 * cos(lat)
        double x1 = a1.getLon() * 60.0 * cosLat;
        double y1 = a1.getLat() * 60.0;
        double x2 = a2.getLon() * 60.0 * cosLat;
        double y2 = a2.getLat() * 60.0;

        // Velocities in NM/hr
        double vx1 = a1.getSpeed() * Math.sin(Math.toRadians(a1.getHeading()));
        double vy1 = a1.getSpeed() * Math.cos(Math.toRadians(a1.getHeading()));
        double vx2 = a2.getSpeed() * Math.sin(Math.toRadians(a2.getHeading()));
        double vy2 = a2.getSpeed() * Math.cos(Math.toRadians(a2.getHeading()));

        double dvx = vx2 - vx1;
        double dvy = vy2 - vy1;
        double dx  = x2 - x1;
        double dy  = y2 - y1;

        double dv2 = dvx * dvx + dvy * dvy;
        if (dv2 == 0) return -1;

        double tHours = -(dx * dvx + dy * dvy) / dv2;
        return tHours * 3600.0; // convert to seconds
    }

    private double distanceAtCPA(Aircraft a1, Aircraft a2) {

        double t = timeToCPA(a1, a2);

        if (t < 0 || t > MAX_LOOKAHEAD_SECONDS) return Double.MAX_VALUE;

        double[] p1 = TrajectoryPredictor.predictPosition(a1, t);
        double[] p2 = TrajectoryPredictor.predictPosition(a2, t);

        return distanceNM(p1[0], p1[1], p2[0], p2[1]);
    }

    private double distanceNM(double lat1, double lon1, double lat2, double lon2) {

        double dLat = (lat2 - lat1) * 60.0;
        double dLon = (lon2 - lon1) * 60.0 * Math.cos(Math.toRadians(lat1));

        return Math.sqrt(dLat * dLat + dLon * dLon);
    }

    private String classifySeverity(double distance, double altDiff) {

        if (distance < 1.0 && altDiff < 500) return "CRITICAL";
        else if (distance < 3.0) return "HIGH";
        else return "MEDIUM";
    }
}