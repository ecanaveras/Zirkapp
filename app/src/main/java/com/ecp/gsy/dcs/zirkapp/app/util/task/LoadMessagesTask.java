package com.ecp.gsy.dcs.zirkapp.app.util.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.HomeReceiver;
import com.ecp.gsy.dcs.zirkapp.app.util.JSONToStringCollection;
import com.ecp.gsy.dcs.zirkapp.app.util.http.ConectorHttpJSON;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;


/**
 * Created by Elder on 15/07/2014.
 */
public class LoadMessagesTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "com.ecp.gsy.dcs.zirkapp.app.util.task.LoadMessagesTask";
    private ArrayList data;
    private ArrayAdapter adapter;
    private String url;
    private ProgressDialog progressDialog;
    private Context context;

    public LoadMessagesTask(Context context, ArrayAdapter adapter, String url) {
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
            //Obtenemos el JSON
            JSONArray jsonArray = conn.execute();

            //Analizamos el JSON y tomamos lo deseado
            data = new JSONToStringCollection(jsonArray).getArrayList();
        } catch (Exception e) {
            Log.e("jsonArray", e.getLocalizedMessage());
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        //Log.i(TAG, result.toString());
        //Añadimos todos los links del adapter
        //Log.v("Data", data.toString());
        for (Object tmp : data) {
            adapter.add(tmp);
        }
        //Indicamos al adapter que ha cambiado para que refresque el Listview
        adapter.notifyDataSetChanged();

        setValuesHome();
        //Eliminanos el ProgressDialog
        progressDialog.dismiss();

        super.onPostExecute(result);
    }

    //Envia el broadcast
    private void setValuesHome() {
        //Actualiza cantidad de mensajes cerca
        Intent intent = new Intent("actualizarzmess");
        intent.putExtra("operacion", HomeReceiver.ZMESS_CARGADOS);
        intent.putExtra("datos", data.size());
        context.sendBroadcast(intent);
    }

}
