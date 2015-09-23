package com.ecp.gsy.dcs.zirkapp.app.util.services;

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
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.ecp.gsy.dcs.zirkapp.app.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by Elder on 02/06/2014.
 */
public class ManagerGPS extends Service {

    private Context mContext;
    private boolean isEnabledGPS = false;
    private boolean isEnabledNetwork = false;
    private MyLocationListener listener;
    private String TAG = MyLocationListener.class.getName();
    private Location location;
    private Location currentBestLocation;
    private Double latitud;
    private Double longitud;
    private Intent intent;

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
        intent = new Intent("broadcast.gps.location_change");
        if (isOnline()) {
            location = this.getCurrentLocation();
        } else {
            networkShowSettingsAlert();
        }

    }

    /**
     * Establece comunicacion con los servicios de Android para hallar la ubicacion bajo demanda
     *
     * @param mContext
     */
    public ManagerGPS(Context mContext, boolean isOnDemand) {
        this.mContext = mContext;
        if (isOnline()) {
            location = this.getCurrentLocation();
        } else {
            networkShowSettingsAlert();
        }
    }

    /**
     * Utiliza android para localizar el dispositivo
     *
     * @return
     */
    private Location getCurrentLocation() {
        try {
            locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
            isEnabledGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isEnabledNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            //Network
            if (isEnabledNetwork) {
                Log.d("provider.location", "network");
                return getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            //Gps
            if (isEnabledGPS) {
                Log.d("provider.location", "gps");
                return getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }

            if (!isEnabledNetwork && !isEnabledGPS) {//Desabilitado GPS
                Log.d("provider.location", "disabled");
                gpsShowSettingsAlert();
            }
        } catch (Exception e) {
            Log.e("Error : Location", "Impossible to connect to LocationManager", e);
        }

        return null;
    }

    /**
     * Obtiene una nueva ubicacion
     *
     * @param provider
     */
    private Location getLastKnownLocation(String provider) {
        listener = new MyLocationListener();
        locationManager.requestLocationUpdates(provider, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, listener);
        return locationManager.getLastKnownLocation(provider);
    }

    /**
     * Determinar la mejor ubicacion
     *
     * @param location
     * @param currentBestLocation
     * @return
     */
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            return true;
        }

        //Chequear cual ubicacion es mas nueva;
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > MIN_TIME_BW_UPDATES;
        boolean isSignificantlyOlder = timeDelta < -MIN_TIME_BW_UPDATES;
        boolean isNewer = timeDelta > 0;

        //Si ha pasado mas del tiempo minimo, se debe usar una nueva ubicacion
        if (isSignificantlyNewer) {
            return true;
        } else if (isSignificantlyOlder) {
            return false;
        }

        //Chequear si la nueva ubicacion es mas o menos exacta
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        //Verificar si es el mismo provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(), currentBestLocation.getProvider());

        //Determinar la calidad de ubicacion, usando la combinacion de tiempo y exactitud
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }

        return false;
    }

    /**
     * Verifica si 2 provider son iguales
     *
     * @param provider1
     * @param provider2
     * @return
     */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    /**
     * Detiene la peticion del servicio para ManagerGPS
     */
    public void stopUsingGPS() {
        if (locationManager != null) {
            locationManager.removeUpdates(listener);
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
                    List<Address> addressList = geocoder.getFromLocation(getLatitud(), getLongitud(), 1);
                    return addressList;
                }
            } catch (IOException e) {
                Log.e("Error : Geocoder", "Impossible to connect to Geocoder", e);
            }
        }
        return null;
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

    /**
     * Indica si estamos conectados a Internet
     *
     * @return
     */
    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v("stop.managerGsp", "done");
        stopUsingGPS();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //LISTENER
    public class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            //Nueva Ubicacion
            if (currentBestLocation != null) {
                if (isBetterLocation(location, currentBestLocation)) {
                    currentBestLocation = location;
                }
            } else {
                currentBestLocation = location;
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d(TAG, "The status of the provider " + provider + " has changed");
            if (status == 0) {
                Log.d(TAG, provider + " is OUT OF SERVICE");
            } else if (status == 1) {
                Log.d(TAG, provider + " is TEMPORARILY_UNAVAILABLE");
            } else {
                Log.d(TAG, provider + " is AVAILABLE");
            }
        }

        @Override
        public void onProviderEnabled(String s) {
        }

        @Override
        public void onProviderDisabled(String s) {
            stopUsingGPS();
        }
    }

    //ALERTAS

    /**
     * Muestra una alerta en caso que esten desabilitados los accesorios de ubicacion
     */
    public void gpsShowSettingsAlert() {
        AlertDialog.Builder alert = new AlertDialog.Builder(mContext, R.style.AppCompatAlertDialogStyle);

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
        AlertDialog.Builder alert = new AlertDialog.Builder(mContext, R.style.AppCompatAlertDialogStyle);

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


    //GETTERS AND SETTERS

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
}
