package com.ecp.gsy.dcs.zirkapp.app.util.parse.models;

import android.support.v4.graphics.drawable.RoundedBitmapDrawable;

import com.ecp.gsy.dcs.zirkapp.app.GlobalApplication;
import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * Created by Elder on 04/07/2015.
 */
@ParseClassName("ParseZimess")
public class ParseZimess extends ParseObject {

    public static final String USER = "user";
    public static final String CANT_COMMENT = "cant_comment";
    public static final String LOCATION = "location";
    public static final String ZIMESS_TEXT = "zimessText";

    private String descDistancia;

    /**
     * Retorna la imagen del usuario
     *
     * @return
     */
    public RoundedBitmapDrawable getAvatar() {
        return GlobalApplication.getAvatar(getUser());
    }

    //<editor-fold desc="METHODS GETTERS">
    public ParseUser getUser() {
        return getParseUser(USER);
    }

    public String getZimessText() {
        return getString(ZIMESS_TEXT);
    }

    public ParseGeoPoint getLocation() {
        return getParseGeoPoint(LOCATION);
    }

    public int getCantComment() {
        return getInt(CANT_COMMENT);
    }

    public String getDescDistancia() {
        return descDistancia;
    }
    //</editor-fold>

    //<editor-fold desc="METHODS SETTERS">
    public void setUser(ParseUser user) {
        put(USER, user);
    }

    public void setZimessText(String zimessText) {
        put(ZIMESS_TEXT, zimessText);
    }

    public void setLocation(ParseGeoPoint location) {
        put(LOCATION, location);
    }

    public void setCantComment(int cant_comment) {
        put(CANT_COMMENT, cant_comment);
    }

    public void setDescDistancia(String descDistancia) {
        this.descDistancia = descDistancia;
    }
    //</editor-fold>
}
