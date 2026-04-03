package com.watchyojet;

import java.util.ArrayList;
import java.util.List;

import com.watchyojet.engine.ConflictDetector;
import com.watchyojet.engine.ResolutionEngine;
import com.watchyojet.model.Aircraft;
import com.watchyojet.model.Conflict;
import com.watchyojet.model.Resolution;

public class Main {
    public static void main(String[] args) {

        List<Aircraft> aircrafts = new ArrayList<>();

        // CLOSE aircraft → should trigger conflict
        aircrafts.add(new Aircraft("A1", 0, 0, 30000, 800, 45));
        aircrafts.add(new Aircraft("A2", 1, 1, 30000, 800, 225));

        // FAR aircraft → no conflict
        aircrafts.add(new Aircraft("A3", 100, 100, 30000, 800, 90));

        ConflictDetector detector = new ConflictDetector();
        ResolutionEngine resolver = new ResolutionEngine();

        // STORE conflicts
        List<Conflict> conflicts = detector.detectConflicts(aircrafts);

        //  LOOP through conflicts
        for (Conflict c : conflicts) {

            Resolution r = resolver.resolveConflict(c, aircrafts);

            Aircraft a = r.getAircraft();
            a.setAltitude(r.getNewAltitude());
        }
    }
}