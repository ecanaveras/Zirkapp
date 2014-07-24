package com.ecp.gsy.dcs.zirkapp.app.util.beans;

/**
 * Created by Elder on 22/07/2014.
 */
public class Zimess {

    private int zid;
    private String zmessage;
    private String zuser;
    private Double zlong;
    private Double zlat;
    private boolean isUpdate;
    private Double timeInicial;
    private Double timeFinal;


    //<editor-fold desc="METHODS GETTERS">
    public String getZmessage() {
        return zmessage;
    }

    public String getZuser() {
        return zuser;
    }

    public int getZid() {
        return zid;
    }

    public Double getZlong() {
        return zlong;
    }

    public Double getZlat() {
        return zlat;
    }

    public boolean isUpdate() {
        return isUpdate;
    }

    public Double getTimeInicial() {
        return timeInicial;
    }

    public Double getTimeFinal() {
        return timeFinal;
    }
    //</editor-fold>

    //<editor-fold desc="METHODS SETTERS">
    public void setZmessage(String zmessage) {
        this.zmessage = zmessage;
    }

    public void setZuser(String zuser) {
        this.zuser = zuser;
    }

    public void setZid(int zid) {
        this.zid = zid;
    }

    public void setZlong(Double zlong) {
        this.zlong = zlong;
    }

    public void setZlat(Double zlat) {
        this.zlat = zlat;
    }

    public void setUpdate(boolean isUpdate) {
        this.isUpdate = isUpdate;
    }

    public void setTimeInicial(Double timeInicial) {
        this.timeInicial = timeInicial;
    }

    public void setTimeFinal(Double timeFinal) {
        this.timeFinal = timeFinal;
    }

    //</editor-fold>
}
