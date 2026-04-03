package com.watchyojet.engine;

import java.util.List;

import com.watchyojet.model.Aircraft;
import com.watchyojet.model.Conflict;
import com.watchyojet.model.Resolution;
import com.watchyojet.simulation.MovementEngine;

public class ATCEngine {

    private ConflictDetector detector;
    private ResolutionEngine resolver;
    private MovementEngine movement;

    public ATCEngine() {
        this.detector = new ConflictDetector();
        this.resolver = new ResolutionEngine();
        this.movement = new MovementEngine();
    }

    public void runCycle(List<Aircraft> aircrafts) {

        System.out.println("\n--- ATC Cycle ---");

        // MOVE aircraft FIRST
        movement.updatePositions(aircrafts);

        //  Detect conflicts
        List<Conflict> conflicts = detector.detectConflicts(aircrafts);

        if (conflicts.isEmpty()) {
            System.out.println("No conflicts");
            return;
        }

        // Resolve conflicts
        for (Conflict c : conflicts) {

            Resolution r = resolver.resolveConflict(c, aircrafts);

            if (r == null) continue;

            Aircraft a = r.getAircraft();
            a.setAltitude(r.getNewAltitude());
        }
    }
}