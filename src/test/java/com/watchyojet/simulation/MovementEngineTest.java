package com.watchyojet.simulation;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.watchyojet.model.Aircraft;
import com.watchyojet.model.AircraftType;

public class MovementEngineTest {

    private static final double EPSILON = 0.000001;

    @Test
    void shouldMoveAircraft() {

        // Arrange
        Aircraft a = new Aircraft(
                "A1",
                0,
                0,
                30000,
                1000,
                0,
                AircraftType.A320
        );

        List<Aircraft> aircrafts = List.of(a);
        MovementEngine engine = new MovementEngine();

        double oldLat = a.getLat();
        double oldLon = a.getLon();

        // Act
        engine.updatePositions(aircrafts);

        // Calculate movement distance
        double distanceMoved = Math.sqrt(
                Math.pow(a.getLat() - oldLat, 2) +
                Math.pow(a.getLon() - oldLon, 2)
        );

        // Assert
        assertTrue(distanceMoved > EPSILON, "Aircraft should have moved");
    }

    @Test
    void shouldMoveAircraftDiagonally() {

        // Arrange
        Aircraft a = new Aircraft(
                "A1",
                0,
                0,
                30000,
                1000,
                45,
                AircraftType.A320
        );

        List<Aircraft> aircrafts = List.of(a);
        MovementEngine engine = new MovementEngine();

        double oldLat = a.getLat();
        double oldLon = a.getLon();

        // Act
        engine.updatePositions(aircrafts);

        // Assert
        boolean latChanged = Math.abs(oldLat - a.getLat()) > EPSILON;
        boolean lonChanged = Math.abs(oldLon - a.getLon()) > EPSILON;

        assertTrue(latChanged, "Latitude should change");
        assertTrue(lonChanged, "Longitude should change");
    }
}