package com.ecp.gsy.dcs.zirkapp.app.util.beans;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Created by Elder on 22/07/2014.
 */
public class Zimess {

    private int zid;
    private String zmessage;
    private String zuser;
    private Double zlongi;
    private Double zlati;
    private boolean isUpdate;
    private Double timeInicial;
    private Double timeFinal;
    private String fechaCreated;

    //Obtener time pass
    public String getTimePass() {
        String timer = "0";
        long diff = 0,
                seconds = 0,
                minutes = 0,
                hours = 0,
                days = 0;
        if (fechaCreated != null) {
            try {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                simpleDateFormat.setTimeZone(TimeZone.getDefault());
                Date fcreate = simpleDateFormat.parse(fechaCreated);
                //TODO Obtener la hora de la base de datos
                Date fSystem = simpleDateFormat.parse(simpleDateFormat.format(new Date()));
                diff = fSystem.getTime() - fcreate.getTime();
                seconds = TimeUnit.MILLISECONDS.toSeconds(diff);
                minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
                hours = TimeUnit.MILLISECONDS.toHours(diff);
                days = TimeUnit.MILLISECONDS.toDays(diff);
            } catch (ParseException e) {
                Log.e("Date parse error:", e.getLocalizedMessage());
            }
        }
        if (seconds <= 59) {
            timer = new StringBuffer("+").append(seconds).append(" s.").toString();
        }
        if (minutes > 0 && minutes <= 59) {
            timer = new StringBuffer("+").append(minutes).append(" m.").toString();
        }
        if (hours > 0 && hours <= 24) {
            timer = new StringBuffer("+").append(hours).append(" h.").toString();
        }
        if (days > 0) {
            timer = new StringBuffer("+").append(days).append(" d.").toString();
        }
        return "Hace " + timer;
    }


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

    public Double getZlongi() {
        return zlongi;
    }

    public Double getZlati() {
        return zlati;
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

    public String getFechaCreated() {
        return fechaCreated;
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

    public void setZlongi(Double zlongi) {
        this.zlongi = zlongi;
    }

    public void setZlati(Double zlati) {
        this.zlati = zlati;
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

    public void setFechaCreated(String fechaCreated) {
        this.fechaCreated = fechaCreated;
    }

    //</editor-fold>
}
