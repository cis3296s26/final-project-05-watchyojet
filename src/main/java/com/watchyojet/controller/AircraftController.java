package com.watchyojet.controller;

import com.watchyojet.manager.AircraftManager;
import com.watchyojet.model.Aircraft;
import com.watchyojet.model.AircraftType;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;
import java.util.Map;

// Spring Annotation that marks class as a controller.
// Every method directly returns data in JSON format
@RestController

// Used to map web requests to specific controller classes
// In this case, all endpoints are prefixed to /api/aircraft
@RequestMapping("/api/aircraft")

// Accepts traffic from any domain
@CrossOrigin(origins = "*")

public class AircraftController {

    private final AircraftManager manager;

    public AircraftController(AircraftManager manager) {
        this.manager = manager;
    }


    // GET /api/aircraft
    // Returns the current state of all active aircrafts as a JSON array
    @GetMapping
    public ResponseEntity<List<Aircraft>> getAll() {
        return ResponseEntity.ok(manager.getAircrafts());
    }


    // POST /api/aircraft
    // Adds a new aircraft to the simulation
    @PostMapping
    public ResponseEntity<?> addAircraft(@RequestBody Map<String, Object> body) {
        try {

            String callsign = (String) body.get("callsign");
            double lat      = toDouble(body.get("lat"));
            double lon      = toDouble(body.get("lon"));
            double altitude = toDouble(body.get("altitude"));
            double speed    = toDouble(body.get("speed"));
            double heading  = toDouble(body.get("heading"));
            String typeName = (String) body.get("type");

            // Validate callsign is present
            if (callsign == null || callsign.isBlank()) {
                return ResponseEntity.badRequest().body("'callsign' is required");
            }

            // Checking for duplicates
            boolean duplicate = manager.getAircrafts().stream()
                    .anyMatch(a -> a.getCallsign().equalsIgnoreCase(callsign));
            if (duplicate) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Aircraft '" + callsign + "' already exists");
            }

            // Validate if aircraft type is in AircraftType enum
            AircraftType type;
            try {
                type = AircraftType.valueOf(typeName.toUpperCase());
            } catch (Exception e) {
                return ResponseEntity.badRequest()
                        .body("Unknown type '" + typeName + "'. Valid: " + List.of(AircraftType.values()));
            }

            // Create and register aircraft
            Aircraft aircraft = new Aircraft(callsign, lat, lon, altitude,
                                            speed, heading, type);
            manager.addAircraft(aircraft);
            return ResponseEntity.status(HttpStatus.CREATED).body("Aircraft " + callsign + " added");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Bad request: " + e.getMessage());
        }
    }


    // Convets JSON number into a Java double
    private double toDouble(Object v) {
        if (v instanceof Number n) {
            return n.doubleValue();
        }
        throw new IllegalArgumentException("Expected a number, got: " + v);
    }

}