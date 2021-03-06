package com.ecp.gsy.dcs.zirkapp.app.util.parse.models;

import android.support.v4.graphics.drawable.RoundedBitmapDrawable;

import com.ecp.gsy.dcs.zirkapp.app.GlobalApplication;
import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Elder on 04/07/2015.
 */
@ParseClassName("ParseZimess")
public class ParseZimess extends ParseObject {

    public static final String USER = "user";
    public static final String CANT_COMMENT = "cant_comment";
    public static final String CANT_FAVORITE = "cant_favorite";
    public static final String LOCATION = "location";
    public static final String ZIMESS_TEXT = "zimessText";
    public static final String FAVORITES = "favorites";

    private boolean myFavorite = false;
    private String descDistancia;
    private Double valueDistancia;

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

    public int getCantFavorite() {
        return getInt(CANT_FAVORITE);
    }

    public List<String> getFavorites() {
        return getList(FAVORITES);
    }

    public String getDescDistancia() {
        if (descDistancia != null) {
            return descDistancia;
        } else {
            return "";
        }
    }

    public Double getValueDistancia() {
        return valueDistancia;
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

    public void setCantFavorite(int cant_favorite) {
        put(CANT_FAVORITE, cant_favorite);
    }

    public void addFavorites(String objectId) {
        addUnique(FAVORITES, objectId);
    }

    public void removeFavorites(List<String> list) {
        removeAll(FAVORITES, list);
    }

    public void setDescDistancia(String descDistancia) {
        this.descDistancia = descDistancia;
    }

    public void setValueDistancia(Double valueDistancia) {
        this.valueDistancia = valueDistancia;
    }

    //</editor-fold>

    public boolean isMyFavorite(String currentId) {
        if (getFavorites() != null && currentId != null) {
            this.myFavorite = getFavorites().contains(currentId);
        }
        return this.myFavorite;
    }
}
