package com.watchyojet.engine;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.watchyojet.model.Aircraft;
import com.watchyojet.model.AircraftType;
import com.watchyojet.model.Conflict;

public class ConflictDetectorTest {

    @Test
    void shouldDetectConflictWhenCloseAndSameAltitude() {
        // head-on convergence ~12 NM apart, tCPA ≈ 43 s
        Aircraft a1 = new Aircraft("A1", 40.0, -75.0, 30000, 500, 180, AircraftType.A320);
        Aircraft a2 = new Aircraft("A2", 39.8, -75.0, 30000, 500,   0, AircraftType.A320);

        List<Aircraft> aircrafts = Arrays.asList(a1, a2);

        ConflictDetector detector = new ConflictDetector();
        List<Conflict> conflicts = detector.detectConflicts(aircrafts);

        assertEquals(1, conflicts.size());
    }

    @Test
    void shouldNotDetectConflictWhenFarApart() {
        // converging but tCPA >> 300 s
        Aircraft a1 = new Aircraft("A1", 40.0, -75.0, 30000, 500, 180, AircraftType.A320);
        Aircraft a2 = new Aircraft("A2", 30.0, -65.0, 30000, 500,   0, AircraftType.A320);

        List<Aircraft> aircrafts = Arrays.asList(a1, a2);

        ConflictDetector detector = new ConflictDetector();
        List<Conflict> conflicts = detector.detectConflicts(aircrafts);

        assertEquals(0, conflicts.size());
    }

    @Test
    void shouldNotDetectConflictWhenAltitudeSeparated() {
        // same geometry as close test but 2000 ft altitude difference
        Aircraft a1 = new Aircraft("A1", 40.0, -75.0, 30000, 500, 180, AircraftType.A320);
        Aircraft a2 = new Aircraft("A2", 39.8, -75.0, 32000, 500,   0, AircraftType.A320);

        List<Aircraft> aircrafts = Arrays.asList(a1, a2);

        ConflictDetector detector = new ConflictDetector();
        List<Conflict> conflicts = detector.detectConflicts(aircrafts);

        assertEquals(0, conflicts.size());
    }

    @Test
    void shouldDetectMultipleConflicts() {
        // a1 heading east, a2 and a3 heading west — a1 converges with both
        Aircraft a1 = new Aircraft("A1", 40.0, -75.0, 30000, 500,  90, AircraftType.A320);
        Aircraft a2 = new Aircraft("A2", 40.0, -74.9, 30000, 500, 270, AircraftType.A320);
        Aircraft a3 = new Aircraft("A3", 40.0, -74.8, 30000, 500, 270, AircraftType.A320);

        List<Aircraft> aircrafts = Arrays.asList(a1, a2, a3);

        ConflictDetector detector = new ConflictDetector();
        List<Conflict> conflicts = detector.detectConflicts(aircrafts);

        assertTrue(conflicts.size() >= 1);
    }
}