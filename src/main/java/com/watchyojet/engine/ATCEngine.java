package com.watchyojet.engine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        this.detector  = new ConflictDetector();
        this.resolver  = new ResolutionEngine();
        this.movement  = new MovementEngine();
        this.predictor = new TrajectoryPredictor();
    }

    public void runCycle(List<Aircraft> aircrafts) {

        // 1. Move first — detect on post-movement positions so CPA and
        //    resolution both operate on the same aircraft state.
        movement.updatePositions(aircrafts);

        // 2. Detect conflicts on the updated positions.
        List<Conflict> conflicts = detector.detectConflicts(aircrafts);

        if (conflicts.isEmpty()) {
            updateMap(aircrafts);
            return;
        }

        List<Resolution>  resolutions          = new ArrayList<>();
        List<String[]>    resolvedConflictPairs = new ArrayList<>();
        List<String[]>    unresolvedConflictPairs = new ArrayList<>();
        // Track which aircraft have already received an altitude assignment
        // this cycle so we don't issue two conflicting instructions.
        Set<String> resolvedCallsigns = new HashSet<>();

        for (Conflict c : conflicts) {
            Resolution r = resolver.resolveConflict(c, aircrafts);
            String cs1 = c.getA1().getCallsign();
            String cs2 = c.getA2().getCallsign();

            if (r == null) {
                unresolvedConflictPairs.add(new String[]{cs1, cs2});
                continue;
            }

            String cs = r.getAircraft().getCallsign();

            if (resolvedCallsigns.contains(cs)) {
                // Primary aircraft already has an altitude assignment this cycle.
                // Try moving the other aircraft instead.
                Aircraft other = (r.getAircraft() == c.getA1()) ? c.getA2() : c.getA1();
                if (resolvedCallsigns.contains(other.getCallsign())) {
                    // Both aircraft already resolved — nothing more to do for this pair.
                    continue;
                }
                r = resolver.resolveForAircraft(other, aircrafts);
                if (r == null) {
                    unresolvedConflictPairs.add(new String[]{cs1, cs2});
                    continue;
                }
                cs = r.getAircraft().getCallsign();
            }

            resolutions.add(r);
            resolvedCallsigns.add(cs);
            resolvedConflictPairs.add(new String[]{
                cs1, cs2,
                r.getAircraft().getCallsign(),
                String.valueOf((int) r.getNewAltitude())
            });
        }

        // 3. Apply all resolutions.
        for (Resolution r : resolutions) {
            r.getAircraft().setAltitude(r.getNewAltitude());
            System.out.println("[RESOLVED] " + r.getAircraft().getCallsign()
                    + " → " + (int) r.getNewAltitude() + " ft");
        }

        if (!unresolvedConflictPairs.isEmpty()) {
            System.out.println("[UNRESOLVED] " + unresolvedConflictPairs.size()
                    + " conflict(s) could not be resolved");
        }

        notifyConflicts(conflicts, resolvedConflictPairs);
        updateMap(aircrafts);
    }

    private void notifyConflicts(List<Conflict> conflicts, List<String[]> resolvedConflictPairs) {
        WYJAppController ctrl = WYJAppController.getInstance();
        if (ctrl == null) return;

        List<String[]> allPairs = new ArrayList<>();
        for (Conflict c : conflicts) {
            allPairs.add(new String[]{c.getA1().getCallsign(), c.getA2().getCallsign()});
        }

        ctrl.batchNotify(allPairs, resolvedConflictPairs);
    }

    private void updateMap(List<Aircraft> aircrafts) {
        WYJAppController ctrl = WYJAppController.getInstance();
        if (ctrl == null) return;
        ctrl.updateAllAircraft(aircrafts);
    }
}
