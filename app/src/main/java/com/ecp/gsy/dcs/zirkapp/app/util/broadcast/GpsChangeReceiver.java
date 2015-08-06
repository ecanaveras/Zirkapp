package com.ecp.gsy.dcs.zirkapp.app.util.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ecp.gsy.dcs.zirkapp.app.GlobalApplication;
import com.ecp.gsy.dcs.zirkapp.app.util.services.LocationService;

/**
 * Created by Elder on 26/05/2015.
 */
public class GpsChangeReceiver extends BroadcastReceiver {

    String TAG = "starServiceGpsChangeReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        //Log.d("GpsChangeReceiver", "started...");
        GlobalApplication globalApplication = (GlobalApplication) context.getApplicationContext();
        LocationService locationService = null;
        if (LocationService.isRunning()) {
            locationService = LocationService.getInstance();
            //locationService.startAutomaticLocation();
            locationService.getCurrentLocation();
        } else {
            if (globalApplication.isEnabledGetLocation()) {
                //Iniciamos el servicio
                Log.i(TAG, LocationService.class.getSimpleName() + " started...");
                Intent intentService = new Intent(context, LocationService.class);
                context.startService(intentService);
            }
        }
    }
}
