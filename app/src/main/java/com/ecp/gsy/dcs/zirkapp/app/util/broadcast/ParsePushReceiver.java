package com.ecp.gsy.dcs.zirkapp.app.util.broadcast;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.ecp.gsy.dcs.zirkapp.app.MainActivity;
import com.ecp.gsy.dcs.zirkapp.app.R;
import com.parse.ParseFile;
import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Elder on 24/04/2015.
 */
public class ParsePushReceiver extends ParsePushBroadcastReceiver {

    private NotificationManager notificationManager;
    public static final int NOTIFICATION_ID = 1;
    private Bitmap imgLargeIcon;

    @Override
    protected Notification getNotification(Context context, Intent intent) {
        return null;
    }

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        JSONObject data = null;
        try {
            data = new JSONObject(bundle.getString("com.parse.Data"));
            String message = data.getString("alert");
            String title = data.getString("title");
            byte[] byteImage = (byte[]) data.get("avatar");
            if (byteImage != null) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPurgeable = true;
                Bitmap bitmap1 = BitmapFactory.decodeByteArray(byteImage, 0, byteImage.length, options);
                if (bitmap1 != null)
                    imgLargeIcon = bitmap1;
            }
            //Enviar notificacion
            sendNotification(message, title, context);
        } catch (JSONException e) {
            Log.e("push.json.receiver", e.getMessage());
            try {
                //Enviar sin title
                if (data != null && (e.getMessage().contains("title") || e.getMessage().contains("avatar"))) {
                    sendNotification(data.getString("alert"), null, context);
                }
            } catch (JSONException e1) {
                Log.e("push.json.receiver", e1.getMessage());
            }

        }
        super.onPushReceive(context, intent);
        //Log.d("bundlePush", bundle.toString());
    }

    private void sendNotification(String msg, String title, Context context) {
        String[] messages = msg.split("-:-");
        msg = msg.contains("-:-") ? msg.substring(0, msg.length() - 4) : msg; //Limpiar mensaje
        int typeNotification = 0;
        if (messages.length > 1)
            typeNotification = Integer.parseInt(messages[messages.length - 1]);
        //Notificacion
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        switch (typeNotification) {
            case 1:
                intent.setAction("OPEN_FRAGMENT_USER"); //Notificacion desde chat
                break;
            case 2:
                intent.setAction("OPEN_FRAGMENT_NOTI"); //Notificacion desde comentarios
                break;
        }

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_zirkapp_noti)
                .setLargeIcon(imgLargeIcon != null ? imgLargeIcon : BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher))
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentTitle(title != null ? title : "Zirkapp")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                .setContentText(msg)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis());

        nBuilder.setContentIntent(contentIntent);
        notificationManager.notify(NOTIFICATION_ID, nBuilder.build());
    }
}

