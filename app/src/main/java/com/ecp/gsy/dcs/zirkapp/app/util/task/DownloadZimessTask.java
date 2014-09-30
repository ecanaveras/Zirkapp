package com.ecp.gsy.dcs.zirkapp.app.util.task;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.HomeReceiver;
import com.ecp.gsy.dcs.zirkapp.app.util.JSONToStringCollection;
import com.ecp.gsy.dcs.zirkapp.app.util.adapters.AdapterZimess;
import com.ecp.gsy.dcs.zirkapp.app.util.beans.Zimess;
import com.ecp.gsy.dcs.zirkapp.app.util.http.ConectorHttpJSON;
import com.ecp.gsy.dcs.zirkapp.app.util.locations.ManagerGPS;

import org.apache.http.HttpStatus;
import org.json.JSONArray;

import java.util.ArrayList;


/**
 * Created by Elder on 15/07/2014.
 */
public class DownloadZimessTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "com.ecp.gsy.dcs.zirkapp.app.util.task.DownloadZimessTask";
    private ArrayList<Zimess> data;
    private AdapterZimess adapter;
    private String url;
    private MenuItem progressBarItemMenu;
    private Context context;
    private boolean isApiOnline;
    private int httpStatusCode;
    private int CantNewZimess = 0;
    private ManagerGPS managerGPS;

    public DownloadZimessTask(Context context, MenuItem menuItem, AdapterZimess adapter, String url) {
        this.context = context;
        this.adapter = adapter;
        this.url = url;
        progressBarItemMenu = menuItem;
    }

    @Override
    protected void onPreExecute() {
        //Mostramos el progressbaritem en la barra
        if (progressBarItemMenu != null) {
            progressBarItemMenu.setActionView(R.layout.progressbar);
            progressBarItemMenu.expandActionView();
        }
        managerGPS = new ManagerGPS(context.getApplicationContext());
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... params) {
        //Obtener preferencias de Distancia en Metros
        //TODO PEndiente obtener preferencias de Distancia
        int distMin = 0;
        int distMax = 5000;
        //Obtener Ubicacion

        String paramsForUrl = managerGPS.getLatitud() + ";" + managerGPS.getLongitud() + ";" + distMin + ";" + distMax;
        //Creamos un objecto que conectar a la URL y analizar su contenido
        ConectorHttpJSON conn = new ConectorHttpJSON(url + paramsForUrl);
        try {
            if (isApiOnline = conn.executeGet()) {
                //Obtenemos el JSON
                JSONArray jsonArray = conn.getJsonArray();
                //Analizamos el JSON y tomamos lo deseado
                data = new JSONToStringCollection(jsonArray).getArrayList();
                //Obtenemos el code http
                httpStatusCode = conn.getHttpStatusCode();
            }
        } catch (Exception e) {
            Log.e("ConectorHttpJSON GET", e.getLocalizedMessage());
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        //Añadimos todos los links del adapter
        if (data != null) {
            for (Zimess tmp : data) {
                adapter.add(tmp);
                CantNewZimess++;
            }
            //Indicamos al adapter que ha cambiado para que refresque el Listview
            adapter.notifyDataSetChanged();
            //Establecer valores en el Home
            setValuesHome();
        }

        //Restauramos el progressbaritem en la barra
        if (progressBarItemMenu != null) {
            progressBarItemMenu.collapseActionView();
            progressBarItemMenu.setActionView(null);
        }
        //Personalizamos el mensaje en UI
        //TODO Se Sugiere Manejar un Dialog
        if (!isApiOnline && httpStatusCode == 0) {//Sin conexion

            Toast.makeText(context, R.string.msgOutConexion, Toast.LENGTH_LONG).show();

        } else if (httpStatusCode != HttpStatus.SC_OK && httpStatusCode != 0) { //Problemas con la API

            String msg = new StringBuilder(context.getResources().getString(R.string.msgErrorGral)).append(" ").append(httpStatusCode).toString();
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();

        } else if (CantNewZimess == 0 && httpStatusCode == HttpStatus.SC_OK) {// No se obtuvieron Zimess

            //TODO Personalizar Toask (Permitir dar click para escribir mensaje)
            Toast.makeText(context, R.string.msgNoLoadZimess, Toast.LENGTH_SHORT).show();
        }
        super.onPostExecute(result);
    }

    //Envia el broadcast
    private void setValuesHome() {
        //TODO la lista solo muestra la cantidad de Zmess que llegan en la ultima consulta (Debe mostrar lo que hay cerca, "lo que tiene la lista")
        //Actualiza cantidad de mensajes cerca
        Intent intent = new Intent("actualizarzmess");
        intent.putExtra("operacion", HomeReceiver.ZMESS_CARGADOS);
        intent.putExtra("datos", data.size());
        context.sendBroadcast(intent);
    }

}
