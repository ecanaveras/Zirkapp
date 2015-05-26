package com.ecp.gsy.dcs.zirkapp.app.util.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.ecp.gsy.dcs.zirkapp.app.GlobalApplication;
import com.ecp.gsy.dcs.zirkapp.app.util.services.LocationService;

/**
 * Created by Elder on 25/05/2015.
 */
public class NetworkChangeReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
//        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//        NetworkInfo mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
//        if (wifi.isAvailable() || mobile.isAvailable()) {
        Log.d("NetworkChangeReceiver", "started...");
        GlobalApplication globalApplication = (GlobalApplication) context.getApplicationContext();
        if (globalApplication.isConectedToInternet())
            if (LocationService.isRunning()) {
                LocationService locationService = LocationService.getInstance();
                locationService.getCurrentLocation(false);
            }
    }

}
