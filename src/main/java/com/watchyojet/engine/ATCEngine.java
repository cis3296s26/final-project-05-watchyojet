package com.watchyojet.engine;

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

        System.out.println("\n--- ATC Cycle ---");
        System.out.println("ATC Engine processing aircraft:");
for (Aircraft a : aircrafts) {
    System.out.println(a.getCallsign());
}
    

        //  Detect conflicts
        List<Conflict> conflicts = detector.detectConflicts(aircrafts);
        movement.updatePositions(aircrafts);

        if (conflicts.isEmpty()) {
            System.out.println("[STATUS] Airspace clear");
            return;
        }

        // Resolve conflicts
        for (Conflict c : conflicts) {

            Resolution r = resolver.resolveConflict(c, aircrafts);

            if (r == null) continue;

            Aircraft a = r.getAircraft();
            a.setAltitude(r.getNewAltitude());
        }

        //UPDATE AIRCRAFT POSITIONS ON MAP
       for (Aircraft aircraft : aircrafts) {
    if (WYJAppController.getInstance() != null) {
        WYJAppController.getInstance().updateMapPoint(
                aircraft.getCallsign(),
                aircraft.getLat(),
                aircraft.getLon()
        );
    }
}
    }
}


/*
        //FUTURE TESTING
        double futureSeconds = 0.0003;
        System.out.println("\n--------------\nSearching for conflict "+futureSeconds+" seconds into the future.");

        List<Aircraft> futureAircraft = predictor.predict(aircrafts, futureSeconds);
        List<Conflict> futureConflicts = detector.detectConflicts(futureAircraft);

        if (futureConflicts.isEmpty()) {
            System.out.println("No FUTURE conflicts");
            return;
        }

        // Resolve conflicts
        for (Conflict c : futureConflicts) {

            Resolution r = resolver.resolveConflict(c, aircrafts);

            if (r == null) continue;

            Aircraft a = r.getAircraft();
            a.setAltitude(r.getNewAltitude());
        }*/
 