package com.ecp.gsy.dcs.zirkapp.app.util.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ecp.gsy.dcs.zirkapp.app.fragments.UsersFragment;
import com.ecp.gsy.dcs.zirkapp.app.fragments.ZimessFragment;
import com.ecp.gsy.dcs.zirkapp.app.util.locations.Location;

/**
 * Created by Elder on 21/04/2015.
 */
public class LocationReceiver extends BroadcastReceiver {

    public static final String ACTION_LISTENER = "broadcast.gps.location.change";

    public LocationReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean locationUpdate = intent.getBooleanExtra("locationchange", false);
        double latitud = intent.getDoubleExtra("latitud", 0.0);
        double longitud = intent.getDoubleExtra("longitud", 0.0);
        int sortZimess = intent.getIntExtra("sort_zimess", 0);

        Location location = new Location(latitud, longitud);
        if (locationUpdate) {
            //actualizar lista de usuarios en el chat y posicion
            if (UsersFragment.isRunning()) {
                UsersFragment usersFragment = UsersFragment.getInstance();
                if (usersFragment.isConnectedUser) {
                    usersFragment.conectarChat(location);
                }
                Log.i("chat.location", "update");
            }

            //actualizar lista de Zimess en la nueva posision
            if (ZimessFragment.isRunning()) {
                ZimessFragment zimessFragment = ZimessFragment.getInstance();
                zimessFragment.findZimessAround(location, sortZimess);
                Log.i("zimess.location", "update");
            }
        }

    }
}
