package com.ecp.gsy.dcs.zirkapp.app.util.task;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.ecp.gsy.dcs.zirkapp.app.GlobalApplication;
import com.ecp.gsy.dcs.zirkapp.app.util.services.MessageService;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

/**
 * Created by Elder on 25/03/2015.
 */

public class RegisterGcmTask extends AsyncTask<Void, Void, String> {

    private GlobalApplication globalApplication;
    private Context context;
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

        String msg = "";
        try {
            String regId = gcm.register(globalApplication.SENDER_ID);
            msg = regId;

            globalApplication.storeRegistrationId(context, regId);
            if (regId != null) {
                globalApplication.storeParseInstallation(regId);
            } else globalApplication.storeParseInstallation();

        } catch (IOException e) {
            msg = "Error :" + e.getMessage();
            globalApplication.storeParseInstallation();
        }

        return msg;
    }

    @Override
    protected void onPostExecute(String _msg) {
        if (context != null) {
            //Iniciar SINCH
            if (GlobalApplication.isChatEnabled()) {
                Intent serviceIntent = new Intent(context.getApplicationContext(), MessageService.class);
                serviceIntent.putExtra("regId", _msg);
                context.startService(serviceIntent);//TODO disable sinch
            }
        }
    }
}
