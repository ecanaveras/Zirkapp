package com.ecp.gsy.dcs.zirkapp.app.util.task;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.util.Log;

import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.beans.Zimess;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Elder on 15/07/2014.
 */
public class GlobalApplication extends Application {

    //Parse
    private ParseUser currentUser;
    private ParseUser customParseUser;
    private Zimess tempZimess;

    private Context context;
//    private boolean useApiPython;
    //Url de la API
    //private final static String URL_API_PHP = "http://zirkapp.byethost3.com/api/v1.1/zsms";

    //public final static String URL_API_PYTHON = "http://192.168.0.12:8000/zimess/?format=json";
//    private final static String DOMAIN = "http://zirkapp.herokuapp.com"; //"http://192.168.56.1:8000";
//    public final static String URL_API_PYTHON = DOMAIN + "/api/zimess/?format=json";
//    public final static String URL_API_PYTHON_GET_RADAR = DOMAIN + "/api/zimess/radar/";

    @Override
    public void onCreate() {
        super.onCreate();

        //Iniciar Parse
        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        Parse.initialize(this, "VDJEJIMbyuOiis9bwBHmrOIc7XDUqYHQ0TMhA23c", "9EJKzvp4LhRdnLqfH6jkHPaWd58IVXaBKAWdeItE");
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }


    /**
     * Retorna el ParseUser buscando por ObjectId
     *
     * @param userId
     * @return
     */
    public ParseUser getCustomParseUser(String userId) {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.getInBackground(userId, new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                if (e == null) {
                    customParseUser = parseUser;
                } else {
                    Log.e("Parse.findUser", "Error la buscar el usuario");
                }
            }
        });
        /*
        query.whereEqualTo("objectId", userId);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> parseUsers, ParseException e) {
                if (e == null) {
                    customParseUser = parseUsers.get(0);
                } else {
                    Log.e("Parse.findUser", "Error la buscar el usuario");
                }
            }
        });
        */
        return customParseUser;
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

    public Zimess getTempZimess() {
        return tempZimess;
    }

    public void setTempZimess(Zimess tempZimess) {
        this.tempZimess = tempZimess;
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
            result = ((time / 1000)) +"s";
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

    private void showDiaglogConection() {
        AlertDialog.Builder alBuilder = new AlertDialog.Builder(context);
        alBuilder.setTitle(R.string.msgDisconnet);
        alBuilder.setCancelable(false)
                .setMessage("Quieres conectarte?")
                .setPositiveButton(R.string.msgYes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        context.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }
                })
                .setNegativeButton(R.string.msgNo, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
        AlertDialog alertDialog = alBuilder.create();
        alertDialog.show();
    }

    private void selectApi() {
        AlertDialog.Builder alBuilder = new AlertDialog.Builder(context);
        alBuilder.setTitle(R.string.msgDisconnet);
        alBuilder.setCancelable(false)
                .setMessage("Quieres la API Python, solo funciona local?")
                .setPositiveButton(R.string.msgYes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
//                        useApiPython = true;
                    }
                })
                .setNegativeButton(R.string.msgNo, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
//                        useApiPython = false;
                        dialogInterface.cancel();
                    }
                });
        AlertDialog alertDialog = alBuilder.create();
        alertDialog.show();
    }
}
