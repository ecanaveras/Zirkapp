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
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.MenuItem;

import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.adapters.AdapterZimess;
import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

/**
 * Created by Elder on 15/07/2014.
 */
public class GlobalApplication extends Application {

    //Parse
    private ParseUser currentUser;

    private Context context;
    private boolean useApiPython;
    //Url de la API
    //private final static String URL_API_PHP = "http://zirkapp.byethost3.com/api/v1.1/zsms";

    //public final static String URL_API_PYTHON = "http://192.168.0.12:8000/zimess/?format=json";
    private final static String DOMAIN =  "http://zirkapp.herokuapp.com"; //"http://192.168.56.1:8000";
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

    public void getData(Context context, SwipeRefreshLayout swipe, AdapterZimess arrayAdapter) {
        this.context = context;
        //Actualizamos los datos del adpater atravez de un Asynctask
        if (isConected()) {
            new DownloadZimessTask(context, swipe, arrayAdapter, URL_API_PYTHON_GET_RADAR).execute();
        } else {
            //Toast.makeText(this, "Sin conexion", Toast.LENGTH_SHORT).show();
            showDiaglogConection();
        }
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

    public ParseUser getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(ParseUser currentUser) {
        this.currentUser = currentUser;
    }
}
