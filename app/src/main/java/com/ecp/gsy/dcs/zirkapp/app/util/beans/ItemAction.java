package com.ecp.gsy.dcs.zirkapp.app.util.beans;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

/**
 * Created by Elder on 03/08/2014.
 */
public class ItemAction {
    private Drawable icon;
    private Bitmap thumb;
    private String title;
    private boolean selected;

    public ItemAction() {
    }

    public ItemAction(Drawable icon) {
        this.icon = icon;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public Drawable getIcon() {
        return this.icon;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return this.selected;
    }

    public void setThumb(Bitmap thumb) {
        this.thumb = thumb;
    }

    public Bitmap getThumb() {
        return this.thumb;
    }
}
