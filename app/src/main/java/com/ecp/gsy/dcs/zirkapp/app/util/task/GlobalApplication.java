package com.ecp.gsy.dcs.zirkapp.app.util.task;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.util.Log;

import com.ecp.gsy.dcs.zirkapp.app.MainActivity;
import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.beans.Zimess;
import com.facebook.FacebookSdk;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.parse.PushService;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Handler;

/**
 * Created by Elder on 15/07/2014.
 */
public class GlobalApplication extends Application {

    //Key GCM
    public static final String SENDER_ID = "323224512527"; //Key GCM
    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "1.1.1";
    public static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    //Parse
    private ParseUser currentUser;
    private ParseUser customParseUser;
    private Zimess tempZimess;

    //Cantidades para el drawer
    private static Integer cantZimess;
    private static Integer cantUsersOnline;
    private static Integer cantNotifications;

    //Order Zimess
    private int sortZimess;

    @Override
    public void onCreate() {
        super.onCreate();

        //Iniciar Parse
        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        Parse.initialize(this, "VDJEJIMbyuOiis9bwBHmrOIc7XDUqYHQ0TMhA23c", "9EJKzvp4LhRdnLqfH6jkHPaWd58IVXaBKAWdeItE");
        //ParseInstallation.getCurrentInstallation().saveInBackground();
        /*final ParseInstallation parseInstallation = ParseInstallation.getCurrentInstallation();
        final String androidId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        android.os.Handler handler = new android.os.Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                parseInstallation.put("GCMSenderId", SENDER_ID);
                parseInstallation.saveInBackground();
            }
        }, 3000);*/

        //Facebook
        FacebookSdk.sdkInitialize(getApplicationContext());
    }


    //GCM

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    public boolean checkPlayServices(Activity activity) {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, activity,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i("Error.GooglePlaySer", "This device is not supported.");
            }
            return false;
        }
        return true;
    }


    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId   registration ID
     */
    public void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences();
        int appVersion = getAppVersion(context);
        Log.i("GCM", "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    /**
     * Gets the current registration ID for application on GCM service.
     * <p/>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     * registration ID.
     */
    public String getRegistrationId(Context context) {
        SharedPreferences preferences = getGCMPreferences();
        String registrationId = preferences.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i("GCM", "Registration not found.");
            return "";
        }

        // Check if app was updated; if so, it must clear the registration ID
        // since the existing registration ID is not guaranteed to work with
        // the new app version.
        int registeredVersion = preferences.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i("GCM", "App version changed.");
            return "";
        }
        return registrationId;
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    public static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGCMPreferences() {
        return getSharedPreferences(MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
    }


    public void storeParseInstallation(final String androidId) {
        final ParseInstallation parseInstallation = ParseInstallation.getCurrentInstallation();
        //final String androidId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        parseInstallation.put("GCMSenderId", SENDER_ID);
        parseInstallation.put("user", ParseUser.getCurrentUser());
        parseInstallation.saveInBackground();
    }

    /**
     * Retorna el current ParseUser
     *
     * @return
     */
    public ParseUser getCurrentUser() {
        this.currentUser = ParseUser.getCurrentUser();
        return this.currentUser;
    }

    /**
     * Retorna la imagen del usuario
     *
     * @return
     */
    public static Bitmap getAvatar(ParseUser currentUser) {
        ParseFile parseFile = currentUser.getParseFile("avatar");
        try {
            if (currentUser != null && parseFile != null && parseFile.getData().length > 0) {
                byte[] byteImage;
                byteImage = parseFile.getData();
                if (byteImage != null) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPurgeable = true;
                    Bitmap bitmap1 = BitmapFactory.decodeByteArray(byteImage, 0, byteImage.length, options);
                    return bitmap1;//Bitmap.createScaledBitmap(bitmap1, 50, 50, true);
                }
            }
        } catch (ParseException e) {
            Log.e("Parse.avatar.exception", e.getMessage());
        } catch (OutOfMemoryError e) {
            Log.e("Parse.avatar.outmemory", e.toString());
        }
        return null;
    }

    public Zimess getTempZimess() {
        return tempZimess;
    }

    public void setTempZimess(Zimess tempZimess) {
        this.tempZimess = tempZimess;
    }

    public ParseUser getCustomParseUser() {
        return customParseUser;
    }

    public void setCustomParseUser(ParseUser customParseUser) {
        this.customParseUser = customParseUser;
    }

    public static Integer getCantZimess() {
        return cantZimess;
    }

    public static void setCantZimess(Integer _cantZimess) {
        cantZimess = _cantZimess;
    }

    public static Integer getCantUsersOnline() {
        return cantUsersOnline;
    }

    public static void setCantUsersOnline(Integer _cantUsersOnline) {
        cantUsersOnline = _cantUsersOnline;
    }

    public Integer getCantNotifications() {
        return cantNotifications;
    }

    public void setCantNotifications(Integer cantNotifications) {
        this.cantNotifications = cantNotifications;
    }

    public int getSortZimess() {
        return sortZimess;
    }

    public void setSortZimess(int sortZimess) {
        this.sortZimess = sortZimess;
    }

    /**
     * Describe una fecha o retorna una.
     *
     * @param createAt
     * @return
     */
    public String getDescFechaPublicacion(Date createAt) {
        String descFec = null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getDefault());
        calendar.setTime(createAt);
        Date dateZimess = calendar.getTime();

        SimpleDateFormat sDateFormat = new SimpleDateFormat("MMM-dd-yyyy");
        if (sDateFormat.format(dateZimess).equals(sDateFormat.format(new Date()))) { //Mismo día
            descFec = "hoy";
        } else {
            calendar.setTime(new Date());
            int diaMes = calendar.get(Calendar.DAY_OF_MONTH);
            calendar.set(Calendar.DAY_OF_MONTH, diaMes - 1);
            if (sDateFormat.format(dateZimess).equals(sDateFormat.format(calendar.getTime()))) {//Mismo dia
                descFec = "ayer";
            } else {
                descFec = new SimpleDateFormat("MMM-dd-yyyy hh:mm a").format(dateZimess);
            }
        }
        return descFec;
    }

    /**
     * Retorna una descripcion del tiempo transcurrido
     *
     * @param createAt
     * @return
     */
    public String getTimepass(Date createAt) {
        long MILLSECS_PER_MINUTES = 60 * 1000; //Minutos
        long MILLSECS_PER_HOUR = 60 * 60 * 1000; //Horas
        long MILLSECS_PER_DAY = 24 * 60 * 60 * 1000; //Milisegundos al dia
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(createAt);
        calendar.set(Calendar.HOUR, 0);
        Date currentDate = new Date();
        Long time = (currentDate.getTime() - createAt.getTime()); //Tiempo real
        Long timeDay = (currentDate.getTime() - calendar.getTime().getTime()); //Tiempo con hora 0
        String result = "";
        if ((timeDay / MILLSECS_PER_DAY) >= 1.0) {
            result = new Double(timeDay / MILLSECS_PER_DAY).intValue() + "d";
        } else if ((time / MILLSECS_PER_HOUR) < 24.0 && (time / MILLSECS_PER_HOUR) > 1.0) {
            result = new Double(time / MILLSECS_PER_HOUR).intValue() + "h";
        } else if ((time / MILLSECS_PER_MINUTES) <= 60.0 && (time / MILLSECS_PER_MINUTES) >= 1.0) {
            result = new Double(time / MILLSECS_PER_MINUTES).intValue() + "m";
        } else if ((time / 1000) <= 60.0) {
            result = ((time / 1000)) + "s";
        }
        return result;
    }


    //Verificar si hay conexion a Internet
    public boolean isConected() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }
}
