package com.watchyojet.engine;

import java.util.ArrayList;
import java.util.List;

import com.watchyojet.model.Aircraft;
import com.watchyojet.model.Conflict;

public class ConflictDetector {

    private static final double MIN_DISTANCE = 1.0;
    private static final double MAX_LOOKAHEAD_SECONDS = 300.0;

    // Toggle for demo
    private static final boolean DEMO_MODE = true;

    public List<Conflict> detectConflicts(List<Aircraft> aircrafts) {

        List<Conflict> conflicts = new ArrayList<>();

        for (int i = 0; i < aircrafts.size(); i++) {
            for (int j = i + 1; j < aircrafts.size(); j++) {

                Aircraft a1 = aircrafts.get(i);
                Aircraft a2 = aircrafts.get(j);

                double altitudeDiff = Math.abs(
                        Math.round(a1.getAltitude()) - Math.round(a2.getAltitude()));

                double tCPA = timeToCPA(a1, a2);

                if (tCPA <= 0 || tCPA >= MAX_LOOKAHEAD_SECONDS) continue;

                double cpaDistance = distanceAtCPA(a1, a2, tCPA);

                // Disabled in demo mode
                if (!DEMO_MODE && cpaDistance < MIN_DISTANCE && altitudeDiff < 1000) {

                    String sevStr = classifySeverity(cpaDistance, altitudeDiff);
                    Conflict.Severity severity = switch (sevStr) {
                        case "CRITICAL" -> Conflict.Severity.CRITICAL;
                        case "HIGH"     -> Conflict.Severity.HIGH;
                        default         -> Conflict.Severity.MEDIUM;
                    };

                    conflicts.add(new Conflict(a1, a2, severity, tCPA, cpaDistance));

                    System.out.println("\n[CONFLICT DETECTED]");
                    System.out.println(a1.getCallsign() + " ↔ " + a2.getCallsign());
                    System.out.println("→ tCPA: " + String.format("%.0f", tCPA) + " sec");
                    System.out.println("→ dCPA: " + String.format("%.2f", cpaDistance) + " NM");
                    System.out.println("→ Altitude diff: " + String.format("%.0f", altitudeDiff) + " ft");
                    System.out.println("→ Severity: " + sevStr);
                }
            }
        }

        return conflicts;
    }

    private double timeToCPA(Aircraft a1, Aircraft a2) {

        double avgLat = (a1.getLat() + a2.getLat()) / 2.0;
        double cosLat = Math.cos(Math.toRadians(avgLat));

        double x1 = a1.getLon() * 60.0 * cosLat;
        double y1 = a1.getLat() * 60.0;
        double x2 = a2.getLon() * 60.0 * cosLat;
        double y2 = a2.getLat() * 60.0;

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
        return tHours * 3600.0;
    }

    private double distanceAtCPA(Aircraft a1, Aircraft a2, double tCPA) {

        double[] p1 = TrajectoryPredictor.predictPosition(a1, tCPA);
        double[] p2 = TrajectoryPredictor.predictPosition(a2, tCPA);

        return distanceNM(p1[0], p1[1], p2[0], p2[1]);
    }

    private double distanceNM(double lat1, double lon1, double lat2, double lon2) {
        double avgLat = (lat1 + lat2) / 2.0;
        double dLat = (lat2 - lat1) * 60.0;
        double dLon = (lon2 - lon1) * 60.0 * Math.cos(Math.toRadians(avgLat));
        return Math.sqrt(dLat * dLat + dLon * dLon);
    }

    private String classifySeverity(double distance, double altDiff) {
        if (distance < 1.0 && altDiff < 300) return "CRITICAL";
        else if (distance < 2.0)             return "HIGH";
        else                                 return "MEDIUM";
    }
}