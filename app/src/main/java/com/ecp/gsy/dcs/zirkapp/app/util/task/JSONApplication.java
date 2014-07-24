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
import android.widget.ArrayAdapter;

import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.adapters.AdapterZimess;

/**
 * Created by Elder on 15/07/2014.
 */
public class JSONApplication extends Application {

    private Context context;
    //Url de la API
    private final static String URL = "http://zirkapp.byethost3.com/api/v1.1/zsms";

    public void getData(Context context, AdapterZimess arrayAdapter) {
        this.context = context;
        //Actualizamos los datos del adpater atravez de un Asynctask
        if (isConected()) {
            new DownloadZimessTask(context, arrayAdapter, URL).execute();
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
}
