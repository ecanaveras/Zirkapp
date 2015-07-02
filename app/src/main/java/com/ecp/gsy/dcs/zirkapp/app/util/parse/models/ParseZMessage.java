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
    public static final String CREATED_BY = "createdBy";

    //<editor-fold desc="METHODS GETTER">
    public String getSinchId() {
        return getString("sinchId");
    }

    public ParseUser getSenderId() {
        return getParseUser("senderId");
    }

    public ParseUser getRecipientId() {
        return getParseUser("recipientId");
    }

    public String getMessageText() {
        return getString("messageText");
    }

    public boolean isMessageRead() {
        return getBoolean("messageRead");
    }
    //</editor-fold>

    //<editor-fold desc="METHODS SETTERS">
    public void setSinchId(String sinchId) {
        put("sinchId", sinchId);
    }

    public void setSenderId(ParseUser senderId) {
        put("senderId", senderId);
    }

    public void setRecipientId(ParseUser recipientId) {
        put("recipientId", recipientId);
    }

    public void setMessageText(String messageText) {
        put("messageText", messageText);
    }

    public void setMessageRead(boolean messageRead) {
        put("messageRead", messageRead);
    }
    //</editor-fold>

}
