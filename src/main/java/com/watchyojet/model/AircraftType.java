package com.watchyojet.model;

public enum AircraftType {

    A320(39000, 2500, AircraftCategory.COMMERCIAL),
    B737(41000, 2400, AircraftCategory.COMMERCIAL),
    A350(43000, 2000, AircraftCategory.COMMERCIAL),

    F16(50000, 5000, AircraftCategory.MILITARY),

    C172(13000, 700, AircraftCategory.GENERAL_AVIATION),

    AIR_AMBULANCE(35000, 2200, AircraftCategory.EMERGENCY),

    GENERIC(35000, 2000, AircraftCategory.GENERAL_AVIATION);

    private final double maxAltitude;
    private final double climbRate;
    private final AircraftCategory category;

    AircraftType(double maxAltitude, double climbRate, AircraftCategory category) {
        this.maxAltitude = maxAltitude;
        this.climbRate = climbRate;
        this.category = category;
    }

    public double getMaxAltitude() {
        return maxAltitude;
    }

    public double getClimbRate() {
        return climbRate;
    }

    public AircraftCategory getCategory() {
        return category;
    }
}