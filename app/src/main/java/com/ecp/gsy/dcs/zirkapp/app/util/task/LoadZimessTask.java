package com.ecp.gsy.dcs.zirkapp.app.util.task;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;

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
    private boolean isApiOnline;
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
        if (isApiOnline = conn.executePost(zimess)) {

        }
        httpStatusCode = conn.getHttpStatusCode();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (httpStatusCode == HttpStatus.SC_OK) {
            String nameapp = context.getResources().getString(R.string.app_name);
            //Notificando
           /* NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            Notification notification = new Notification(android.R.drawable.stat_sys_upload_done, nameapp, System.currentTimeMillis());
            PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(), 0, new Intent(), 0);
            notification.setLatestEventInfo(context.getApplicationContext(), "Zimess Enviado", "Mensaje enviado correctamente!", pendingIntent);
            notificationManager.notify(0, notification);
            */

            //Otra notificacion
            NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(context)
                    .setSmallIcon(android.R.drawable.stat_sys_upload_done)
                    .setContentTitle(nameapp)
                    .setContentText(context.getResources().getString(R.string.msgSend));
            //Crear intent para manipular la noti en Zirkapp
            /*Intent intent = new Intent(context, MainActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addParentStack(MainActivity.class);
            stackBuilder.addNextIntent(intent);
            PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            nBuilder.setContentIntent(pendingIntent);
            */
            //No ir a Zirkapp
            PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(), 0, new Intent(), PendingIntent.FLAG_CANCEL_CURRENT);
            nBuilder.setContentIntent(pendingIntent);
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            int mId = 990; // id de la notificacion, permite actulizarla mas adelante
            notificationManager.notify(mId, nBuilder.build());

        }
    }
}
