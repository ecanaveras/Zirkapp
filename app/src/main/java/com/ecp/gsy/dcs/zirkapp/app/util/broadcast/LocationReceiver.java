package com.ecp.gsy.dcs.zirkapp.app.util.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.ecp.gsy.dcs.zirkapp.app.fragments.UsersOnlineFragment;
import com.ecp.gsy.dcs.zirkapp.app.fragments.ZimessFragment;
import com.ecp.gsy.dcs.zirkapp.app.util.locations.Location;
import com.ecp.gsy.dcs.zirkapp.app.util.task.GlobalApplication;

/**
 * Created by Elder on 21/04/2015.
 */
public class LocationReceiver extends BroadcastReceiver {

    private UsersOnlineFragment usersOnlineFragment;
    private ZimessFragment zimessFragment;
    private GlobalApplication globalApplication;

    public LocationReceiver(UsersOnlineFragment usersOnlineFragment) {
        this.usersOnlineFragment = usersOnlineFragment;
    }

    public LocationReceiver(ZimessFragment zimessFragment) {
        this.zimessFragment = zimessFragment;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean locationUpdate = intent.getBooleanExtra("locationchange", false);
        double latitud = intent.getDoubleExtra("latitud", 0.0);
        double longitud = intent.getDoubleExtra("longitud", 0.0);

        Location location = new Location(latitud, longitud);
        if (locationUpdate) {
            //actualizar lista de usuarios en el chat y posicion
            if (usersOnlineFragment != null) {
                if (usersOnlineFragment.isConnectedUser) {
                    usersOnlineFragment.conectarChat(location);
                }
                Log.i("chat.location", "update");
            }

            //actualizar lista de Zimess en la nueva posision
            if (zimessFragment != null) {
                globalApplication = (GlobalApplication) zimessFragment.getActivity().getApplicationContext();
                zimessFragment.findZimessAround(location, globalApplication.getSortZimess());
                Log.i("zimess.location", "update");
            }
        }

    }
}
