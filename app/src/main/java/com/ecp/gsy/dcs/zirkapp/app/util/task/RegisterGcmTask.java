package com.ecp.gsy.dcs.zirkapp.app.util.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.ecp.gsy.dcs.zirkapp.app.GlobalApplication;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

/**
 * Created by Elder on 25/03/2015.
 */

public class RegisterGcmTask extends AsyncTask<Void, Void, String> {

    private GlobalApplication application;
    private Context context;
    private GoogleCloudMessaging gcm;
    private String username;

    public RegisterGcmTask(GoogleCloudMessaging gcm, String username, Context applicationContext) {
        this.gcm = gcm;
        this.username = username;
        this.context = applicationContext;
        application = (GlobalApplication) context.getApplicationContext();
    }

    @Override
    protected String doInBackground(Void... params) {

        String regId = "";
        try {
            //Obtenemos el id de la instalacion
            regId = gcm.register(application.SENDER_ID);
            //Guardamos los datos de la instalacion
            application.storeRegistrationId(context, regId, username);
            //Info del gcm id
            Log.d("GCM regID", "Registrado en GCM: registration_id=" + regId);

        } catch (IOException e) {
            Log.e("Error registro GCM:", e.getMessage());
        }

        return regId;
    }

    @Override
    protected void onPostExecute(String _msg) {
        if (context != null) {
            //Iniciar SINCH
            /*if (GlobalApplication.isChatEnabled()) {
                Intent serviceIntent = new Intent(context.getApplicationContext(), MessageService.class);
                serviceIntent.putExtra("regId", _msg);
                context.startService(serviceIntent);
            }
            */
        }
    }
}
