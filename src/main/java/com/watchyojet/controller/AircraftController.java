package com.watchyojet.controller;

import com.watchyojet.manager.AircraftManager;
import com.watchyojet.model.Aircraft;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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


    // GET/api/aircraft
    // Returns the current state of all active aircrafts as a JSOn array
    @GetMapping
    public ResponseEntity<List<Aircraft>> getAll() {
        return ResponseEntity.ok(manager.getAircrafts());
    }

}