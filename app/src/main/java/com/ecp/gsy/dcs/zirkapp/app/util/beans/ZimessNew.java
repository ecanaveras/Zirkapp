package com.ecp.gsy.dcs.zirkapp.app.util.beans;

import com.parse.ParseGeoPoint;

/**
 * Created by Elder on 23/02/2015.
 */
public class ZimessNew {

    private String zimessId;
    private String userId;
    private String zimessText;
    private ParseGeoPoint location;


    public String getZimessId() {
        return zimessId;
    }

    public void setZimessId(String zimessId) {
        this.zimessId = zimessId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getZimessText() {
        return zimessText;
    }

    public void setZimessText(String zimessText) {
        this.zimessText = zimessText;
    }

    public ParseGeoPoint getLocation() {
        return location;
    }

    public void setLocation(ParseGeoPoint location) {
        this.location = location;
    }
}
