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
import com.ecp.gsy.dcs.zirkapp.app.util.beans.ZimessNew;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseQuery;
import com.parse.ParseUser;

/**
 * Created by Elder on 15/07/2014.
 */
public class GlobalApplication extends Application {

    //Parse
    private ParseUser currentUser;
    private ParseUser customParseUser;
    private ZimessNew tempZimess;

    private Context context;
    private boolean useApiPython;
    //Url de la API
    //private final static String URL_API_PHP = "http://zirkapp.byethost3.com/api/v1.1/zsms";

    //public final static String URL_API_PYTHON = "http://192.168.0.12:8000/zimess/?format=json";
    private final static String DOMAIN = "http://zirkapp.herokuapp.com"; //"http://192.168.56.1:8000";
    public final static String URL_API_PYTHON = DOMAIN + "/api/zimess/?format=json";
    public final static String URL_API_PYTHON_GET_RADAR = DOMAIN + "/api/zimess/radar/";

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
     * @return
     */
    public ParseUser getCurrentUser() {
        return ParseUser.getCurrentUser();
    }

    public ZimessNew getTempZimess() {
        return tempZimess;
    }

    public void setTempZimess(ZimessNew tempZimess) {
        this.tempZimess = tempZimess;
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
                        useApiPython = true;
                    }
                })
                .setNegativeButton(R.string.msgNo, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        useApiPython = false;
                        dialogInterface.cancel();
                    }
                });
        AlertDialog alertDialog = alBuilder.create();
        alertDialog.show();
    }
}
