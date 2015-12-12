package com.ecp.gsy.dcs.zirkapp.app.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;

import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.beans.BadgeDrawable;

/**
 * Created by ecanaveras on 10/12/2015.
 * <p/>
 * Clase de Utilidades
 */
public class Utils {

    public static void setBadgeCount(Context context, LayerDrawable icon, int count) {
        BadgeDrawable badge;

        //Reusar Drawable
        Drawable reuse = icon.findDrawableByLayerId(R.id.ic_badge);
        if (reuse != null && reuse instanceof BadgeDrawable) {
            badge = (BadgeDrawable) reuse;
        } else {
            badge = new BadgeDrawable(context);
        }

        if (icon != null) {
            badge.setCount(count);
            icon.mutate();
            icon.setDrawableByLayerId(R.id.ic_badge, badge);
        }
    }
}
