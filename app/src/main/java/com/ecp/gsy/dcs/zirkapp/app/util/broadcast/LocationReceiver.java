package com.ecp.gsy.dcs.zirkapp.app.util.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.ecp.gsy.dcs.zirkapp.app.fragments.UsersOnlineFragment;
import com.ecp.gsy.dcs.zirkapp.app.util.locations.Location;

/**
 * Created by Elder on 21/04/2015.
 */
public class LocationReceiver extends BroadcastReceiver {

    private UsersOnlineFragment usersOnlineFragment;

    public LocationReceiver(UsersOnlineFragment usersOnlineFragment) {
        this.usersOnlineFragment = usersOnlineFragment;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean locationUpdate = intent.getBooleanExtra("locationchange", false);
        double latitud = intent.getDoubleExtra("latitud", 0.0);
        double longitud = intent.getDoubleExtra("longitud", 0.0);
//        Bundle bundle = intent.getExtras();
//        android.location.Location currentLocation = (android.location.Location) bundle.get(LocationManager.KEY_LOCATION_CHANGED);
        Location location = new Location(latitud, longitud);
        if (locationUpdate && usersOnlineFragment != null) {
            usersOnlineFragment.findUsersOnline(location);
        }
        Log.i("current.location", "update");
    }
}
