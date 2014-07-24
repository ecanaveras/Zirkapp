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
    private JSONArray array;

    public JSONToStringCollection(JSONArray jsonArray) {
        this.jArray = jsonArray;
    }

    public ArrayList<Zimess> getArrayList() throws JSONException {
        ArrayList<Zimess> data = new ArrayList<Zimess>();
        if (!jArray.equals(new JSONArray())) {
            //Del documento JSON extraemos el array "items", que contiene una colección de publicaciones
            Log.i("jSonObject", jArray.toString());
            //JSONArray array = jArray.getJSONArray("");
            //Obetenemos el valor de message:
            for (int i = 0; i < jArray.length(); i++) {
                Zimess zimess = new Zimess();
                JSONObject jObj = jArray.getJSONObject(i);
                zimess.setZmessage(jObj.getString("zmessage"));
                zimess.setZuser(jObj.getString("zuser"));
                data.add(zimess);
            }
        }
        return data;
    }
}
