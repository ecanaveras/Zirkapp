package com.ecp.gsy.dcs.zirkapp.app.util.task;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.ecp.gsy.dcs.zirkapp.app.MessagingActivity;
import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.broadcast.SinchConnectReceiver;
import com.ecp.gsy.dcs.zirkapp.app.util.services.MessageService;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

/**
 * Created by Elder on 25/03/2015.
 */

public class RegisterGcmTask extends AsyncTask<Void, Void, String> {

    private GlobalApplication globalApplication;
    private Context context;
    private String msg = "";
    private String regId;
    private GoogleCloudMessaging gcm;

    public RegisterGcmTask(GoogleCloudMessaging gcm, Context applicationContext) {
        this.gcm = gcm;
        this.context = applicationContext;
        globalApplication = (GlobalApplication) context.getApplicationContext();
    }

    @Override
    protected String doInBackground(Void... params) {
        if (gcm == null) {
            gcm = GoogleCloudMessaging.getInstance(context);
        }

        try {
            regId = gcm.register(GlobalApplication.SENDER_ID);
            msg = regId;

            globalApplication.storeRegistrationId(context, regId);
            globalApplication.storeParseInstallation(regId);

        } catch (IOException e) {
            msg = "Error :" + e.getMessage();
        }


        return msg;
    }

    @Override
    protected void onPostExecute(String _msg) {
        if (context != null) {
            Intent serviceIntent = new Intent(context.getApplicationContext(), MessageService.class);
            serviceIntent.putExtra("regId", _msg);
            context.startService(serviceIntent); //TODO Mensajeria Disabled
        }
    }
}
