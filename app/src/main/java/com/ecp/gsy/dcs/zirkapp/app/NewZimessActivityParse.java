package com.ecp.gsy.dcs.zirkapp.app;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.ecp.gsy.dcs.zirkapp.app.util.locations.ManagerGPS;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.text.ParsePosition;

/**
 * Created by Elder on 23/02/2015.
 */
public class NewZimessActivityParse extends Activity {

    private String currentUserId;

    private EditText message;
    private SeekBar seekBarDuraZimess;
    private ImageButton btnSendZimess;
    private TextView txtIndicadorConn;
    private ToggleButton isUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_zimess);
        inicializarCompUI();
        currentUserId = ParseUser.getCurrentUser().getObjectId();
    }



    private void inicializarCompUI() {
        //Creando UI
        message = (EditText) findViewById(R.id.editText);
        seekBarDuraZimess = (SeekBar) findViewById(R.id.seekBarTimeOut);
        btnSendZimess = (ImageButton) findViewById(R.id.btnSendZmess);
        //btnSendZimess.setEnabled(false);
        btnSendZimess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendZmessPost(message.getText().toString());
            }
        });
        txtIndicadorConn = (TextView) findViewById(R.id.txtIndicadorConn);
        isUpdate = (ToggleButton) findViewById(R.id.chkZimessUpdate);
    }


    private void sendZmessPost(String zimessText) {
        //Tomar ubicacion
        ManagerGPS managerGPS = new ManagerGPS(getApplicationContext());
        ParseGeoPoint parseGeoPoint = new ParseGeoPoint(managerGPS.getLatitud(), managerGPS.getLongitud());
        ParseObject parseObject = new ParseObject("ParseZimess");
        parseObject.put("userId", currentUserId);
        parseObject.put("zimessText", zimessText);
        parseObject.put("location", parseGeoPoint);
        parseObject.saveInBackground();

    }
}
