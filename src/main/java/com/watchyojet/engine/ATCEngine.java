package com.watchyojet.engine;

import java.util.List;

import com.watchyojet.model.Aircraft;
import com.watchyojet.model.Conflict;
import com.watchyojet.model.Resolution;

public class ATCEngine {

    private ConflictDetector detector;
    private ResolutionEngine resolver;

    public ATCEngine() {
        this.detector = new ConflictDetector();
        this.resolver = new ResolutionEngine();
    }

    public void runCycle(List<Aircraft> aircrafts) {

        System.out.println("\n--- ATC Cycle ---");

        // 1. Detect conflicts
        List<Conflict> conflicts = detector.detectConflicts(aircrafts);

        // 2. Resolve conflicts
        for (Conflict c : conflicts) {

            Resolution r = resolver.resolveConflict(c, aircrafts);

            Aircraft a = r.getAircraft();
            a.setAltitude(r.getNewAltitude());
        }
    }
}