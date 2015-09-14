package com.ecp.gsy.dcs.zirkapp.app.util.parse.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * Created by ecanaveras on 13/09/2015.
 */
@ParseClassName("ParseZFavorite")
public class ParseZFavorite extends ParseObject {

    public static final String ZIMESS_ID = "zimessId";
    public static final String USER = "user";

    //<editor-fold desc="METHODS GETTERS">
    public ParseUser getUser() {
        return getParseUser(USER);
    }

    public ParseObject getZimessId() {
        return getParseObject(ZIMESS_ID);
    }
    //</editor-fold>

    //<editor-fold desc="METHODS SETTERS">
    public void setUser(ParseUser user) {
        put(USER, user);
    }

    public void setZimessId(ParseObject zimessId) {
        put(ZIMESS_ID, zimessId);
    }
    //</editor-fold>
}
