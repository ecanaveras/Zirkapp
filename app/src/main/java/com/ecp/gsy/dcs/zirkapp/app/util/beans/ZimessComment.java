package com.ecp.gsy.dcs.zirkapp.app.util.beans;

import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;

import com.ecp.gsy.dcs.zirkapp.app.GlobalApplication;
import com.parse.ParseUser;

import java.util.Date;

/**
 * Created by Elder on 23/02/2015.
 */
public class ZimessComment {

    private String commentId;
    private String zimessId;
    private ParseUser userComment;
    private String commentText;
    private Date createAt;
    private Bitmap avatar;

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

    public Date getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
    }
}
