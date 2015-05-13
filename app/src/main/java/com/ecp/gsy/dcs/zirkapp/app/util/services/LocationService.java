package com.ecp.gsy.dcs.zirkapp.app.util.services;

import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import com.alertdialogpro.AlertDialogPro;
import com.ecp.gsy.dcs.zirkapp.app.R;


/**
 * Created by Elder on 13/05/2015.
 */
public class LocationService extends Service {

    private static LocationService instance = null;
    boolean isLocationEnabled = false;
    private boolean isAutomatic = false;

    //private Context mContext;

    private MyLocationListener listener;
    private Location currentBestLocation;
    private String TAG = MyLocationListener.class.getName();
    private boolean isEnabledGPS;
    private boolean isEnabledNetwork;
    private Intent intent;


    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 500; // 500 metros
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 5; // 5 minutoS

    private final Handler handler = new Handler();

    protected LocationManager locationManager;

    @Override
    public void onCreate() {
        instance = this;
        this.intent = new Intent("broadcast.gps.location_change");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler.post(getLocation);
        return super.onStartCommand(intent, flags, startId);
    }

    private final Runnable getLocation = new Runnable() {
        @Override
        public void run() {
            if (intent == null) intent = new Intent("broadcast.gps.location_change");
            getCurrentLocation();
        }
    };

    public static boolean isRunning() {
        return instance != null;
    }

    private Location getCurrentLocation() {
        return getCurrentLocation(false);
    }

    /**
     * Utiliza android para localizar el dispositivo
     *
     * @param isManual
     * @return
     */
    public Location getCurrentLocation(boolean isManual) {
        if (isLocationEnabled) {
            isLocationEnabled = false;
            stopUsingGPS();
        }
        if (!isLocationEnabled) {
            isLocationEnabled = true;
            isAutomatic = !isManual;
            try {
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
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
        }

        handler.postDelayed(getLocation, MIN_TIME_BW_UPDATES);
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
     * Indica si estamos conectados a Internet
     *
     * @return
     */
    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    @Override
    public void onDestroy() {
        stopUsingGPS();
        super.onDestroy();
    }

    /**
     * Detiene la peticion del servicio para ManagerGPS
     */
    public void stopUsingGPS() {
        if (locationManager != null) {
            locationManager.removeUpdates(listener);
        }
    }

    //LISTENER
    public class MyLocationListener implements LocationListener {

        private void sendItent(Location location) {
            if (location != null && intent != null && isAutomatic) {
                currentBestLocation = location;
                intent.putExtra("locationchange", true);
                intent.putExtra("latitud", location.getLatitude());
                intent.putExtra("longitud", location.getLongitude());
                intent.putExtra("provider", location.getProvider());
                sendBroadcast(intent);//mContext.sendBroadcast(intent);
                Log.i("intent.location.service", "update");
            }
        }

        @Override
        public void onLocationChanged(Location location) {
            //Nueva Ubicacion
            if (currentBestLocation != null) {
                if (isBetterLocation(location, currentBestLocation)) {
                    sendItent(location);
                }
            } else {
                sendItent(location);
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

    public static LocationService getInstance() {
        return instance;
    }

    //BINDER
    public class MyBinder extends Binder {
        LocationService getService() {
            return LocationService.this;
        }
    }

    //ALERTAS

    /**
     * Muestra una alerta en caso que esten desabilitados los accesorios de ubicacion
     */
    public void gpsShowSettingsAlert() {
        AlertDialogPro.Builder alert = new AlertDialogPro.Builder(this.getApplicationContext());

        alert.setTitle(R.string.lblSettingGPS);
        alert.setMessage(R.string.msgGPSdisabled);
        alert.setPositiveButton(R.string.lblOk, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
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
        AlertDialogPro.Builder alert = new AlertDialogPro.Builder(this.getApplicationContext());

        alert.setTitle(R.string.lblSettingNetwork);
        alert.setMessage(R.string.msgNetworkDisabled);
        alert.setPositiveButton(R.string.lblOk, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK);
                startActivity(intent);
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


}
