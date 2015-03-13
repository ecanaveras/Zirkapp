package com.ecp.gsy.dcs.zirkapp.app.util.beans;

import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * Created by Elder on 23/02/2015.
 */
public class ZimessComment {

    private String commentId;
    private String zimessId;
    private ParseUser userComment;
    private ParseObject profile;
    private String commentText;

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getZimessId() {
        return zimessId;
    }

    public void setZimessId(String zimessId) {
        this.zimessId = zimessId;
    }

    public ParseUser getUserComment() {
        return userComment;
    }

    public void setUserComment(ParseUser userComment) {
        this.userComment = userComment;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public ParseObject getProfile() {
        return profile;
    }

    public void setProfile(ParseObject profile) {
        this.profile = profile;
    }
}
