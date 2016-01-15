package com.ecp.gsy.dcs.zirkapp.app;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.ImageView;

import com.ecp.gsy.dcs.zirkapp.app.activities.MainActivity;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.models.ParseZComment;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.models.ParseZHistory;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.models.ParseZLastMessage;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.models.ParseZMessage;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.models.ParseZNotifi;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.models.ParseZimess;
import com.ecp.gsy.dcs.zirkapp.app.util.picasso.CircleTransform;
import com.ecp.gsy.dcs.zirkapp.app.util.services.LocationService;
import com.parse.Parse;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

//import com.parse.ParseFacebookUtils;
//import com.parse.ParseTwitterUtils;

/**
 * Created by Elder on 15/07/2014.
 */
public class GlobalApplication extends Application {

    //Key GCM
    public static final String SENDER_ID = "323224512527"; //Key GCM
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final String PROPERTY_USER = "user";

    private static Context context;

    //Controla si el chat esta habilidado
    private static boolean chatEnabled = false;

    //Controla los mensajes de GPS y NETWORK
    private static boolean isShowNetworkAlert = false;
    private static boolean isShowGpsAlert = false;

    //Parse
    private ParseUser currentUser;
    private ParseUser messagingParseUser;
    private ParseUser profileParseUser;
    private ParseZimess tempZimess;

    //Cantidades para el drawer
    private static Integer cantZimess;
    private static Integer cantUsersOnline;
    private static Integer cantNotifications;
    //private static Resources resources;

    //Order Zimess
    private int sortZimess;
    private boolean listeningNotifi = true;

    @Override
    public void onCreate() {
        super.onCreate();

        //Iniciar servicio de ubicacion
        Intent intentService = new Intent(this, LocationService.class);
        startService(intentService);

        // Enable Local Datastore. Mantiene el current user
        Parse.enableLocalDatastore(this);

        //Registrando Modelos
        ParseObject.registerSubclass(ParseZimess.class);
        ParseObject.registerSubclass(ParseZComment.class);
        ParseObject.registerSubclass(ParseZMessage.class);
        ParseObject.registerSubclass(ParseZHistory.class);
        ParseObject.registerSubclass(ParseZNotifi.class);
        ParseObject.registerSubclass(ParseZLastMessage.class);

        //Iniciar Parse
        Parse.initialize(this, getResources().getString(R.string.parse_api_id), getResources().getString(R.string.parse_api_key));

        //Facebook
        //ParseFacebookUtils.initialize(this);
        //Twitter
        //ParseTwitterUtils.initialize(getResources().getString(R.string.twitter_api_key), getResources().getString(R.string.twitter_api_secret));
    }

    /**
     * Busca en las prefencias si existe un registro posterior
     *
     * @param context
     * @param user
     * @return
     */
    public String getRegistrationId(Context context, String user) {
        SharedPreferences pref = getGCMPreferences();
        String registrationId = pref.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.d("GCM regId", "Registro GCM no encontrado");
            return "";
        }

        String registeredUser = pref.getString(PROPERTY_USER, "user");
        int registeredVersion = pref.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);

        Log.d("GCM", "Registro GCM encontrado (usuario=" + registeredUser + ", version=" + registeredVersion + ")");

        int currentVersion = getAppVersionCode(context);
        if (registeredVersion != currentVersion) {
            Log.d("GCM", "Nueva versión de la aplicación.");
            return "";
        } else if (!user.equals(registeredUser)) {
            Log.d("GCM", "Nuevo lblNombreUsuario de usuario.");
            return "";
        }
        return registrationId;
    }


    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId   registration ID
     */
    public void storeRegistrationId(Context context, String regId, String user) {
        final SharedPreferences prefs = getGCMPreferences();
        int appVersion = getAppVersionCode(context);
        Log.i("GCM", "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putString(PROPERTY_USER, user);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
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

    public void storeParseInstallation() {
        final ParseInstallation parseInstallation = ParseInstallation.getCurrentInstallation();
        parseInstallation.put("GCMSenderId", SENDER_ID);
        parseInstallation.put("user", ParseUser.getCurrentUser());
        parseInstallation.saveInBackground();
    }

    public void setAvatarParse(ParseFile parseFile, ImageView imageViewRender, boolean rounded) {
        if (imageViewRender != null) {
            if (parseFile != null) {
                if (rounded) {
                    Picasso.with(this)
                            .load(parseFile.getUrl())
                            .transform(new CircleTransform())
                            .into(imageViewRender);
                } else {
                    Picasso.with(this)
                            .load(parseFile.getUrl())
                            .into(imageViewRender);
                }
            } else {
                if (rounded) {
                    Picasso.with(this)
                            .load(R.drawable.ic_user_male)
                            .transform(new CircleTransform())
                            .into(imageViewRender);
                } else {
                    Picasso.with(this)
                            .load(R.drawable.ic_user_male)
                            .into(imageViewRender);

                }
            }
        }
    }

    /**
     * Dibuja la imgAvatar del parseFile en el ImageView
     *
     * @param parseFile
     * @param imageViewRender
     */
    public void setAvatarRounded(ParseFile parseFile, ImageView imageViewRender) {
        this.setAvatarParse(parseFile, imageViewRender, true);

    }


    /**
     * Dibuja y redimensiona la imgAvatar del parseFile en el ImageView
     *
     * @param parseFile
     * @param imageViewRender
     * @param width
     * @param height
     */
    public void setAvatarRoundedResize(ParseFile parseFile, ImageView imageViewRender, int width, int height) {
        if (imageViewRender != null) {
            if (parseFile != null) {
                Picasso.with(this)
                        .load(parseFile.getUrl())
                        .transform(new CircleTransform())
                        .resize(width, height)
                        .centerCrop()
                        .into(imageViewRender);
            } else {
                Picasso.with(this)
                        .load(R.drawable.ic_user_male)
                        .transform(new CircleTransform())
                        .resize(width, height)
                        .centerCrop()
                        .into(imageViewRender);
            }
        }
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

    public ParseZimess getTempZimess() {
        return tempZimess;
    }

    public void setTempZimess(ParseZimess tempZimess) {
        this.tempZimess = tempZimess;
    }

    public ParseUser getMessagingParseUser() {
        return messagingParseUser;
    }

    public void setMessagingParseUser(ParseUser messagingParseUser) {
        this.messagingParseUser = messagingParseUser;
    }

    public ParseUser getProfileParseUser() {
        return profileParseUser;
    }

    public void setProfileParseUser(ParseUser profileParseUser) {
        this.profileParseUser = profileParseUser;
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

    public static String getDescTimepass(Date createAt) {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTime(createAt);
        calendar.set(Calendar.HOUR, 0);
        Date currentDate = new Date();
        Long time = (currentDate.getTime() - createAt.getTime()); //Tiempo real
        String result = "";
        DateFormat fechaFormat = new SimpleDateFormat("dd/MM/yyyy");
        if (fechaFormat.format(currentDate).equals(fechaFormat.format(createAt))) {
            //HORAS
            result = new SimpleDateFormat("hh:mm a").format(createAt);
        } else {
            int diffInDays = (int) (TimeUnit.DAYS.convert(time, TimeUnit.MILLISECONDS));
            if (diffInDays <= 1) {
                DateFormat dayFormat = new SimpleDateFormat("dd");
                int dayToday = Integer.parseInt(dayFormat.format(currentDate));
                int dayMessage = Integer.parseInt(dayFormat.format(createAt));
                if ((dayToday - dayMessage) <= 1) {
                    result = context.getString(R.string.lblYesterday).toUpperCase();
                } else {
                    result = fechaFormat.format(createAt);
                }
            } else {
                result = fechaFormat.format(createAt);
            }
        }
        return result;
    }

    /**
     * Retorna una descripcion del tiempo transcurrido
     *
     * @param createAt
     * @return
     */
    public static String getTimepass(Date createAt) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(createAt);
        calendar.set(Calendar.HOUR, 0);
        Date currentDate = new Date();
        Long time = (currentDate.getTime() - createAt.getTime()); //Tiempo real
        Long timeDay = (currentDate.getTime() - calendar.getTime().getTime()); //Tiempo con hora 0
        String result = "";
        if (TimeUnit.DAYS.convert(timeDay, TimeUnit.MILLISECONDS) > 30) {
            //MESES
            result = "+" + String.valueOf(TimeUnit.DAYS.convert(time, TimeUnit.MILLISECONDS) / 30) + "M";
        } else if (TimeUnit.DAYS.convert(timeDay, TimeUnit.MILLISECONDS) > 1 && TimeUnit.DAYS.convert(timeDay, TimeUnit.MILLISECONDS) < 30) {
            //DIAS
            result = String.valueOf(TimeUnit.DAYS.convert(time, TimeUnit.MILLISECONDS)) + "d";
        } else if (TimeUnit.HOURS.convert(time, TimeUnit.MILLISECONDS) > 1 && TimeUnit.HOURS.convert(time, TimeUnit.MILLISECONDS) < 24) {
            //HORAS
            result = String.valueOf(TimeUnit.HOURS.convert(time, TimeUnit.MILLISECONDS)) + "h";
        } else if (TimeUnit.MINUTES.convert(time, TimeUnit.MILLISECONDS) > 1 && TimeUnit.MINUTES.convert(time, TimeUnit.MILLISECONDS) < 60) {
            //MINUTOS
            result = String.valueOf(TimeUnit.MINUTES.convert(time, TimeUnit.MILLISECONDS)) + "m";
        } else if (TimeUnit.SECONDS.convert(time, TimeUnit.MILLISECONDS) < 60) {
            //SEGUNDOS
            result = String.valueOf(TimeUnit.SECONDS.convert(time, TimeUnit.MILLISECONDS)) + "s";
        }
        return result;
    }

    /**
     * Indica si hay conexion a Internet
     *
     * @return
     */
    public boolean isConectedToInternet() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    /**
     * Indica si es posible se puede solicitar la ubicación
     *
     * @return
     */
    public boolean isEnabledGetLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean isEnabledGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isEnabledNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        return (isEnabledGPS || isEnabledNetwork);
    }

    //ALERTAS

    public void gpsShowSettingsAlert() {
        gpsShowSettingsAlert(context);
    }

    /**
     * Muestra una alerta en caso que esten desabilitados los accesorios de ubicacion
     */
    public void gpsShowSettingsAlert(final Context context) {
        if (context == null || isShowGpsAlert) {
            return;
        }
        isShowGpsAlert = true;
        AlertDialog.Builder alert = new AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyle);

        alert.setTitle(getResources().getString(R.string.lblSettingGPS));
        alert.setMessage(getResources().getString(R.string.msgGPSdisabled));
        alert.setPositiveButton(getResources().getString(R.string.lblOk), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(intent);
                isShowGpsAlert = false;
            }
        });

        alert.setNegativeButton(getResources().getString(R.string.lblCancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
                isShowGpsAlert = false;
            }
        });
        alert.setCancelable(false);
        alert.show();

    }

    public void networkShowSettingsAlert() {
        networkShowSettingsAlert(context);
    }

    /**
     * Muestra una alerta en caso que esten desabilitados los datos (wifi, movil)
     */
    public void networkShowSettingsAlert(final Context context) {
        if (context == null || isShowNetworkAlert) {
            return;
        }

        isShowNetworkAlert = true;
        AlertDialog.Builder alert = new AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyle);

        alert.setTitle(getResources().getString(R.string.lblSettingNetwork));
        alert.setMessage(getResources().getString(R.string.msgNetworkDisabled));
        alert.setPositiveButton(getResources().getString(R.string.lblOk), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                context.startActivity(intent);
                isShowNetworkAlert = false;
            }
        });

        alert.setNegativeButton(getResources().getString(R.string.lblCancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
                isShowNetworkAlert = false;
            }
        });
        alert.setCancelable(false);
        alert.show();
    }


    public boolean isListeningNotifi() {
        return listeningNotifi;
    }

    public void setListeningNotifi(boolean listeningNotifi) {
        this.listeningNotifi = listeningNotifi;
    }


    public static boolean isChatEnabled() {
        return chatEnabled;
    }

    public static void setChatEnabled(boolean chatEnabled) {
        GlobalApplication.chatEnabled = chatEnabled;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
