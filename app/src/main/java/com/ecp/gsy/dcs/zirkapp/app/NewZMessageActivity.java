package com.ecp.gsy.dcs.zirkapp.app;

import android.app.Activity;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class NewZMessageActivity extends Activity implements View.OnClickListener {

    private EditText message;
    private ImageButton btnSendZmess;
    private TextView txtIndicadorConn;
    private String userTemp = "zirkapp_developer"; //TODO USuario de publicaciones temporales

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_zmessage);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        //Creando UI
        message = (EditText) findViewById(R.id.editText);
        btnSendZmess = (ImageButton) findViewById(R.id.btnSendZmess);

        txtIndicadorConn = (TextView) findViewById(R.id.txtIndicadorConn);

        //TODO Implementar metodo en segundo plano para verificar conexion
        if (!isConected()) {
            txtIndicadorConn.setBackgroundColor(Color.RED);
            txtIndicadorConn.setText(R.string.msgDisconnet);
            btnSendZmess.setEnabled(false);
        } else {
            btnSendZmess.setEnabled(true);
            txtIndicadorConn.setText(null);
        }

        btnSendZmess.setOnClickListener(this);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.messages_activity_action, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnSendZmess:
                sendZmessPost("http://zirkapp.byethost3.com/api/v1.1/zsms");
                break;
            default:
                return;
        }
    }

    private void sendZmessPost(String url) {
        //TODO Pasar por parametro la clase zmensaje
        //InputStream inputStream = null;
        //String result = null;
        try {
            //TODO Enviar parametros de timeout en la peticion http
            //1. Crear la peticion
            HttpClient httpClient = new DefaultHttpClient();
            //2. Crear Post
            HttpPost httpPost = new HttpPost(url);
            //3.Generar Json
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("zmessage", message.getText());
            jsonObject.accumulate("zuser", userTemp);
            //4.Json to StringEntity
            StringEntity stringEntity = new StringEntity(jsonObject.toString());
            //5. Establecer httPost Entity
            httpPost.setEntity(stringEntity);
            //6. Establecer headers
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            //7. Ejecutar Post
            HttpResponse response = httpClient.execute(httpPost);
            //8. Recibir respuesta
            //TODO Terminar codigo para Recibir Json
            int responseCode = response.getStatusLine().getStatusCode();
            if (responseCode == HttpStatus.SC_OK) {
                Toast.makeText(this, R.string.msgSend, Toast.LENGTH_SHORT).show();
                onBackPressed();
                //TODO Refrescar el adparter para mostrar el nuevo mensaje.
            } else {
                String msg = new StringBuilder(this.getResources().getString(R.string.msgError)).append(" ").append(responseCode).toString();
                Log.d("MSG", msg);
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            Log.e("Error al enviar Json", e.getLocalizedMessage());
        } catch (JSONException e) {
            Log.e("Error al crear Json", e.getLocalizedMessage());
        }


    }

    //Verificar si hay conexion a Internet
    public boolean isConected() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }
}
