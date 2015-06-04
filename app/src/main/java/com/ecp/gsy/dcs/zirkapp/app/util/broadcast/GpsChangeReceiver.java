package com.ecp.gsy.dcs.zirkapp.app.util.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ecp.gsy.dcs.zirkapp.app.GlobalApplication;
import com.ecp.gsy.dcs.zirkapp.app.util.services.LocationService;

/**
 * Created by Elder on 26/05/2015.
 */
public class GpsChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //Log.d("GpsChangeReceiver", "started...");
        GlobalApplication globalApplication = (GlobalApplication) context.getApplicationContext();
        LocationService locationService = null;
        if (LocationService.isRunning()) {
            locationService = LocationService.getInstance();
            if (globalApplication.isEnabledGetLocation()) {
                locationService.startAutomaticLocation();
            } else {
                locationService.stopUsingGPS();
            }
        }
    }
}
