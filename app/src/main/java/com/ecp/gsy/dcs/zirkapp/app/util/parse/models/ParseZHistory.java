package com.ecp.gsy.dcs.zirkapp.app.util.parse.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * Created by Elder on 01/07/2015.
 */
@ParseClassName("ParseZHistory")
public class ParseZHistory extends ParseObject {

    public static final String SINCH_ID = "sinchId";
    public static final String ZMESSAGE_ID = "zmessageId";
    public static final String USER = "user";

    //<editor-fold desc="METHODS GETTERS">
    public String getSinchId() {
        return getString(SINCH_ID);
    }

    public ParseZMessage getZMessageId() {
        return (ParseZMessage) getParseObject(ZMESSAGE_ID);
    }

    public ParseUser getUser() {
        return getParseUser(USER);
    }
    //</editor-fold>

    //<editor-fold desc="METHODS SETTERS">
    public void setZMessageId(ParseZMessage zMessageId) {
        put(ZMESSAGE_ID, zMessageId);
    }

    public void setSinchId(String sinchId) {
        put(SINCH_ID, sinchId);
    }

    public void setUser(ParseUser user) {
        put(USER, user);
    }
    //</editor-fold>

}
