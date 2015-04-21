package com.ecp.gsy.dcs.zirkapp.app.util.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.ecp.gsy.dcs.zirkapp.app.MainActivity;
import com.ecp.gsy.dcs.zirkapp.app.MessagingActivity;
import com.ecp.gsy.dcs.zirkapp.app.R;
import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * Created by Elder on 24/03/2015.
 */
public class GcmIntentService extends IntentService {

    public static final int NOTIFICATION_ID = 1;
    private NotificationManager notificationManager;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();

        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) { // has effect of unparcelling Bundle

            /** Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.*/

            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("Deleted messages on Server: " + extras.toString());
                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                // This loop represents the service doing some work.
                for (int i = 0; i < 5; i++) {
                    Log.i("GCM", "Working... " + (i + 1)
                            + "/5 @ " + SystemClock.elapsedRealtime());
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                    }
                }
                Log.i("GCM", "Completed work @ " + SystemClock.elapsedRealtime());
                // Post notification of received message.
                sendNotification(extras.toString());
                Log.i("GCM", "Received: " + extras.toString());
            }
        }
        //sendNotification("Nuevo mensaje");

        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void sendNotification(String msg) {
        notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentTitle("Zirkapp Notification")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                .setContentText(msg)
                .setAutoCancel(true);

        nBuilder.setContentIntent(contentIntent);
        notificationManager.notify(NOTIFICATION_ID, nBuilder.build());
    }
}
