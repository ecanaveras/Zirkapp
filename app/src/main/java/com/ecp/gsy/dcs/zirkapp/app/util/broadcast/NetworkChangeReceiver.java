package com.ecp.gsy.dcs.zirkapp.app.util.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ecp.gsy.dcs.zirkapp.app.GlobalApplication;
import com.ecp.gsy.dcs.zirkapp.app.fragments.NotificationsFragment;
import com.ecp.gsy.dcs.zirkapp.app.fragments.UsersFragment;
import com.ecp.gsy.dcs.zirkapp.app.fragments.ZimessFragment;
import com.ecp.gsy.dcs.zirkapp.app.util.services.LocationService;
import com.parse.ParseUser;

/**
 * Created by Elder on 25/05/2015.
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

    String TAG = "starServiceNetworkChangeReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        //Log.d("NetworkChangeReceiver", "started...");
        GlobalApplication globalApplication = (GlobalApplication) context.getApplicationContext();
        if (globalApplication.isConectedToInternet()) {
            if (LocationService.isRunning()) {
                LocationService locationService = LocationService.getInstance();
                //locationService.startAutomaticLocation();
                locationService.getCurrentLocation();
            } else {
                //Iniciamos el servicio
                Log.i(TAG, LocationService.class.getSimpleName() + " started...");
                Intent intentService = new Intent(context, LocationService.class);
                context.startService(intentService);
            }
            if (!globalApplication.isEnabledGetLocation()) {
                changeLayoutFragments();
            }

            //Actualizar listado notificaciones
            if (NotificationsFragment.isRunning()) {
                NotificationsFragment fragment = NotificationsFragment.getInstance();
                fragment.findNotifications(ParseUser.getCurrentUser());
            }
        } else {
            changeLayoutFragments();
        }
    }

    /**
     * Actualiza Layout para indicar que el Internet o GPS esta apagado
     */
    private void changeLayoutFragments() {
        if (ZimessFragment.isRunning()) {
            ZimessFragment zimessFragment = ZimessFragment.getInstance();
            zimessFragment.findZimessAround(null, 0);
        }
        if (UsersFragment.isRunning()) {
            UsersFragment usersFragment = UsersFragment.getInstance();
            usersFragment.findUsersOnline(null);
        }
    }

}