package com.ecp.gsy.dcs.zirkapp.app.util.parse.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * Created by Elder on 04/07/2015.
 */
@ParseClassName("ParseZVisit")
public class ParseZVisit extends ParseObject {

    public static final String COUNT_VISIT = "count_visit";
    public static final String USER = "user";

    private int countVisit;
    private ParseUser user;

    //<editor-fold desc="METHODS GETTERS">
    public int getCountVisit() {
        return getInt(COUNT_VISIT);
    }

    public ParseUser getUser() {
        return getParseUser(USER);
    }
    //</editor-fold>

    //<editor-fold desc="METHODS SETTERS">
    public void setCountVisit(int countVisit) {
        put(COUNT_VISIT, countVisit);
    }

    public void setUser(ParseUser user) {
        put(USER, user);
    }
    //</editor-fold>
}
