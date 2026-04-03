package com.watchyojet.model;

public class Resolution {

    private Aircraft aircraft;
    private double newAltitude;

    public Resolution(Aircraft aircraft, double newAltitude) {
        this.aircraft = aircraft;
        this.newAltitude = newAltitude;
    }

    public Aircraft getAircraft() {
        return aircraft;
    }

    public double getNewAltitude() {
        return newAltitude;
    }
}