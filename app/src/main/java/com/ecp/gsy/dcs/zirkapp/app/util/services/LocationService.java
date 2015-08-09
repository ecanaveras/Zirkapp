package com.ecp.gsy.dcs.zirkapp.app.util.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.ecp.gsy.dcs.zirkapp.app.GlobalApplication;
import com.ecp.gsy.dcs.zirkapp.app.util.broadcast.LocationReceiver;


/**
 * Created by Elder on 13/05/2015.
 */
public class LocationService extends Service {

    private static LocationService instance = null;
    //private boolean isAutomatic = false;

    private MyLocationListener listener;
    private Location oldLocation;
    private String TAG = MyLocationListener.class.getSimpleName();
    private GlobalApplication globalApplication;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 0 metros
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 15; // 15 minutoS

    /**
     * Tiempo de umbral de diferencia fija durante un minuto.
     */
    public static final int TIME_DIFFERENCE_THRESHOLD = 1 * 60 * 1000;

    public static long TIME_LAST_LOCATION;


    //private final Handler handler = new Handler();

    protected LocationManager locationManager;

    @Override
    public void onCreate() {
        instance = this;
        globalApplication = (GlobalApplication) this.getApplicationContext();
//        getCurrentLocation();
        //getLocation();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "started");
        //handler.post(getLocation);
        return super.onStartCommand(intent, flags, startId);
    }

    /*private final Runnable getLocation = new Runnable() {
        @Override
        public void run() {
            if (intent == null) intent = new Intent(LocationReceiver.ACTION_LISTENER);
            getCurrentLocation();
        }
    };*/

    public static boolean isRunning() {
        return instance != null;
    }

    /*public void startAutomaticLocation() {
        handler.post(getLocation);
    }*/

    /*private void getLocation() {
        isAutomatic = true;
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        String provider = getProviderName(locationManager);
        Location location = locationManager.getLastKnownLocation(provider);
        listener = new MyLocationListener();
//        if (location != null) {
//            listener.onLocationChanged(location);
//        }
        locationManager.requestLocationUpdates(provider, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, listener);
        Log.d("provider.location", provider);
    }
*/

    /**
     * Retorna la ubicacion actual del dispositivo
     *
     * @return
     */
    public Location getCurrentLocation() {
        //isAutomatic = !isManual;
        if (globalApplication.isConectedToInternet()) {
            //handler.postDelayed(getLocation, MIN_TIME_BW_UPDATES);
            try {
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                boolean isEnabledGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                boolean isEnabledNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                //Network
                if (isEnabledNetwork) {
                    //Log.d("provider.location", "network");
                    //return getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    return getLastKnownLocation(getProviderName(locationManager));
                }
                //Gps
                if (isEnabledGPS) {
                    //Log.d("provider.location", "gps");
                    //return getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    return getLastKnownLocation(getProviderName(locationManager));
                }

                //Desabilitado la RED y GPS
                Log.d("provider.location", "disabled");
                globalApplication.gpsShowSettingsAlert();

            } catch (Exception e) {
                stopUsingGPS();
                Log.e("Error : Location", "Impossible to connect to LocationManager", e);
            }

        } else {
            globalApplication.networkShowSettingsAlert();
        }
        return null;
    }

    /**
     * Obtiene una nueva ubicacion
     *
     * @param provider
     */
    private Location getLastKnownLocation(String provider) {
        Log.d("provider.location", provider);
        Location location = locationManager.getLastKnownLocation(provider);
        if (listener == null) {
            listener = new MyLocationListener();
            locationManager.requestLocationUpdates(provider, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, listener);
        }
        TIME_LAST_LOCATION = System.currentTimeMillis();
        return location;
    }


    /**
     * Determinar la mejor ubicacion
     *
     * @param oldLocation
     * @param newLocation
     * @return
     */
    protected boolean isBetterLocation(Location oldLocation, Location newLocation) {
        if (oldLocation == null) {
            return true;
        }

        //Comprobar si la nueva ubicacion es mas reciente
        boolean isNewer = newLocation.getTime() > oldLocation.getTime();

        //Comprobar si la nueva ubicacion es mas precisa, la precision es el radio en metrios, menos mejor.
        boolean isMoreAccurate = newLocation.getAccuracy() < oldLocation.getAccuracy();

        if (isMoreAccurate && isNewer) {
            //Mas precisa y mas reciente
            return true;
        } else if (isMoreAccurate && !isNewer) {
            //Mas precisa, pero desactualizada debido a movimientos del usuario
            //Establecemos un umbral para determinar la viabilidad de la ubicacion
            long timeDiff = newLocation.getTime() - oldLocation.getTime();
            if (timeDiff > -TIME_DIFFERENCE_THRESHOLD) {
                return true;
            }
        }

        return false;
    }


    /**
     * Determina el mejor proveedor
     */
    private String getProviderName(LocationManager locationManager) {
        if (locationManager != null) {
            Criteria criteria = new Criteria();
            criteria.setPowerRequirement(Criteria.POWER_LOW);
            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
            criteria.setSpeedRequired(true);
            criteria.setAltitudeRequired(false);
            criteria.setBearingRequired(false);
            criteria.setCostAllowed(false);
            return locationManager.getBestProvider(criteria, true);
        }
        return null;
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
     * Detiene la peticion del servicio para LocationService
     */
    public void stopUsingGPS() {
        if (locationManager != null && listener != null) {
            locationManager.removeUpdates(listener);
        }
    }

    //LISTENER
    public class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            //Comprobar si la nueva ubicacion es mejor
            if (isBetterLocation(oldLocation, location)) {
                sendItent(location);
                oldLocation = location;
            } else {
                sendItent(oldLocation); //Se envia la mejor ubicacion
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            /*Log.d(TAG, "The status of the provider " + provider + " has changed");
            if (status == 0) {
                Log.d(TAG, provider + " is OUT OF SERVICE");
            } else if (status == 1) {
                Log.d(TAG, provider + " is TEMPORARILY_UNAVAILABLE");
            } else {
                Log.d(TAG, provider + " is AVAILABLE");
            }*/
        }

        @Override
        public void onProviderEnabled(String s) {
        }

        @Override
        public void onProviderDisabled(String s) {
            stopUsingGPS();
        }

        private void sendItent(Location location) {
            if (location != null) {
                Intent intent = new Intent(LocationReceiver.ACTION_LISTENER);
                intent.putExtra("locationchange", true);
                intent.putExtra("latitud", location.getLatitude());
                intent.putExtra("longitud", location.getLongitude());
                intent.putExtra("provider", location.getProvider());
                intent.putExtra("sort_zimess", globalApplication.getSortZimess());
                sendBroadcast(intent);
                Log.i("intent.location.service", "update");
            }
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
}
