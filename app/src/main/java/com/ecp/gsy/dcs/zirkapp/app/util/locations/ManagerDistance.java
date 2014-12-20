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

    public Double getDistancia() {
        //distancia (A, B) = R * arccos (sen (LATA) * sen (LATB) + cos (lata) * cos (LATB) * cos (LonA-LonB))
        double vlatA, vlatB, vlongA, vlongB, dlong, dlat, a, b, c;
        vlatA = Math.toRadians(ubicacionPhone.getLatitud());
        vlongA = Math.toRadians(ubicacionPhone.getLongitud());
        vlatB = Math.toRadians(ubicacionZmsg.getLatitud());
        vlongB = Math.toRadians(ubicacionZmsg.getLongitud());
        distancia = (RADIO * Math.acos(Math.sin(vlatA) * Math.sin(vlatB) + Math.cos(vlatA) * Math.cos(vlatB) * Math.cos(vlongA - vlongB)));
        distancia *= 1000;
        return roundDistancia(Math.round(distancia));
    }

    public String getDistanciaToString() {
        Double distance = getDistancia();
        if (distance.intValue() >= 1000) {
            //Kilometros
            return "+ " + distance / 1000 + "Km";
        } else if (distance.intValue() < 100) {
            // Metros
            return "- 99m";
        } else {
            // Metros
            return "+ " + distance.intValue() + "m";
        }

    }

    /**
     * Redondea la distancia ej:
     * <b>20 == 50</b>
     * <b>67 == 100</b>
     * <b>1230 == 1250</b>
     *
     * @param distance
     * @return
     */
    private Double roundDistancia(Long distance) {
        Double result = new Double((distance - 50) / 100 * 100);//Redondear las distancias a centenas
        return result;
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
