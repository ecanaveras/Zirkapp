package com.ecp.gsy.dcs.zirkapp.app.util.task;

import android.app.NotificationManager;
import android.app.PendingIntent;

import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.widget.Toast;

import com.ecp.gsy.dcs.zirkapp.app.NewZimessActivity;
import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.beans.Zimess;
import com.ecp.gsy.dcs.zirkapp.app.util.http.ConectorHttpJSON;

import org.apache.http.HttpStatus;

/**
 * Created by Elder on 22/07/2014.
 */
public class LoadZimessTask extends AsyncTask<Void, Void, Void> {

    private String url;
    private Context context;
    private Zimess zimess;
    private int httpStatusCode;

    public LoadZimessTask(Context context, Zimess zimess, String url) {
        this.url = url;
        this.context = context;
        this.zimess = zimess;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        //Creamos un objecto que conectar a la URL y analizar su contenido
        ConectorHttpJSON conn = new ConectorHttpJSON(url);
        conn.executePost(zimess);
        httpStatusCode = conn.getHttpStatusCode();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        String nameapp = context.getResources().getString(R.string.app_name);
        if (httpStatusCode == HttpStatus.SC_CREATED || httpStatusCode == HttpStatus.SC_OK) {
            showNotificacion(true, 0, null, context.getResources().getString(R.string.msgZimessSend));
        } else {
            showNotificacion(false, android.R.drawable.ic_dialog_alert, nameapp, context.getResources().getString(R.string.msgZimesFailed));
        }
    }

    private void showNotificacion(boolean isOk, int icon, String title, String msg) {

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
            intent.putExtra("zimess_noti", zimess);
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
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(idNoti);
    }
}
