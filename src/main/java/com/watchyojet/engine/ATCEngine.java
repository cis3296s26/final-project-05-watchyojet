package com.watchyojet.engine;

import java.util.ArrayList;
import java.util.List;

import com.watchyojet.WYJAppController;
import com.watchyojet.model.Aircraft;
import com.watchyojet.model.Conflict;
import com.watchyojet.model.Resolution;
import com.watchyojet.simulation.MovementEngine;

public class ATCEngine {

    private ConflictDetector detector;
    private ResolutionEngine resolver;
    private MovementEngine movement;
    private TrajectoryPredictor predictor;

    public ATCEngine() {
        this.detector = new ConflictDetector();
        this.resolver = new ResolutionEngine();
        this.movement = new MovementEngine();
        this.predictor = new TrajectoryPredictor();
    }

    public void runCycle(List<Aircraft> aircrafts) {

        System.out.println("\n--- ATC Cycle | tracking " + aircrafts.size() + " aircraft ---");

        // Detect conflicts based on the current state
        List<Conflict> conflicts = detector.detectConflicts(aircrafts);

        // Update positions to simulate motion
        movement.updatePositions(aircrafts);

        if (conflicts.isEmpty()) {
            System.out.println("[STATUS] Airspace clear");
            updateMap(aircrafts);
            return;
        }

        notifyConflicts(conflicts);

        List<Resolution> resolutions = new ArrayList<>();
        List<String> resolvedAircraft = new ArrayList<>();
        int unresolvedCount = 0;

        // Evaluate resolutions while ensuring each aircraft is handled once per cycle
        for (Conflict c : conflicts) {

            Resolution r = resolver.resolveConflict(c, aircrafts);

            if (r == null) {
                unresolvedCount++;
                continue;
            }

            String callsign = r.getAircraft().getCallsign();

            if (resolvedAircraft.contains(callsign)) {
                continue;
            }

            resolutions.add(r);
            resolvedAircraft.add(callsign);
        }

        // Apply all valid resolutions together
        for (Resolution r : resolutions) {
            r.getAircraft().setAltitude(r.getNewAltitude());
        }

        if (resolutions.isEmpty()) {
            System.out.println("[WARNING] Conflicts detected but no valid resolution found");
        } else if (unresolvedCount > 0) {
            System.out.println("[WARNING] Some conflicts remain unresolved");
        }

        updateMap(aircrafts);
    }

    private void notifyConflicts(List<Conflict> conflicts) {
        WYJAppController ctrl = WYJAppController.getInstance();
        if (ctrl == null) return;
        for (Conflict c : conflicts) {
            ctrl.markConflict(c.getA1().getCallsign(), c.getA2().getCallsign());
            ctrl.logToMap("CONFLICT: " + c.getA1().getCallsign() + " / " + c.getA2().getCallsign());
        }
    }

    private void updateMap(List<Aircraft> aircrafts) {
        WYJAppController ctrl = WYJAppController.getInstance();
        if (ctrl == null) return;
        ctrl.updateAllAircraft(aircrafts);
    }
}