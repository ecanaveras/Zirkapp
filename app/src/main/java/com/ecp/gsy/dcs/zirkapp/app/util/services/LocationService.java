package com.ecp.gsy.dcs.zirkapp.app.util.services;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

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
    private String TAG = LocationService.class.getSimpleName();
    private GlobalApplication globalApplication;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 0 metros
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 15; // 15 minutoS

    /**
     * Tiempo de umbral de diferencia fija durante un minuto.
     */
    public static final int TIME_DIFFERENCE_THRESHOLD = 1 * 60 * 1000;


    protected LocationManager locationManager;

    @Override
    public void onCreate() {
        instance = this;
        globalApplication = (GlobalApplication) this.getApplicationContext();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "started");
        return super.onStartCommand(intent, flags, startId);
    }

    public static boolean isRunning() {
        return instance != null;
    }

    /**
     * Retorna la ubicacion actual del dispositivo
     *
     * @return
     */
    public Location getCurrentLocation() {
        //isAutomatic = !isManual;
        if (globalApplication.isConectedToInternet()) {
            boolean isPermissionGranted = true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                isPermissionGranted = isPermissionGranted();
            }
            if (isPermissionGranted) {
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
    @SuppressWarnings("ResourceType")
    private Location getLastKnownLocation(String provider) {
        Log.d("provider.location", provider);
        Location location = null;
        boolean pedirLocation = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pedirLocation = isPermissionGranted();
        }
        if (pedirLocation) {
            location = locationManager.getLastKnownLocation(provider);
            setLocationListener(provider);
        }
        return location;
    }

    @SuppressWarnings("ResourceType")
    private void setLocationListener(String provider) {
        if (listener == null) {
            listener = new MyLocationListener();
            locationManager.requestLocationUpdates(provider, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, listener);
        }
    }

    /**
     * Verifica si la app tiene permisos para obtener la ubicacion [Aplica para Android M]
     *
     * @return
     */
    private boolean isPermissionGranted() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Zirkapp no tiene permisos para otbener tu ubicación, fijate en tus ajustes", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;

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
            // A new location is always better than no location
            return true;
        }

        long TWO_MINUTES = 2 * 60 * 1000;

        // Check whether the new location fix is newer or older
        long timeDelta = newLocation.getTime() - oldLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (newLocation.getAccuracy() - oldLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(newLocation.getProvider(),
                oldLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
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
     * Checks whether two providers are the same
     */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
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
            sendItent(location);
            //Comprobar si la nueva ubicacion es mejor
            /*if (isBetterLocation(oldLocation, location)) { //Se envia la mejor ubicacion
                sendItent(location);
                oldLocation = location;
            } else {
                sendItent(oldLocation);
            }*/
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
