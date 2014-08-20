package com.ecp.gsy.dcs.zirkapp.app.util;

import com.ecp.gsy.dcs.zirkapp.app.util.beans.Zimess;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Elder on 15/07/2014.
 */
public class JSONToStringCollection {
    JSONArray jArray;

    public JSONToStringCollection(JSONArray jsonArray) {
        this.jArray = jsonArray;
    }

    public ArrayList<Zimess> getArrayList() throws JSONException {
        ArrayList<Zimess> data = new ArrayList<Zimess>();
        if (!jArray.equals(new JSONArray())) {
            //Obetenemos el valor de message:
            for (int i = 0; i < jArray.length(); i++) {
                Zimess zimess = new Zimess();
                //Obtieniendo Datos
                JSONObject jsonObj = jArray.getJSONObject(i);
                zimess.setId(jsonObj.getInt("id"));
                zimess.setUsuario(jsonObj.getString("usuario")+"");
                zimess.setLatitud(jsonObj.getDouble("latitud"));
                zimess.setLongitud(jsonObj.getDouble("longitud"));
                zimess.setZimess(jsonObj.getString("zimess"));
                zimess.setMinutosDuracion(jsonObj.getInt("duracion"));
                zimess.setUpdate(jsonObj.getBoolean("actualizable"));
                //zimess.setFechaCreated(new Date(jsonObj.getString("fcreate")));
                //zimess.setFechaUpdate(new Date(jsonObj.getString("fupdate")));
                //Agregando datos al Array
                data.add(zimess);
            }
        }
        return data;
    }
}
