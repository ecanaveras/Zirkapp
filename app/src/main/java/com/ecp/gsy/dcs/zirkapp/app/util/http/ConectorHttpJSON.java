package com.ecp.gsy.dcs.zirkapp.app.util.http;

import android.util.Base64;
import android.util.Log;

import com.ecp.gsy.dcs.zirkapp.app.util.beans.Zimess;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
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
    private JSONArray jsonArray;
    private int httpStatusCode;
    //Login en la APi
    private String auth = Base64.encodeToString(("ecanaveras:ecanave").getBytes(), Base64.NO_WRAP);


    public ConectorHttpJSON(String url) {
        this.url = url;
    }

    /**
     * Realiza la peticion de Zimess en la API
     *
     * @return true en caso de exito, false en caso de falla
     * @throws JSONException
     */
    public boolean executeGet() throws JSONException {
        try {
            //Se ejecuta la peticion y se almacena la respuesta
            HttpGet httpGet = new HttpGet(url);
            //Enviar Headers
            httpGet.addHeader("Authorization", "Basic " + auth);
            //Parametros
            HttpParams httpParams = new BasicHttpParams();
            //Param1. Establecer tiempo de conexion a la url en milisegundos
            HttpConnectionParams.setConnectionTimeout(httpParams, 4000);
            //Param2. Establecer tiempo de respuesta para data
            HttpConnectionParams.setSoTimeout(httpParams, 10000);
            //Creamos el objeto cliente que realiza la peticion al servidor
            HttpClient client = new DefaultHttpClient(httpParams);

            //Ejecutar API
            HttpResponse response = client.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            Log.i("Response status", response.getStatusLine().toString());
            Log.i("Response code", statusCode + "");
            //Log.i("Response user-auth", EntityUtils.toString(response.getEntity()));
            //Si OK, se procede
            if (statusCode == HttpStatus.SC_OK) {
                //Recogemos la respuesta del servidor
                String JSONResponse = inputStreamToString(response.getEntity().getContent());

                JSONArray jsonArrayTmp = new JSONArray(JSONResponse);
                this.jsonArray = jsonArrayTmp;
                this.httpStatusCode = statusCode;
                return true;
            }
            //Se establece el estatus code para manejo de mensajes en UI
            this.httpStatusCode = statusCode;

        } catch (IOException c) {
            Log.e("http Exception", c.getLocalizedMessage());
        }
        return false;
    }

    /**
     * Realiza un accion Post en la API - Agrega un nuevo Zimess
     *
     * @return true en caso de exito, false en caso de falla
     */
    public boolean executePost(Zimess zimess) {
        try {
            //TODO Enviar parametros de timeout en la peticion http
            //1. Crear la peticion
            HttpClient httpClient = new DefaultHttpClient();
            //2. Crear Post
            HttpPost httpPost = new HttpPost(url);
            //3.Generar Zimess Json
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("usuario", 1);
            jsonObject.accumulate("latitud", zimess.getLatitud());
            jsonObject.accumulate("longitud", zimess.getLongitud());
            jsonObject.accumulate("zimess", zimess.getZimess());
            jsonObject.accumulate("duracion", zimess.getMinutosDuracion());
            jsonObject.accumulate("actualizable", zimess.isUpdate());
            //4.Json to StringEntity
            StringEntity stringEntity = new StringEntity(jsonObject.toString());
            //5. Establecer httPost Entity
            httpPost.setEntity(stringEntity);
            //6. Establecer headers
            httpPost.addHeader("Accept", "application/json");
            httpPost.addHeader("Content-type", "application/json");
            httpPost.addHeader("Authorization", "Basic " + auth);
            //7. Ejecutar Post
            HttpResponse response = httpClient.execute(httpPost);
            //8. Recibir respuesta
            //TODO Terminar codigo para Recibir Json
            int responseCode = response.getStatusLine().getStatusCode();
            Log.i("Response status", response.getStatusLine().toString());
            Log.i("Response code", responseCode + "");
            Log.i("Response user-auth", EntityUtils.toString(response.getEntity()));
            if (responseCode == HttpStatus.SC_OK) {
                //TODO Refrescar el adparter para mostrar el nuevo mensaje.
                this.httpStatusCode = responseCode;
                return true;
            }
            this.httpStatusCode = responseCode;
        } catch (IOException e) {
            Log.e("Error al enviar Json", e.getLocalizedMessage());
        } catch (JSONException e) {
            Log.e("Error al crear Json", e.getLocalizedMessage());
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

    public int getHttpStatusCode() {
        return httpStatusCode;
    }
}
