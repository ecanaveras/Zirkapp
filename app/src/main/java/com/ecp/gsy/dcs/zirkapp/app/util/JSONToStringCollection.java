package com.ecp.gsy.dcs.zirkapp.app.util;

import android.util.Log;

import com.ecp.gsy.dcs.zirkapp.app.util.beans.Zimess;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

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
            //Log.i("jSonObject", jArray.toString());
            //Obetenemos el valor de message:
            for (int i = 0; i < jArray.length(); i++) {
                Zimess zimess = new Zimess();
                //Obtieniendo Datos
                JSONObject jsonObj = jArray.getJSONObject(i);
                zimess.setZid(jsonObj.getInt("id"));
                zimess.setZmessage(jsonObj.getString("zmessage"));
                zimess.setZuser(jsonObj.getString("zuser"));
                zimess.setFechaCreated(jsonObj.getString("created_at"));
                //Agregando datos al Array
                data.add(zimess);
            }
        }
        return data;
    }
}
