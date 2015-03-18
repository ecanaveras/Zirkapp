package com.ecp.gsy.dcs.zirkapp.app.util.beans;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.parse.ParseException;
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
    private Integer cantComment;

    /**
     * Retorna la imagen del usuario
     * @return
     */
    public Bitmap getAvatar() {
        if (this.user != null && this.user.getParseFile("avatar") != null) {
            byte[] byteImage;
            try {
                byteImage = this.user.getParseFile("avatar").getData();
                if (byteImage != null) {
                    return BitmapFactory.decodeByteArray(byteImage, 0, byteImage.length);
                }
            } catch (ParseException e) {
                Log.e("Parse.avatar.exception", e.getMessage());
            } catch (OutOfMemoryError e) {
                Log.e("Parse.avatar.outmemory", e.toString());
            }
        }
        return null;
    }


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
