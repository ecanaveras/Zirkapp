package com.ecp.gsy.dcs.zirkapp.app.util.locations;

/**
 * Created by Elder on 02/06/2014.
 */
public class ManagerDistance {

    private static final Double RADIO = 6371.795477598;
    private Double distancia;
    private Location ubicacionPhone; //Ubicacion Smartphone
    private Location ubicacionZmsg; //Ubicacion Publicacion

    public ManagerDistance(Location miUbicacion, Location ubicacionZmsg) {
        this.ubicacionPhone = miUbicacion;
        this.ubicacionZmsg = ubicacionZmsg;
    }

    public Integer getDistancia() {
        //distancia (A, B) = R * arccos (sen (LATA) * sen (LATB) + cos (lata) * cos (LATB) * cos (LonA-LonB))
        double vlatA, vlatB, vlongA, vlongB, dlong, dlat,a, b ,c;
        vlatA = Math.toRadians(ubicacionPhone.getLatitud());
        vlongA = Math.toRadians(ubicacionPhone.getLongitud());
        vlatB = Math.toRadians(ubicacionZmsg.getLatitud());
        vlongB = Math.toRadians(ubicacionZmsg.getLongitud());
        distancia = (RADIO * Math.acos(Math.sin(vlatA) * Math.sin(vlatB) + Math.cos(vlatA) * Math.cos(vlatB) * Math.cos(vlongA - vlongB)));
        distancia *=  1000;
        /*
        dlong = vlongB - vlongA;
        dlat = vlatB - vlatA;
        a = Math.sin(Math.pow((dlat/2),2)) + Math.cos(vlatA) * Math.cos(vlatB) * Math.sin(Math.pow((dlong/2),2));
        c = 2 * Math.asin(Math.min(1,Math.sqrt(a)));
        distancia = RADIO * c;
        */
        return distancia.intValue();
    }

    //<editor-fold desc="METHODS GETTERS">
    public Location getUbicacionPhone() {
        return ubicacionPhone;
    }

    public Location getUbicacionZmsg() {
        return ubicacionZmsg;
    }
    //</editor-fold>

    //<editor-fold desc="METHODS SETTERS">
    public void setUbicacionPhone(Location ubicacionPhone) {
        this.ubicacionPhone = ubicacionPhone;
    }

    public void setUbicacionZmsg(Location ubicacionZmsg) {
        this.ubicacionZmsg = ubicacionZmsg;
    }
    //</editor-fold>
}
