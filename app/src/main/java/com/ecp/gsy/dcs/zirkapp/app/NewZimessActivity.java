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
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.ecp.gsy.dcs.zirkapp.app.util.beans.Zimess;
import com.ecp.gsy.dcs.zirkapp.app.util.locations.ManagerGPS;
import com.ecp.gsy.dcs.zirkapp.app.util.task.GlobalApplication;
import com.ecp.gsy.dcs.zirkapp.app.util.task.LoadZimessTask;

public class NewZimessActivity extends Activity implements View.OnClickListener {

    private EditText message;
    private ImageButton btnSendZimess;
    private TextView txtIndicadorConn;
    private SeekBar seekBarDuraZimess;
    private String usuario = "1"; //TODO Manipular USuario que hace la publicacion
    private boolean isInternetConected;
    private ToggleButton isUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_zimess);
        getActionBar().setDisplayHomeAsUpEnabled(true);


        //Creando UI
        message = (EditText) findViewById(R.id.editText);
        seekBarDuraZimess = (SeekBar) findViewById(R.id.seekBarTimeOut);
        btnSendZimess = (ImageButton) findViewById(R.id.btnSendZmess);
        btnSendZimess.setEnabled(false);
        txtIndicadorConn = (TextView) findViewById(R.id.txtIndicadorConn);
        isUpdate = (ToggleButton) findViewById(R.id.chkZimessUpdate);

        //TODO Implementar metodo en segundo plano para verificar conexion
        if (isConected()) {
            txtIndicadorConn.setVisibility(View.GONE);
        } else {
            txtIndicadorConn.setBackgroundColor(Color.RED);
            txtIndicadorConn.setText(R.string.msgDisconnet);
            txtIndicadorConn.setVisibility(View.VISIBLE);
        }

        //Recuper el zimess no enviado.
       Zimess zimessNoti = (Zimess) getIntent().getSerializableExtra("zimess_noti");
        //Restablece el mensaje que no se pude enviar
        if (zimessNoti != null) {
            message.setText(zimessNoti.getZimess());
            btnSendZimess.setEnabled(true);
            isUpdate.setChecked(zimessNoti.isUpdate());
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
                if (message.getText().length() >= 4 && isInternetConected) {
                    btnSendZimess.setEnabled(true);
                } else {
                    btnSendZimess.setEnabled(false);
                }
            }
        });

        btnSendZimess.setOnClickListener(this);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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

    /**
     * Enviar el Zimess via POST
     */
    private void sendZmessPost() {
        Zimess zimess = new Zimess();
        zimess.setZimess(message.getText().toString());
        zimess.setUsuario(usuario);
        zimess.setUpdate(isUpdate.isChecked());
        zimess.setMinutosDuracion(seekBarDuraZimess.getProgress());
        //Tomar ubicacion
        ManagerGPS managerGPS = new ManagerGPS(getApplicationContext());
        zimess.setLatitud(managerGPS.getLatitud());
        zimess.setLongitud(managerGPS.getLongitud());
        //Cargarlo a la API
        new LoadZimessTask(this, zimess, GlobalApplication.URL_API_PYTHON).execute();
        onBackPressed();
    }

    /**
     * Verificar si hay conexion a Internet
     */
    public boolean isConected() {
        isInternetConected = false;
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            isInternetConected = true;
        }
        return isInternetConected;
    }
}
