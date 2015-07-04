package com.ecp.gsy.dcs.zirkapp.app.activities;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ecp.gsy.dcs.zirkapp.app.GlobalApplication;
import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.fragments.ZimessFragment;
import com.ecp.gsy.dcs.zirkapp.app.util.beans.Zimess;
import com.ecp.gsy.dcs.zirkapp.app.util.locations.Location;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.models.ParseZimess;
import com.ecp.gsy.dcs.zirkapp.app.util.services.LocationService;
import com.ecp.gsy.dcs.zirkapp.app.util.task.RefreshDataAddressTask;
import com.gc.materialdesign.views.ButtonRectangle;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;

/**
 * Created by Elder on 23/02/2015.
 */
public class NewZimessActivity extends ActionBarActivity {

    private ParseUser currentUser;

    private EditText message;
    private ButtonRectangle btnSendZimess;
    private TextView txtIndicadorConn;
    private TextView lblCurrentLocation;

    private GlobalApplication globalApplication;
    private ProgressBar progressBar;
    private Activity activity;
    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_zimess);

        globalApplication = (GlobalApplication) getApplicationContext();

        activity = this;

        inicializarCompUI();

        currentUser = globalApplication.getCurrentUser();

        //Name Location
        if (LocationService.isRunning()) {
            new RefreshDataAddressTask(this, getCurrentLocation(), lblCurrentLocation, progressBar).execute();
        }
    }


    private void inicializarCompUI() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
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


        btnSendZimess = (ButtonRectangle) findViewById(R.id.btnSendZmess);
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
        //lblCurrentLocation.setText(globalApplication.getNameLocation());
        progressBar = (ProgressBar) findViewById(R.id.progressLoad);

        //Recuper el zimess no enviado.
        Zimess zimessNoti = (Zimess) getIntent().getSerializableExtra("zimess_noti");
        //Restablece el mensaje que no se pude enviar
        if (zimessNoti != null) {
            message.setText(zimessNoti.getZimessText());
            btnSendZimess.setEnabled(true);
        }
    }


    private void sendZmessPost(final String zimessText) {
        if (zimessText.length() < 4) {
            Toast.makeText(this,
                    "Escribe mínimo 4 caracteres!",
                    Toast.LENGTH_LONG).show();
            return;
        }

        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage(getResources().getString(R.string.msgZimessSending));
        dialog.show();
        btnSendZimess.setEnabled(false);

        //Tomar ubicacion
        final Location currentLocation = getCurrentLocation();
        if (currentLocation != null) {
            ParseGeoPoint parseGeoPoint = new ParseGeoPoint(currentLocation.getLatitud(), currentLocation.getLongitud());
            ParseZimess zimess = new ParseZimess();
            zimess.put(ParseZimess.USER, currentUser);
            zimess.put(ParseZimess.ZIMESS_TEXT, zimessText);
            zimess.put(ParseZimess.LOCATION, parseGeoPoint);
            zimess.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        if (ZimessFragment.isRunning()) {
                            ZimessFragment zf = ZimessFragment.getInstance();
                            zf.findZimessAround(currentLocation, globalApplication.getSortZimess());
                        }
                        /*Intent intent = new Intent();
                        intent.putExtra("newZimessOk", true);
                        setResult(Activity.RESULT_OK, intent);*/
                        finish();
                    } else {
                        showNotificacion(true, 0, null, getResources().getString(R.string.msgZimesFailed), zimessText);
                        Log.e("ZimessError:", "No es posible publicar el Zimess");
                        onBackPressed();
                    }
                    dialog.dismiss();
                }
            });
        } else {
            btnSendZimess.setEnabled(true);
        }

    }

    /**
     * retorna la Ubicacion actual
     *
     * @return
     */
    private Location getCurrentLocation() {
        Location location = null;
        if (LocationService.isRunning()) {
            LocationService locationService = LocationService.getInstance();
            android.location.Location tmpLocation = locationService.getCurrentLocation(true);
            location = new Location(tmpLocation.getLatitude(), tmpLocation.getLongitude());
        }
        return location;
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
            Intent intent = new Intent(context, NewZimessActivity.class);
            intent.putExtra("zimess_noti", zimessText);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addParentStack(NewZimessActivity.class);
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
    public void onBackPressed() {
        super.onBackPressed();
    }
}
