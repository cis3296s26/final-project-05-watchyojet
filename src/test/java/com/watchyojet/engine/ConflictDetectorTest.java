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

        Aircraft a1 = new Aircraft("A1", 0, 0, 30000, 800, 0, AircraftType.A320);
        Aircraft a2 = new Aircraft("A2", 1, 1, 30000, 800, 0, AircraftType.A320);

        List<Aircraft> aircrafts = Arrays.asList(a1, a2);

        ConflictDetector detector = new ConflictDetector();
        List<Conflict> conflicts = detector.detectConflicts(aircrafts);

        assertEquals(1, conflicts.size());
    }

    @Test
    void shouldNotDetectConflictWhenFarApart() {

        Aircraft a1 = new Aircraft("A1", 0, 0, 30000, 800, 0, AircraftType.A320);
        Aircraft a2 = new Aircraft("A2", 100, 100, 30000, 800, 0, AircraftType.A320);

        List<Aircraft> aircrafts = Arrays.asList(a1, a2);

        ConflictDetector detector = new ConflictDetector();
        List<Conflict> conflicts = detector.detectConflicts(aircrafts);

        assertEquals(0, conflicts.size());
    }

    @Test
    void shouldNotDetectConflictWhenAltitudeSeparated() {

        Aircraft a1 = new Aircraft("A1", 0, 0, 30000, 800, 0, AircraftType.A320);
        Aircraft a2 = new Aircraft("A2", 1, 1, 32000, 800, 0, AircraftType.A320);

        List<Aircraft> aircrafts = Arrays.asList(a1, a2);

        ConflictDetector detector = new ConflictDetector();
        List<Conflict> conflicts = detector.detectConflicts(aircrafts);

        assertEquals(0, conflicts.size());
    }

    @Test
    void shouldDetectMultipleConflicts() {

        Aircraft a1 = new Aircraft("A1", 0, 0, 30000, 800, 0, AircraftType.A320);
        Aircraft a2 = new Aircraft("A2", 1, 1, 30000, 800, 0, AircraftType.A320);
        Aircraft a3 = new Aircraft("A3", 2, 2, 30000, 800, 0, AircraftType.A320);

        List<Aircraft> aircrafts = Arrays.asList(a1, a2, a3);

        ConflictDetector detector = new ConflictDetector();
        List<Conflict> conflicts = detector.detectConflicts(aircrafts);

        assertTrue(conflicts.size() >= 1);
    }
}