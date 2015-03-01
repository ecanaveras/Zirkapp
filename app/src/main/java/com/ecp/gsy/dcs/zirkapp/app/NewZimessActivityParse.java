package com.ecp.gsy.dcs.zirkapp.app;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ecp.gsy.dcs.zirkapp.app.util.beans.ZimessNew;
import com.ecp.gsy.dcs.zirkapp.app.util.locations.ManagerGPS;
import com.ecp.gsy.dcs.zirkapp.app.util.task.GlobalApplication;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by Elder on 23/02/2015.
 */
public class NewZimessActivityParse extends Activity {

    private ParseUser currentUser;

    private EditText message;
    private ImageButton btnSendZimess;
    private TextView txtIndicadorConn;
    private TextView lblCurrentLocation;

    private GlobalApplication globalApplication;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_zimess);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        globalApplication = (GlobalApplication) getApplicationContext();

        inicializarCompUI();
        currentUser = globalApplication.getCurrentUser();
    }


    private void inicializarCompUI() {
        //Creando UI
        message = (EditText) findViewById(R.id.editText);
        message.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (message.getText().toString().trim().length() >= 4) {
                    btnSendZimess.setEnabled(true);
                } else {
                    btnSendZimess.setEnabled(false);
                }
            }
        });


        btnSendZimess = (ImageButton) findViewById(R.id.btnSendZmess);
        btnSendZimess.setEnabled(false);
        btnSendZimess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendZmessPost(message.getText().toString());
            }
        });
        txtIndicadorConn = (TextView) findViewById(R.id.txtIndicadorConn);

        //Informar Ubicación
        lblCurrentLocation = (TextView) findViewById(R.id.lblCurrentLocation);
        lblCurrentLocation.setText(globalApplication.getNameLocation());

        //Recuper el zimess no enviado.
        ZimessNew zimessNoti = (ZimessNew) getIntent().getSerializableExtra("zimess_noti");
        //Restablece el mensaje que no se pude enviar
        if (zimessNoti != null) {
            message.setText(zimessNoti.getZimessText());
            btnSendZimess.setEnabled(true);
        }
    }


    private void sendZmessPost(final String zimessText) {
        final Context context = getApplicationContext();
        if (zimessText.length() < 4) {
            Toast.makeText(context,
                    "Escribe mínimo 4 caracteres!",
                    Toast.LENGTH_LONG).show();
            return;
        }

        //Tomar ubicacion
        ManagerGPS managerGPS = new ManagerGPS(context);
        ParseGeoPoint parseGeoPoint = new ParseGeoPoint(managerGPS.getLatitud(), managerGPS.getLongitud());
        ParseObject parseObject = new ParseObject("ParseZimess");
        parseObject.put("user", currentUser);
        parseObject.put("zimessText", zimessText);
        parseObject.put("location", parseGeoPoint);
        parseObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    onBackPressed();
                } else {
                    showNotificacion(true, 0, null, context.getResources().getString(R.string.msgZimesFailed), zimessText);
                    Log.e("ZimessError:", "No es posible publicar el Zimess en parse");
                    onBackPressed();
                }
            }
        });

    }

    /**
     * Muestra una notificacion indicando que el mensaje falló
     *
     * @param isOk
     * @param icon
     * @param title
     * @param msg
     * @param zimessText
     */
    private void showNotificacion(boolean isOk, int icon, String title, String msg, String zimessText) {
        Context context = getApplicationContext();
        if (!isOk) {
            //Sonido
            Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            //Notificando problema
            NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(context)
                    .setSound(soundUri)
                    .setSmallIcon(icon)
                    .setContentTitle(title)
                    .setContentText(msg)
                    .setAutoCancel(true)
                    .setWhen(System.currentTimeMillis());
            //Crear intent para manipular la noti en Zirkapp
            Intent intent = new Intent(context, NewZimessActivityParse.class);
            intent.putExtra("zimess_noti", zimessText);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addParentStack(NewZimessActivityParse.class);
            stackBuilder.addNextIntent(intent);
            PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            nBuilder.setContentIntent(pendingIntent);
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            int mId = 990; // id de la notificacion, permite actualizarla mas adelante
            notificationManager.notify(mId, nBuilder.build());
        } else {
            removeNotification(990);
            //No ir a Zirkapp
//            PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(), 0, new Intent(), PendingIntent.FLAG_CANCEL_CURRENT);
//            nBuilder.setContentIntent(pendingIntent);
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
        }

    }

    // Remove notification
    private void removeNotification(int idNoti) {
        NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(idNoti);
    }

    @Override
    protected void onResume() {
        lblCurrentLocation.setText(globalApplication.getNameLocation());
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
