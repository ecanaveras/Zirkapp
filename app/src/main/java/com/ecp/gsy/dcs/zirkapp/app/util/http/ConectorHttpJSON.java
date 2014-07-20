package com.ecp.gsy.dcs.zirkapp.app.util.http;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * Created by Elder on 15/07/2014.
 */
public class ConectorHttpJSON {

    private String url;

    public ConectorHttpJSON(String url) {
        this.url = url;
    }

    public JSONArray execute() throws ClientProtocolException, IOException, IllegalStateException, JSONException{
        //Creamos el objeto cliente que realiza la peticion al servidor
        HttpClient client = new DefaultHttpClient();

        //Se ejecuta la peticion y se almacena la respuesta
        HttpResponse response = client.execute(new HttpGet(url));
        Log.i("Response status", response.getStatusLine().toString());
        //if(response)

        //Recogemos la respuesta del servidor
        String zmess = inputStreamToString(response.getEntity().getContent());

        JSONArray jsonArray = new JSONArray(zmess);

        return jsonArray;
    }

    private String inputStreamToString(InputStream is) throws UnsupportedEncodingException{
        String line = "";
        StringBuilder sb = new StringBuilder();
        //Guardamos la direccion en buffer de lectura
        BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf-8"),8);
        //Leemos toda la direccion
        try{
            while ((line = br.readLine())!=null){
                sb.append(line.trim());
            }
        }catch (Exception e){
            Log.e("inputStreamToString", e.getLocalizedMessage());
        }
        return sb.toString();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
