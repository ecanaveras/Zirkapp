package com.ecp.gsy.dcs.zirkapp.app.util.task;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;

import com.ecp.gsy.dcs.zirkapp.app.MainActivity;
import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.beans.Zimess;
import com.facebook.FacebookSdk;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.PushService;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

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
    private static Resources resources;

    //Order Zimess
    private int sortZimess;
    private boolean listeningNotifi;

    @Override
    public void onCreate() {
        super.onCreate();

        resources = this.getResources();

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        //Iniciar Parse
        Parse.initialize(this, "VDJEJIMbyuOiis9bwBHmrOIc7XDUqYHQ0TMhA23c", "9EJKzvp4LhRdnLqfH6jkHPaWd58IVXaBKAWdeItE");
        PushService.setDefaultPushCallback(this, MainActivity.class);

        //Facebook
        FacebookSdk.sdkInitialize(this);
        //ParseFacebookUtils.initialize(getResources().getString(R.string.facebook_app_id));
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
        int appVersion = getAppVersionCode(context);
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
        int currentVersion = getAppVersionCode(context);
        if (registeredVersion != currentVersion) {
            Log.i("GCM", "App version changed.");
            return "";
        }
        return registrationId;
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    public static int getAppVersionCode(Context context) {
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
     * @return Application's version code from the {@code PackageManager}.
     */
    public static String getAppVersionName(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
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

    public void storeParseInstallation() {
        final ParseInstallation parseInstallation = ParseInstallation.getCurrentInstallation();
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
    public static RoundedBitmapDrawable getAvatar(ParseUser currentUser) {
        if (currentUser != null) {
            ParseFile parseFile = currentUser.getParseFile("avatar");
            try {
                if (parseFile != null && parseFile.getData().length > 0) {
                    byte[] byteImage;
                    byteImage = parseFile.getData();
                    if (byteImage != null) {
                        Bitmap bitmap = decodeSampledBitmapFromResource(byteImage, 100, 100);
                        if (bitmap != null)
                            //Cuadrar imagen
                            if (bitmap != null && bitmap.getWidth() > bitmap.getHeight()) {
                                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getHeight(), bitmap.getHeight());
                            } else if (bitmap != null && bitmap.getHeight() > bitmap.getWidth()) {
                                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getWidth());
                            }
                        RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(resources, bitmap);
                        //corner radius
                        roundedBitmapDrawable.setCornerRadius(bitmap.getHeight());
                        return roundedBitmapDrawable;
                    }
                }
            } catch (ParseException e) {
                Log.e("Parse.avatar.exception", e.getMessage());
            } catch (OutOfMemoryError e) {
                Log.e("Parse.avatar.outmemory", e.toString());
            }
        }
        //Si no hay imagen se retorna una imagen por defecto.
        Bitmap bitmapDefault = BitmapFactory.decodeResource(resources, R.drawable.ic_user_male);
        RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(resources, bitmapDefault);
        return roundedBitmapDrawable;
    }


    /**
     * Code by http://developer.android.com/intl/es/training/displaying-bitmaps/load-bitmap.html
     *
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    private static Bitmap decodeSampledBitmapFromResource(byte[] byteImage, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        //BitmapFactory.decodeResource(res, resId, options);
        BitmapFactory.decodeByteArray(byteImage, 0, byteImage.length, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        //return BitmapFactory.decodeResource(res, resId, options);
        return BitmapFactory.decodeByteArray(byteImage, 0, byteImage.length, options);
    }

    /**
     * Code by http://developer.android.com/intl/es/training/displaying-bitmaps/load-bitmap.html
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
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

    public static Integer getCantNotifications() {
        return cantNotifications;
    }

    public static void setCantNotifications(Integer _cantNotifications) {
        cantNotifications = _cantNotifications;
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

    public boolean isListeningNotifi() {
        return listeningNotifi;
    }

    public void setListeningNotifi(boolean listeningNotifi) {
        this.listeningNotifi = listeningNotifi;
    }
}
