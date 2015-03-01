package com.ecp.gsy.dcs.zirkapp.app.util.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.TextView;

/**
 * Created by Elder on 27/02/2015.
 */
public class UpdateDrawerReceiver extends BroadcastReceiver {

    private Integer cantRows;

    @Override
    public void onReceive(Context context, Intent intent) {
        Integer datos = (Integer) intent.getSerializableExtra("datos");
        if (datos != null) {
            this.cantRows = datos;
        }
    }

    public Integer getCantRows() {
        return cantRows;
    }
}
