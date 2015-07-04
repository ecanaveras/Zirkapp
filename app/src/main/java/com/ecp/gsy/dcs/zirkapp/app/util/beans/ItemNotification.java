package com.ecp.gsy.dcs.zirkapp.app.util.beans;

import com.ecp.gsy.dcs.zirkapp.app.util.parse.models.ParseZimess;
import com.parse.ParseUser;

import java.util.Date;

/**
 * Created by Elder on 04/05/2015.
 */
public class ItemNotification {

    private String notiId;
    private int typeNoti;
    private String summaryNoti;
    private String detailNoti;
    private ParseUser receptorUser;
    private ParseUser senderUser;
    private boolean readNoti;
    private Date created;
    private ParseUser userTarget;
    private ParseZimess zimessTarget;


    public String getSummaryNoti() {
        return summaryNoti;
    }

    public void setSummaryNoti(String summaryNoti) {
        this.summaryNoti = summaryNoti;
    }

    public String getDetailNoti() {
        return detailNoti;
    }

    public void setDetailNoti(String detailNoti) {
        this.detailNoti = detailNoti;
    }

    public int getTypeNoti() {
        return typeNoti;
    }

    public void setTypeNoti(int typeNoti) {
        this.typeNoti = typeNoti;
    }

    public boolean isReadNoti() {
        return readNoti;
    }

    public void setReadNoti(boolean readNoti) {
        this.readNoti = readNoti;
    }

    public String getNotiId() {
        return notiId;
    }

    public void setNotiId(String notiId) {
        this.notiId = notiId;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public ParseUser getSenderUser() {
        return senderUser;
    }

    public void setSenderUser(ParseUser senderUser) {
        this.senderUser = senderUser;
    }

    public ParseUser getUserTarget() {
        return userTarget;
    }

    public void setUserTarget(ParseUser userTarget) {
        this.userTarget = userTarget;
    }

    public ParseZimess getZimessTarget() {
        return zimessTarget;
    }

    public void setZimessTarget(ParseZimess zimessTarget) {
        this.zimessTarget = zimessTarget;
    }

    public ParseUser getReceptorUser() {
        return receptorUser;
    }

    public void setReceptorUser(ParseUser receptorUser) {
        this.receptorUser = receptorUser;
    }
}
