package com.ecp.gsy.dcs.zirkapp.app;

import android.app.Activity;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ecp.gsy.dcs.zirkapp.app.util.beans.Zimess;
import com.ecp.gsy.dcs.zirkapp.app.util.task.LoadZimessTask;

public class NewZimessActivity extends Activity implements View.OnClickListener {

    //Url de la API
    private final static String URL = "http://zirkapp.byethost3.com/api/v1.1/zsms";

    private EditText message;
    private ImageButton btnSendZmess;
    private TextView txtIndicadorConn;
    private String userTemp = "zirkapp_developer"; //TODO USuario de publicaciones temporales
    private boolean apiConected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_zimess);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        //Creando UI
        message = (EditText) findViewById(R.id.editText);

        btnSendZmess = (ImageButton) findViewById(R.id.btnSendZmess);
        btnSendZmess.setEnabled(false);
        txtIndicadorConn = (TextView) findViewById(R.id.txtIndicadorConn);

        //TODO Implementar metodo en segundo plano para verificar conexion
        if (isConected()) {
            txtIndicadorConn.setText(null);
        } else {
            txtIndicadorConn.setBackgroundColor(Color.RED);
            txtIndicadorConn.setText(R.string.msgDisconnet);
        }

        message.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (message.getText().length() >= 4 && apiConected) {
                        btnSendZmess.setEnabled(true);
                }else{
                    btnSendZmess.setEnabled(false);
                }
            }
        });

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
                sendZmessPost();
                break;
            default:
                return;
        }
    }

    private void sendZmessPost() {
        Zimess zimess = new Zimess();
        zimess.setZmessage(message.getText().toString());
        zimess.setZuser(userTemp);
        new LoadZimessTask(this, zimess, URL).execute();
        onBackPressed();
    }

    //Verificar si hay conexion a Internet
    public boolean isConected() {
        apiConected = false;
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            apiConected = true;
        }
        return apiConected;
    }
}
