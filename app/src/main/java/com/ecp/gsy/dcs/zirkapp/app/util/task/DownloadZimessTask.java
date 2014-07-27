package com.ecp.gsy.dcs.zirkapp.app.util.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.HomeReceiver;
import com.ecp.gsy.dcs.zirkapp.app.util.JSONToStringCollection;
import com.ecp.gsy.dcs.zirkapp.app.util.adapters.AdapterZimess;
import com.ecp.gsy.dcs.zirkapp.app.util.beans.Zimess;
import com.ecp.gsy.dcs.zirkapp.app.util.http.ConectorHttpJSON;

import org.apache.http.HttpStatus;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashSet;


/**
 * Created by Elder on 15/07/2014.
 */
public class DownloadZimessTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "com.ecp.gsy.dcs.zirkapp.app.util.task.DownloadZimessTask";
    private ArrayList<Zimess> data;
    private AdapterZimess adapter;
    private String url;
    private ProgressDialog progressDialog;
    private Context context;
    private boolean isApiOnline;
    private int httpStatusCode;

    public DownloadZimessTask(Context context, AdapterZimess adapter, String url) {
        this.adapter = adapter;
        this.url = url;
        progressDialog = new ProgressDialog(context);
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        //Config para mostrar el ProgressDialog "Cargando contenido"
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(context.getResources().getString(R.string.msgProgressDialog));
        progressDialog.setTitle(context.getResources().getString(R.string.msgLoading));
        //Mostrar el Dialog
        progressDialog.show();
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... params) {
        //Creamos un objecto que conectar a la URL y analizar su contenido
        ConectorHttpJSON conn = new ConectorHttpJSON(url);
        try {
            if (isApiOnline = conn.executeGet()) {
                //Obtenemos el JSON
                JSONArray jsonArray = conn.getJsonArray();
                //Analizamos el JSON y tomamos lo deseado
                data = new JSONToStringCollection(jsonArray).getArrayList();
            }
            //Obtenemos el code http
            httpStatusCode = conn.getHttpStatusCode();
        } catch (Exception e) {
            Log.e("jsonArray", e.getLocalizedMessage());
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        //Añadimos todos los links del adapter
        if (data != null) {
            //Evitar duplicados
            HashSet hashSet = new HashSet();
            hashSet.addAll(data);
            data.clear();
            data.addAll(hashSet);
            for (Zimess tmp : data) {
                adapter.add(tmp);
            }
            //Indicamos al adapter que ha cambiado para que refresque el Listview
            adapter.notifyDataSetChanged();
            setValuesHome();
        }
        //Eliminanos el ProgressDialog
        progressDialog.dismiss();
        //Personalizamos el mensaje en UI
        //TODO Se Sugiere Manejar un Dialog
        if (!isApiOnline && httpStatusCode == 0) {
            Toast.makeText(context, R.string.out_conexion, Toast.LENGTH_LONG).show();
        } else if (httpStatusCode != HttpStatus.SC_OK && httpStatusCode != 0) {
            String msg = new StringBuilder(context.getResources().getString(R.string.error_conexion)).append(" ").append(httpStatusCode).toString();
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
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
