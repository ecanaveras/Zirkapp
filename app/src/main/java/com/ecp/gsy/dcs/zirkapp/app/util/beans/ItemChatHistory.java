package com.ecp.gsy.dcs.zirkapp.app.util.beans;

import com.ecp.gsy.dcs.zirkapp.app.util.parse.models.ParseZMessage;
import com.parse.ParseUser;

/**
 * Created by ecanaveras on 02/12/2015.
 */
public class ItemChatHistory {

    private ParseUser userMessage;
    private Integer cantMessagesNoRead;
    private ParseZMessage lastMessage;

    public ItemChatHistory() {
    }

    public ItemChatHistory(ParseUser userMessage, Integer cantMessagesNoRead) {
        this.userMessage = userMessage;
        this.cantMessagesNoRead = cantMessagesNoRead;
    }

    public ItemChatHistory(ParseUser userMessage, Integer cantMessagesNoRead, ParseZMessage lastMessage) {
        this.userMessage = userMessage;
        this.cantMessagesNoRead = cantMessagesNoRead;
        this.lastMessage = lastMessage;
    }

    //<editor-fold desc="GETTERS">
    public ParseUser getUserMessage() {
        return userMessage;
    }

    public Integer getCantMessagesNoRead() {
        return cantMessagesNoRead;
    }

    public ParseZMessage getLastMessage() {
        return lastMessage;
    }

    //</editor-fold>


    //<editor-fold desc="SETTERS">
    public void setUserMessage(ParseUser userMessage) {
        this.userMessage = userMessage;
    }

    public void setCantMessagesNoRead(Integer cantMessagesNoRead) {
        this.cantMessagesNoRead = cantMessagesNoRead;
    }

    public void setLastMessage(ParseZMessage lastMessage) {
        this.lastMessage = lastMessage;
    }

    //</editor-fold>
}
