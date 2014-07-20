package com.ecp.gsy.dcs.zirkapp.app.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by Elder on 16/07/2014.
 */
public class HomeReceiver extends BroadcastReceiver {

    public static final int ZMESS_CARGADOS = 1;
    public static final int DISTANCIA_ACTUALIZADA = 2;
    public static final int INBOX_CARGADOS = 3;

    private final TextView cantZMess;

    public HomeReceiver(TextView cantZMess) {
        this.cantZMess = cantZMess;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int operacion = intent.getIntExtra("operacion", -1);
        switch (operacion){
            case ZMESS_CARGADOS: cargarZmessAPI(intent); break;
            case DISTANCIA_ACTUALIZADA: actualizarInfoDistancia(intent); break;
            case INBOX_CARGADOS: actualizarInfoInbox(intent); break;
        }
    }

    private void cargarZmessAPI(Intent intent) {
        Integer cantZmess = (Integer) intent.getSerializableExtra("datos");
        cantZMess.setText(cantZmess.toString());
    }

    private void actualizarInfoDistancia(Intent intent) {
        int dist = (Integer) intent.getSerializableExtra("datos");
    }

    private void actualizarInfoInbox(Intent intent) {
        int cantInbox = (Integer) intent.getSerializableExtra("datos");
    }
}
