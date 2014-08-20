package com.ecp.gsy.dcs.zirkapp.app.util.beans;

import android.util.Log;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Created by Elder on 22/07/2014.
 */
public class Zimess implements Serializable{

    private int id;
    private String zimess;
    private String usuario;
    private Double longitud;
    private Double latitud;
    private boolean isUpdate;
    private int minutosDuracion;
    //private Double timeInicial;
    //private Double timeFinal;
    private Date fechaCreated;
    private Date fechaUpdate;

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
                Date fcreate = fechaCreated;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Zimess)) return false;

        Zimess zimess = (Zimess) o;

        if (id != zimess.id) return false;
        if (!this.zimess.equals(zimess.zimess)) return false;
        if (!usuario.equals(zimess.usuario)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + zimess.hashCode();
        result = 31 * result + usuario.hashCode();
        return result;
    }

    //<editor-fold desc="METHODS GETTERS">

    public int getId() {
        return id;
    }

    public String getZimess() {
        return zimess;
    }

    public String getUsuario() {
        return usuario;
    }

    public Double getLongitud() {
        return longitud;
    }

    public Double getLatitud() {
        return latitud;
    }

    public boolean isUpdate() {
        return isUpdate;
    }

    public int getMinutosDuracion() {
        return minutosDuracion;
    }

    /*
    public Double getTimeInicial() {
        return timeInicial;
    }

    public Double getTimeFinal() {
        return timeFinal;
    }*/

    public Date getFechaCreated() {
        return fechaCreated;
    }

    public Date getFechaUpdate() {
        return fechaUpdate;
    }


    //</editor-fold>

    //<editor-fold desc="METHODS SETTERS">

    public void setId(int id) {
        this.id = id;
    }

    public void setZimess(String zimess) {
        this.zimess = zimess;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public void setUpdate(boolean isUpdate) {
        this.isUpdate = isUpdate;
    }

    public void setMinutosDuracion(int minutosDuracion) {
        this.minutosDuracion = minutosDuracion;
    }

    /*
        public void setTimeInicial(Double timeInicial) {
            this.timeInicial = timeInicial;
        }

        public void setTimeFinal(Double timeFinal) {
            this.timeFinal = timeFinal;
        }
        */
    public void setFechaCreated(Date fechaCreated) {
        this.fechaCreated = fechaCreated;
    }

    public void setFechaUpdate(Date fechaUpdate) {
        this.fechaUpdate = fechaUpdate;
    }


    //</editor-fold>
}
