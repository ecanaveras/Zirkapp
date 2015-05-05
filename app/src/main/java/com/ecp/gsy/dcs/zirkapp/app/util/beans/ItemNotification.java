package com.ecp.gsy.dcs.zirkapp.app.util.beans;

/**
 * Created by Elder on 04/05/2015.
 */
public class ItemNotification {

    private String notiId;
    private int typeNoti;
    private String summaryNoti;
    private String detailNoti;
    private String receptorId;
    private String senderId;
    private String targetId;
    private boolean readNoti;


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


    public String getReceptorId() {
        return receptorId;
    }

    public void setReceptorId(String receptorId) {
        this.receptorId = receptorId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public int getTypeNoti() {
        return typeNoti;
    }

    public void setTypeNoti(int typeNoti) {
        this.typeNoti = typeNoti;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
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
}
