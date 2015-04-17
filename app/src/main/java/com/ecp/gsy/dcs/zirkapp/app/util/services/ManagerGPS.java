package com.ecp.gsy.dcs.zirkapp.app.util.services;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import com.alertdialogpro.AlertDialogPro;
import com.ecp.gsy.dcs.zirkapp.app.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by Elder on 02/06/2014.
 */
public class ManagerGPS extends Service implements LocationListener {

    private Context mContext;
    private boolean isEnabledGPS = false;
    private boolean isEnabledNetwork = false;
    private boolean enableGetLocation = false;
    private Location location;
    private Double latitud;
    private Double longitud;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 15; // 15 metros
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 5; // 5 minutoS

    protected LocationManager locationManager;

    /**
     * Establece comunicacion con los servicios de Android para hallar la ubicacion
     *
     * @param mContext
     */
    public ManagerGPS(Context mContext) {
        this.mContext = mContext;
        this.obtenertUbicacion();
    }

    /**
     * Utiliza android para localizar el dispositivo
     *
     * @return
     */
    public Location obtenertUbicacion() {
        try {
            locationManager = (LocationManager) mContext.getSystemService(mContext.LOCATION_SERVICE);
            isEnabledGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isEnabledNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (!isEnabledGPS && !isEnabledNetwork) {//Desabilitados
                this.enableGetLocation = false;
            } else { //Habilitados
                this.enableGetLocation = true;
                if (isEnabledNetwork) {
                    this.establecerUbicacion(LocationManager.NETWORK_PROVIDER);
                    Log.d("Network", "Network");
                }
                if (isEnabledGPS) {
                    if (location == null) {
                        this.establecerUbicacion(LocationManager.GPS_PROVIDER);
                        Log.d("GPS Enabled", "GPS Enabled");
                    }
                }
            }
        } catch (Exception e) {
            Log.e("Error : Location", "Impossible to connect to LocationManager", e);
        }

        return location;
    }

    /**
     * Establece la ubicacion
     *
     * @param provider
     */
    private void establecerUbicacion(String provider) {
        locationManager.requestLocationUpdates(provider, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
        //Log.d(provider, provider);
        if (locationManager != null) {
            location = locationManager.getLastKnownLocation(provider);
            updateGPSCoordenadas();
        }
    }

    /**
     * Actualiza las coordenadas
     */
    private void updateGPSCoordenadas() {
        if (location != null) {
            latitud = location.getLatitude();
            longitud = location.getLongitude();
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
     * Devuelve la direccion actual del dispositivo
     *
     * @return
     */
    public List<Address> getGeocoderAddress() {
        if (location != null) {
            Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
            try {
                if (geocoder != null) {
                    List<Address> addressList = geocoder.getFromLocation(latitud, longitud, 1);
                    return addressList;
                }
            } catch (IOException e) {
                Log.e("Error : Geocoder", "Impossible to connect to Geocoder", e);
            }
        }
        return null;
    }

    /**
     * Retorna la direccion resumida del dispositivo
     *
     * @return
     */
    public String getAddressLine() {
        return getDataAddress(1);
    }

    public String getLocality() {
        return getDataAddress(2);
    }

    public String getPostalCode() {
        return getDataAddress(3);
    }

    public String getCountryName() {
        return getDataAddress(4);
    }

    /**
     * Retorna los datos de la direccion, segun el TypeAddres
     *
     * @param typeAddress
     * @return
     */
    private String getDataAddress(int typeAddress) {
        List<Address> addressList = getGeocoderAddress();
        if (addressList != null && addressList.size() > 0) {
            Address address = addressList.get(0);

            switch (typeAddress) {
                case 1:
                    return address.getAddressLine(0);
                case 2:
                    int maxLines = addressList.get(0).getMaxAddressLineIndex();
                    for (int i = 0; i < maxLines; i++) {
                        if ((maxLines - 1) == i) {
                            return addressList.get(0).getAddressLine(i);
                        }
                    }
                case 3:
                    return address.getPostalCode();
                case 4:
                    return address.getCountryName();
                default:
                    return null;
            }
        }

        return null;
    }

    public Double getLatitud() {
        if (location != null) {
            latitud = location.getLatitude();
        }
        return latitud;
    }

    public Double getLongitud() {
        if (location != null) {
            longitud = location.getLongitude();
        }
        return longitud;
    }

    public boolean isEnableGetLocation() {
        return enableGetLocation;
    }

    /**
     * Indica si estamos conectados a Internet
     *
     * @return
     */
    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(mContext.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }


    /**
     * Muestra una alerta en caso que esten desabilitados los accesorios de ubicacion
     */
    public void gpsShowSettingsAlert() {
        AlertDialogPro.Builder alert = new AlertDialogPro.Builder(mContext);

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

    /**
     * Muestra una alerta en caso que esten desabilitados los datos (wifi, movil)
     */
    public void networkShowSettingsAlert() {
        AlertDialogPro.Builder alert = new AlertDialogPro.Builder(mContext);

        alert.setTitle(R.string.lblSettingNetwork);
        alert.setMessage(R.string.msgNetworkDisabled);
        alert.setPositiveButton(R.string.lblOk, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK);
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

    @Override
    public void onLocationChanged(Location location) {
        //Nueva Ubicacion
        this.getLatitud();
        this.getLongitud();
//        Log.d("GPS.latitud", getLatitud().toString());
//        Log.d("GPS.longitud", getLongitud().toString());
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
