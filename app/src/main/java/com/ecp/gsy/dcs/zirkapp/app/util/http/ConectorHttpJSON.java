package com.ecp.gsy.dcs.zirkapp.app.util.http;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.ecp.gsy.dcs.zirkapp.app.MainActivity;
import com.ecp.gsy.dcs.zirkapp.app.R;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by Elder on 15/07/2014.
 */
public class ConectorHttpJSON {

    private String url;
    private JSONArray jsonArray;

    public ConectorHttpJSON(String url) {
        this.url = url;
    }

    public boolean execute() throws JSONException {
        //Se ejecuta la peticion y se almacena la respuesta
        HttpGet httpGet = new HttpGet(url);
        HttpParams httpParams = new BasicHttpParams();
        //Establecer tiempo de conexion a la url en milisegundos
        HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
        //Establecer tiempo de respuesta para data
        HttpConnectionParams.setSoTimeout(httpParams, 10000);
        //Creamos el objeto cliente que realiza la peticion al servidor
        HttpClient client = new DefaultHttpClient(httpParams);
        try {
            HttpResponse response = client.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            Log.i("Response status", response.getStatusLine().toString());
            Log.i("Response code", statusCode + "");
            if (statusCode == HttpStatus.SC_OK) {
                //Recogemos la respuesta del servidor
                String JSONResponse = inputStreamToString(response.getEntity().getContent());

                JSONArray jsonArrayTmp = new JSONArray(JSONResponse);
                this.jsonArray = jsonArrayTmp;

                return true;
            } else if (statusCode == HttpStatus.SC_BAD_REQUEST) {

            }
        } catch (IOException c) {
            Log.e("http Exception", c.getLocalizedMessage());
        }
        return false;
    }

    private String inputStreamToString(InputStream is) throws UnsupportedEncodingException {
        String line = "";
        StringBuilder sb = new StringBuilder();
        //Guardamos la direccion en buffer de lectura
        BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
        //Leemos toda la direccion
        try {
            while ((line = br.readLine()) != null) {
                sb.append(line.trim());
            }
        } catch (Exception e) {
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

    public JSONArray getJsonArray() {
        return jsonArray;
    }
}
