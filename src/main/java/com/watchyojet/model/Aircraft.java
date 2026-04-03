package com.watchyojet.model;

public class Aircraft {

    private AircraftType type;
    private String callsign;
    private double lat;
    private double lon;
    private double altitude;
    private double speed;
    private double heading;

    public Aircraft(String callsign, double lat, double lon,
    double altitude, double speed, double heading,
    AircraftType type) 
    {
    this.callsign = callsign;
    this.lat = lat;
    this.lon = lon;
    this.altitude = altitude;
    this.speed = speed;
    this.heading = heading;
    this.type = type;
    }

    public double getLat()
    {
        return lat; 
    }
    public double getLon()
    { 
        return lon; 
    }
    public double getHeading() 
    {
         return heading;
    }
    public void setHeading(double heading)
    {
        this.heading = heading;
    }
    public double getSpeed() 
    { 
        return speed;
    }

    public void setLat(double lat) 
    {
        this.lat = lat; 
    }
    public void setLon(double lon)
    {
         this.lon = lon; 
    }

    public String getCallsign() 
    {
        return callsign;
    }
    public double getAltitude() 
    {
        return altitude;
    }
    public void setAltitude(double altitude) 
    {
    this.altitude = altitude;
    }
    public AircraftType getType() 
    {
    return type;
    }

    public AircraftCategory getCategory()
    {
    return type.getCategory();
    }
}