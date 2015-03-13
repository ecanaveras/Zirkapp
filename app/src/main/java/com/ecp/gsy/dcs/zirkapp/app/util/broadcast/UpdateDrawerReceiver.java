package com.ecp.gsy.dcs.zirkapp.app.util.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.TextView;

/**
 * Created by Elder on 27/02/2015.
 */
public class UpdateDrawerReceiver extends BroadcastReceiver {

    private TextView textView;

    private Integer cantRows;

    public UpdateDrawerReceiver() {
    }

    public UpdateDrawerReceiver(TextView textView) {
        this.textView = textView;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Integer datos = (Integer) intent.getSerializableExtra("datos");
        if (datos != null) {
            this.cantRows = datos;
            if(textView != null) {
                textView.setText(Integer.toString(datos));
            }
        }
    }

    public Integer getCantRows() {
        return cantRows;
    }
}
