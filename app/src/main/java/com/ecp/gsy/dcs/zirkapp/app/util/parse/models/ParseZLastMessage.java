package com.ecp.gsy.dcs.zirkapp.app.util.parse.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.List;

/**
 * Created by Elder on 01/07/2015.
 */
@ParseClassName("ParseZLastMessage")
public class ParseZLastMessage extends ParseObject {

    public static final String ZMESSAGE_ID = "zmessageId";
    public static final String SENDER_ID = "senderId";
    public static final String RECIPIENT_ID = "recipientId";
    public static final String DELETE_FOR = "deleteFor";

    private boolean deletedForMe = false;

    //<editor-fold desc="METHODS GETTER">
    public ParseZMessage getZMessageId() {
        return (ParseZMessage) getParseObject(ZMESSAGE_ID);
    }

    public ParseUser getSenderId() {
        return getParseUser(SENDER_ID);
    }

    public ParseUser getRecipientId() {
        return getParseUser(RECIPIENT_ID);
    }

    public List<String> getDeletedFor() {
        return getList(DELETE_FOR);
    }
    //</editor-fold>

    //<editor-fold desc="METHODS SETTERS">
    public void setZMessageId(ParseZMessage zMessageId) {
        put(ZMESSAGE_ID, zMessageId);
    }

    public void setSenderId(ParseUser senderId) {
        put(SENDER_ID, senderId);
    }

    public void setRecipientId(ParseUser recipientId) {
        put(RECIPIENT_ID, recipientId);
    }

    public void addDeleteFor(String objectId) {
        addUnique(DELETE_FOR, objectId);
    }
    //</editor-fold>


    public boolean isDeletedForMe(String currentId) {
        if (getDeletedFor() != null && currentId != null) {
            this.deletedForMe = getDeletedFor().contains(currentId);
        }
        return deletedForMe;
    }
}
