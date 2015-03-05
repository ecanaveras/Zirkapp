package com.ecp.gsy.dcs.zirkapp.app.util.beans;

/**
 * Created by elcapi05 on 26/08/2014.
 */
public class ItemListDrawer {

    private int Icono;
    private String Titulo;
    private Integer cantNotificaciones;
    private String textSeparador;
    private boolean showSeparator = false;

    public ItemListDrawer(String titulo, int icono) {
        this.Icono = icono;
        this.Titulo = titulo;
    }

    public ItemListDrawer(String titulo, int icono, String textSeparator) {
        this.Icono = icono;
        this.Titulo = titulo;
        this.showSeparator = textSeparator != null;
        this.textSeparador = textSeparator;
    }

    public ItemListDrawer(String titulo, int icono, Integer cantNotificaciones) {
        this.Icono = icono;
        this.Titulo = titulo;
        this.cantNotificaciones = cantNotificaciones;
    }

    public ItemListDrawer(String titulo, int icono, Integer cantNotificaciones, String textSeparator) {
        this.Icono = icono;
        this.Titulo = titulo;
        this.cantNotificaciones = cantNotificaciones;
        this.showSeparator = textSeparator != null;
        this.textSeparador = textSeparator;
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

    public boolean isShowSeparator() {
        return showSeparator;
    }

    public String getTextSeparador() {
        return textSeparador;
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

    public void setShowSeparator(boolean showSeparator) {
        this.showSeparator = showSeparator;
    }

    public void setTextSeparador(String textSeparador) {
        this.textSeparador = textSeparador;
    }

    //</editor-fold>
}
