package com.ecp.gsy.dcs.zirkapp.app.util.locations;

/**
 * Created by Elder on 02/06/2014.
 */
public class Location {

    private double latitud;
    private double longitud;

    public Location(double latitud, double longitud) {
        this.latitud = latitud;
        this.longitud = longitud;
    }

    //<editor-fold desc="METHODS GETTERS">
    public double getLatitud() {
        return latitud;
    }

    public double getLongitud() {
        return longitud;
    }
    //</editor-fold>

    //<editor-fold desc="METHODS SETTERS">
    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }
    //</editor-fold>
}

