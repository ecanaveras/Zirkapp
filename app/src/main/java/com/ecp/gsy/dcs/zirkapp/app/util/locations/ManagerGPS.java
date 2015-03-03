package com.ecp.gsy.dcs.zirkapp.app.util.locations;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import com.ecp.gsy.dcs.zirkapp.app.R;

/**
 * Created by Elder on 02/06/2014.
 */
public class ManagerGPS extends Service implements LocationListener {

    private final Context mContext;
    private boolean isEnabledGPS = false;
    private boolean isEnabledNetwork = false;
    private Location location;
    private Double latitud;
    private Double longitud;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 20; // 20 metros
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 5; // 5 minutoS

    protected LocationManager locationManager;

    /**
     * Establece comunicacion con los servicios de Android para hallar la ubicacion
     *
     * @param mContext
     */
    public ManagerGPS(Context mContext) {
        this.mContext = mContext;
    }

    /**
     * Utiliza android para localizar el dispositivo
     *
     * @return
     */
    public void obtenertUbicacion() {
        try {
            locationManager = (LocationManager) mContext.getSystemService(mContext.LOCATION_SERVICE);
            isEnabledGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isEnabledNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (!isEnabledGPS && !isEnabledNetwork) {//Desabilitados
                //Mostrar Dialog.
                this.showSettingsAlert();
            } else { //Habilitados
                if (isEnabledNetwork) {
                    this.establecerUbicacion(LocationManager.NETWORK_PROVIDER);
                }
                if (isEnabledGPS) {
                    if (location == null) {
                        this.establecerUbicacion(LocationManager.GPS_PROVIDER);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Establece la unbicacion
     *
     * @param provider
     */
    private void establecerUbicacion(String provider) {
        locationManager.requestLocationUpdates(provider, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
        //Log.d(provider, provider);
        if (locationManager != null) {
            location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                latitud = location.getLatitude();
                longitud = location.getLongitude();
            }
        }
    }

    /**
     * Detiene la peticion del servicio para ManagerGPS
     */
    public void stopUsingGPS() {
        if (locationManager != null) {
            locationManager.removeUpdates(ManagerGPS.this);
        }
    }

    /**
     * Muestra una alerta en caso que esten desabilitados los accesorios de ubicacion
     */
    private void showSettingsAlert() {
        AlertDialog.Builder alert = new AlertDialog.Builder(mContext);

        alert.setTitle(R.string.lblSettingGPS);
        alert.setMessage(R.string.msgGPSdisabled);
        alert.setPositiveButton(R.string.lblOk, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });

        alert.setNegativeButton(R.string.lblCancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        alert.show();
    }

    public Double getLatitud() {
        return latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    @Override
    public void onLocationChanged(Location location) {
        //Nueva Ubicacion
        this.latitud = location.getLatitude();
        this.longitud = location.getLongitude();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override
    public void onProviderEnabled(String s) {
    }

    @Override
    public void onProviderDisabled(String s) {
        stopUsingGPS();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
