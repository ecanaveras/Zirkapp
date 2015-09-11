package com.ecp.gsy.dcs.zirkapp.app.util.parse.models;

import android.support.v4.graphics.drawable.RoundedBitmapDrawable;

import com.ecp.gsy.dcs.zirkapp.app.GlobalApplication;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * Created by Elder on 01/07/2015.
 */
@ParseClassName("ParseZComment")
public class ParseZComment extends ParseObject {

    public static final String ZIMESS_ID = "zimessId";
    public static final String COMMENT_TEXT = "commentText";
    public static final String USER = "user";

    //<editor-fold desc="METHODS GETTERS">
    public String getCommentText() {
        return getString(COMMENT_TEXT);
    }

    public ParseUser getUser() {
        return getParseUser(USER);
    }

    public ParseObject getZimessId() {
        return getParseObject(ZIMESS_ID);
    }
    //</editor-fold>

    //<editor-fold desc="METHODS SETTERS">
    public void setCommentText(String commentText) {
        put(COMMENT_TEXT, commentText);
    }

    public void setUser(ParseUser user) {
        put(USER, user);
    }

    public void setZimessId(ParseObject zimessId) {
        put(ZIMESS_ID, zimessId);
    }
    //</editor-fold>
}
