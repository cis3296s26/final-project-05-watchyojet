package com.watchyojet.model;

public class Resolution {

    public enum Type { ALTITUDE, HEADING, SPEED }

    private final Aircraft aircraft;
    private final double   newAltitude;
    private final double   newHeading;
    private final double   newSpeed;
    private final Type     type;

    public Resolution(Aircraft aircraft, double newAltitude) {
        this.aircraft    = aircraft;
        this.newAltitude = newAltitude;
        this.newHeading  = Double.NaN;
        this.newSpeed    = Double.NaN;
        this.type        = Type.ALTITUDE;
    }

    public static Resolution forHeading(Aircraft aircraft, double newHeading) {
        return new Resolution(aircraft, aircraft.getAltitude(), newHeading, Double.NaN, Type.HEADING);
    }

    public static Resolution forSpeed(Aircraft aircraft, double newSpeed) {
        return new Resolution(aircraft, aircraft.getAltitude(), Double.NaN, newSpeed, Type.SPEED);
    }

    private Resolution(Aircraft aircraft, double alt, double hdg, double spd, Type type) {
        this.aircraft    = aircraft;
        this.newAltitude = alt;
        this.newHeading  = hdg;
        this.newSpeed    = spd;
        this.type        = type;
    }

    public Aircraft getAircraft()         { return aircraft; }
    public double   getNewAltitude()      { return newAltitude; }
    public double   getNewHeading()       { return newHeading; }
    public double   getNewSpeed()         { return newSpeed; }
    public boolean  isHeadingResolution() { return type == Type.HEADING; }
    public boolean  isSpeedResolution()   { return type == Type.SPEED; }
}
