package com.ecp.gsy.dcs.zirkapp.app.util.beans;

/**
 * Created by elcapi05 on 26/08/2014.
 */
public class ItemListDrawer {

    private int Icono;
    private String Titulo;
    private Integer cantNotificaciones;

    public ItemListDrawer(String titulo, int icono, Integer cantNotificaciones) {
        this.Icono = icono;
        this.Titulo = titulo;
        this.cantNotificaciones = cantNotificaciones;
    }

    //<editor-fold desc="GETTERS">
    public int getIcono() {
        return Icono;
    }

    public String getTitulo() {
        return Titulo;
    }

    public Integer getCantNotificaciones() {
        return cantNotificaciones;
    }
    //</editor-fold>

    //<editor-fold desc="SETTERS">
    public void setIcono(int icono) {
        Icono = icono;
    }

    public void setTitulo(String titulo) {
        Titulo = titulo;
    }

    public void setCantNotificaciones(Integer cantNotificaciones) {
        this.cantNotificaciones = cantNotificaciones;
    }

    //</editor-fold>
}
