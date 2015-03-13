package com.ecp.gsy.dcs.zirkapp.app.util.beans;

import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Elder on 23/02/2015.
 */
public class Zimess implements Serializable {

//    private int mData;

    private String zimessId;
    private ParseUser user;
    private String zimessText;
    private ParseGeoPoint location;
    private Date createAt;
    private ParseObject profile;
    private Integer cantComment;

    public String getZimessId() {
        return zimessId;
    }

    public void setZimessId(String zimessId) {
        this.zimessId = zimessId;
    }

    public ParseUser getUser() {
        return user;
    }

    public void setUser(ParseUser user) {
        this.user = user;
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

    public Date getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
    }

    public ParseObject getProfile() {
        return profile;
    }

    public void setProfile(ParseObject profile) {
        this.profile = profile;
    }

    public Integer getCantComment() {
        return cantComment != null ? cantComment : 0;
    }

    public void setCantComment(Integer cantComments) {
        this.cantComment = cantComments;
    }

    /*
    /**
     * Manejo del Parceable.
     *
    public static final Creator<ZimessNew> CREATOR = new Creator<ZimessNew>() {
        @Override
        public ZimessNew createFromParcel(Parcel parcel) {
            return new ZimessNew(parcel);
        }

        @Override
        public ZimessNew[] newArray(int i) {
            return new ZimessNew[i];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(mData);
    }

    private ZimessNew(Parcel in){
        mData = in.readInt();
    }
    */
}
