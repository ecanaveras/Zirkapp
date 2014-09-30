package com.ecp.gsy.dcs.zirkapp.app.util.beans;

/**
 * Created by elcapi05 on 26/08/2014.
 */
public class ItemListDrawer {

    private int Icono;
    private String Titulo;

    public ItemListDrawer(String titulo, int icono) {
        Icono = icono;
        Titulo = titulo;
    }

    //<editor-fold desc="GETTERS">
    public int getIcono() {
        return Icono;
    }

    public String getTitulo() {
        return Titulo;
    }
    //</editor-fold>

    //<editor-fold desc="SETTERS">
    public void setIcono(int icono) {
        Icono = icono;
    }

    public void setTitulo(String titulo) {
        Titulo = titulo;
    }
    //</editor-fold>
}
