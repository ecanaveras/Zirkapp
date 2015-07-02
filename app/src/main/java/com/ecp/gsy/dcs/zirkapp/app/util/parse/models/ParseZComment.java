package com.ecp.gsy.dcs.zirkapp.app.util.parse.models;

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
        return getString("commentText");
    }

    public ParseUser getUser() {
        return getParseUser("user");
    }

    public ParseObject getZimessId() {
        return getParseObject("zimessId");
    }
    //</editor-fold>

    //<editor-fold desc="METHODS SETTERS">
    public void setCommentText(String commentText) {
        put("commentText", commentText);
    }

    public void setUser(ParseUser user) {
        put("user", user);
    }

    public void setZimessId(ParseObject zimessId) {
        put("zimessId", zimessId);
    }
    //</editor-fold>
}
