package com.ecp.gsy.dcs.zirkapp.app.util.parse.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * Created by Elder on 04/07/2015.
 */
@ParseClassName("ParseZNotifi")
public class ParseZNotifi extends ParseObject {

    public static final String RECEPTOR_USER = "receptorUser";
    public static final String SENDER_USER = "senderUser";
    public static final String ZIMESS_TARGET = "zimessTarget";
    public static final String DETAIL_NOTI = "detailNoti";
    public static final String READ_NOTI = "readNoti";
    public static final String SUMMARY_NOTI = "summaryNoti";
    public static final String TYPE_NOTI = "typeNoti";

    //<editor-fold desc="METHODS GETTERS">
    public ParseUser getReceptorUser() {
        return getParseUser(RECEPTOR_USER);
    }

    public ParseUser getSenderUser() {
        return getParseUser(SENDER_USER);
    }

    public ParseZimess getZimessTarget() {
        return (ParseZimess) getParseObject(ZIMESS_TARGET);
    }

    public String getDetailNoti() {
        return getString(DETAIL_NOTI);
    }

    public boolean isReadNoti() {
        return getBoolean(READ_NOTI);
    }

    public String getSummaryNoti() {
        return getString(SUMMARY_NOTI);
    }

    public int getTypeNoti() {
        return getInt(TYPE_NOTI);
    }
    //</editor-fold>


    //<editor-fold desc="METHODS SETTERS">
    public void setReceptorUser(ParseUser receptorUser) {
        put(RECEPTOR_USER, receptorUser);
    }

    public void setSenderUser(ParseUser senderUser) {
        put(SENDER_USER, senderUser);
    }

    public void setZimessTarget(ParseZimess zimessTarget) {
        put(ZIMESS_TARGET, zimessTarget);
    }

    public void setDetailNoti(String detailNoti) {
        put(DETAIL_NOTI, detailNoti);
    }

    public void setReadNoti(boolean readNoti) {
        put(READ_NOTI, readNoti);
    }

    public void setSummaryNoti(String summaryNoti) {
        put(SUMMARY_NOTI, summaryNoti);
    }

    public void setTypeNoti(int typeNoti) {
        put(TYPE_NOTI, typeNoti);
    }
    //</editor-fold>
}
