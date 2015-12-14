package com.ecp.gsy.dcs.zirkapp.app.util.parse.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * Created by Elder on 01/07/2015.
 */
@ParseClassName("ParseZMessage")
public class ParseZMessage extends ParseObject {

    public static final String SINCH_ID = "sinchId";
    public static final String SENDER_ID = "senderId";
    public static final String RECIPIENT_ID = "recipientId";
    public static final String MESSAGE_TEXT = "messageText";
    public static final String MESSAGE_READ = "messageRead";
    public static final String CANT_HIST_DELETE = "cant_hist_delete";

    //<editor-fold desc="METHODS GETTER">
    public String getSinchId() {
        return getString(SINCH_ID);
    }

    public ParseUser getSenderId() {
        return getParseUser(SENDER_ID);
    }

    public ParseUser getRecipientId() {
        return getParseUser(RECIPIENT_ID);
    }

    public String getMessageText() {
        return getString(MESSAGE_TEXT);
    }

    public boolean isMessageRead() {
        return getBoolean(MESSAGE_READ);
    }
    //</editor-fold>

    //<editor-fold desc="METHODS SETTERS">
    public void setSinchId(String sinchId) {
        put(SINCH_ID, sinchId);
    }

    public void setSenderId(ParseUser senderId) {
        put(SENDER_ID, senderId);
    }

    public void setRecipientId(ParseUser recipientId) {
        put(RECIPIENT_ID, recipientId);
    }

    public void setMessageText(String messageText) {
        put(MESSAGE_TEXT, messageText);
    }

    public void setMessageRead(boolean messageRead) {
        put(MESSAGE_READ, messageRead);
    }

    public void setCantHistDelete(int cantHistDelete) {
        put(CANT_HIST_DELETE, cantHistDelete);
    }
    //</editor-fold>


    public void makeMessageRead() {
        this.setMessageRead(true);
        saveInBackground();
    }

}
