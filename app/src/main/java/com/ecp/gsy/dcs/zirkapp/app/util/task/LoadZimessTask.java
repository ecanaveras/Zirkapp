package com.ecp.gsy.dcs.zirkapp.app.util.task;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.beans.Zimess;
import com.ecp.gsy.dcs.zirkapp.app.util.http.ConectorHttpJSON;

/**
 * Created by Elder on 22/07/2014.
 */
public class LoadZimessTask extends AsyncTask<Void, Void, Void> {

    private String url;
    private Context context;
    private boolean isApiOnline;
    private Zimess zimess;
    private int httpStatusCode;

    public LoadZimessTask(Context context, Zimess zimess, String url) {
        this.url = url;
        this.context = context;
        this.zimess = zimess;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        //Creamos un objecto que conectar a la URL y analizar su contenido
        ConectorHttpJSON conn = new ConectorHttpJSON(url);
        if (isApiOnline = conn.executePost(zimess)) {

        }
        httpStatusCode = conn.getHttpStatusCode();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        Toast.makeText(context, R.string.msgSend, Toast.LENGTH_LONG).show();
    }
}
