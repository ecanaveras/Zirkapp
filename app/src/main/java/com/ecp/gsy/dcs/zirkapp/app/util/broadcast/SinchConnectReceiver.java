package com.ecp.gsy.dcs.zirkapp.app.util.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * Created by Elder on 20/04/2015.
 */
public class SinchConnectReceiver extends BroadcastReceiver {

    private LinearLayout layoutInfo;

    public SinchConnectReceiver(LinearLayout layoutInfo) {
        this.layoutInfo = layoutInfo;
    }

    /**
     * Comprueba que el servicio Sinch se inicie
     *
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Boolean succes = intent.getBooleanExtra("success", false);
        if (!succes) {
            Toast.makeText(context.getApplicationContext(), "Messaging service failed to start", Toast.LENGTH_LONG).show();
            Log.e("Sinch.service.state", "Failed...");
        } else {
            Log.i("Sinch.service.state", "Starting...");
            if (layoutInfo != null) {
                layoutInfo.setVisibility(View.GONE);
            }
        }
    }
}
