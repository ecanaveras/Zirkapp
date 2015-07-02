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
        return getString("sinchId");
    }

    public ParseZMessage getZMessageId() {
        return (ParseZMessage) getParseObject("zmessageId");
    }

    public ParseUser getUser() {
        return getParseUser("user");
    }
    //</editor-fold>

    //<editor-fold desc="METHODS SETTERS">
    public void setZMessageId(ParseZMessage zMessageId) {
        put("zmessageId", zMessageId);
    }

    public void setSinchId(String sinchId) {
        put("sinchId", sinchId);
    }

    public void setUser(ParseUser user) {
        put("user", user);
    }
    //</editor-fold>

}
