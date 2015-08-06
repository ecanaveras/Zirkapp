package com.ecp.gsy.dcs.zirkapp.app.util.broadcast;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.util.Log;

import com.ecp.gsy.dcs.zirkapp.app.GlobalApplication;
import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.activities.MainActivity;
import com.ecp.gsy.dcs.zirkapp.app.fragments.UsersFragment;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.DataParseHelper;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.models.ParseZNotifi;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.models.ParseZimess;
import com.ecp.gsy.dcs.zirkapp.app.util.task.SendPushTask;
import com.parse.ParseObject;
import com.parse.ParsePushBroadcastReceiver;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Elder on 24/04/2015.
 */
public class ParsePushReceiver extends ParsePushBroadcastReceiver {

    private NotificationManager notificationManager;
    private RoundedBitmapDrawable imgLargeIcon;
    private String targetId;
    private String receptorId;
    private String senderId;
    private ParseZNotifi itemNotifi;
    private ParseUser senderUser;
    private GlobalApplication globalApplication;
    private int typeNotify = 0;
    private boolean notificar = true;

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
            typeNotify = data.getInt("type");
            targetId = data.getString("targetId");
            receptorId = data.getString("receptorId");
            senderId = data.getString("senderId");
            //Enviar notificacion
            sendNotification(message, title, context);
        } catch (JSONException e) {
            Log.e("push.json.receiver", e.getMessage());
            try {
                //Enviar sin title
                if (data != null && (e.getMessage().contains("title"))) {
                    sendNotification(data.getString("alert"), null, context);
                }
            } catch (JSONException e1) {
                Log.e("push.json.receiver", e1.getMessage());
            }

        }
        super.onPushReceive(context, intent);
        //Log.d("bundlePush", bundle.toString());
    }

    /**
     * Envia y guarda la notificacion
     *
     * @param msg
     * @param title
     * @param context
     */
    private void sendNotification(String msg, String title, Context context) {
        //String[] messages = msg.split("-:-");
        msg = msg.contains("-:-") ? msg.substring(0, msg.length() - 4) : msg; //Limpiar mensaje
        //Notificacion
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(0);//Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        String titleNoti = "%s de %s";
        String bodyNoti = "%s %s";
        String summary = null, senderName = null, typeNotiString = null;
        senderUser = findParseUser(senderId);
        if (senderId != null) {
            switch (typeNotify) {
                case SendPushTask.PUSH_CHAT:
                    //Receiver
                    Intent broad = new Intent();
                    broad.setAction(CountMessagesReceiver.ACTION_LISTENER);
                    broad.putExtra("senderId", senderId);
                    broad.putExtra("recipientId", receptorId);
                    context.sendBroadcast(broad);

                    intent.setAction("OPEN_FRAGMENT_USER"); //Notificacion desde chat
                    intent.putExtra("targetId", targetId);
                    intent.putExtra("receptorId", receptorId);
                    intent.putExtra("senderId", senderId);
                    //Manejar Noti
                    if (UsersFragment.isRunning()) {
                        globalApplication = (GlobalApplication) context.getApplicationContext();
                        if (senderUser != null && !senderUser.equals(globalApplication.getCustomParseUser()) || globalApplication.isListeningNotifi()) {
                            globalApplication.setCustomParseUser(senderUser);
                        } else {
                            //No notificar
                            notificar = false;
                        }
                    }
                    typeNotiString = "[Chat]";
                    break;

                case SendPushTask.PUSH_COMMENT:
                    intent.setAction("OPEN_FRAGMENT_NOTI"); //Notificacion desde comentarios
                    intent.putExtra("targetId", targetId);
                    intent.putExtra("receptorId", receptorId);
                    intent.putExtra("senderId", senderId);
                    //Manejar Noti
                    itemNotifi = new ParseZNotifi();
                    itemNotifi.setTypeNoti(SendPushTask.PUSH_COMMENT);
                    summary = "Nuevo comentario";
                    typeNotiString = "[Coment]";
                    //Buscar Nombre de usuario
                    senderName = senderUser.getString("name") != null ? senderUser.getString("name") : senderUser.getUsername();
                    break;

                case SendPushTask.PUSH_QUOTE:
                    intent.setAction("OPEN_FRAGMENT_NOTI"); //Notificacion desde comentarios
                    intent.putExtra("targetId", targetId);
                    intent.putExtra("receptorId", receptorId);
                    intent.putExtra("senderId", senderId);
                    //Manejar Noti
                    itemNotifi = new ParseZNotifi();
                    itemNotifi.setTypeNoti(SendPushTask.PUSH_QUOTE);
                    summary = "Nueva respuesta";
                    typeNotiString = "[Resp]";
                    //Buscar Nombre de usuario
                    senderName = senderUser.getString("name") != null ? senderUser.getString("name") : senderUser.getUsername();
                    break;

                default:
                    typeNotiString = "[Gral]";
                    itemNotifi = null;
                    break;
            }
            //imgLargeIcon = GlobalApplication.getAvatar(senderUser);
        } else {
            typeNotiString = "[Gral]";
            itemNotifi = null;
        }

        if (notificar) {

            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.ic_zirkapp_noti)
                    .setLargeIcon(imgLargeIcon != null ? imgLargeIcon.getBitmap() : BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher))
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setContentTitle(title != null ? title : "Zirkapp")
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(String.format(bodyNoti, typeNotiString, msg)))
                    .setContentText(String.format(bodyNoti, typeNotiString, msg))
                    .setAutoCancel(true)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setWhen(System.currentTimeMillis());

            nBuilder.setContentIntent(contentIntent);
            notificationManager.notify(typeNotify, nBuilder.build());

            if (itemNotifi != null) {
                itemNotifi.setSummaryNoti(String.format(titleNoti, summary, senderName));
                itemNotifi.setDetailNoti(msg);

                //Guardar la Noti
                //saveNotificacion(itemNotifi, findParseUser(receptorId));

            }
        }
    }

    /**
     * Guarda la notificacion
     *
     * @param noti
     */
    private void saveNotificacion(ParseZNotifi noti, ParseUser receptorUser) {
        if (noti != null && senderUser != null && targetId != null && receptorUser != null) {
            noti.put(ParseZNotifi.SENDER_USER, senderUser);
            noti.put(ParseZNotifi.RECEPTOR_USER, receptorUser);
            switch (noti.getTypeNoti()) {
                case SendPushTask.PUSH_CHAT:
                    noti.put("userTarget", ParseObject.createWithoutData("user", targetId));
                    break;
                case SendPushTask.PUSH_COMMENT:
                    noti.put(ParseZNotifi.ZIMESS_TARGET, ParseObject.createWithoutData(ParseZimess.class, targetId));
                    break;
                case SendPushTask.PUSH_QUOTE:
                    noti.put(ParseZNotifi.ZIMESS_TARGET, ParseObject.createWithoutData(ParseZimess.class, targetId));
                    break;
            }
            noti.put(ParseZNotifi.READ_NOTI, false);
            noti.saveInBackground();
        }
    }

    /**
     * Busca usuario del objectId
     *
     * @param objectId
     * @return
     */
    private ParseUser findParseUser(String objectId) {
        return DataParseHelper.findUser(objectId);
    }

    /**
     * Busca usuario del parseUser
     *
     * @param username
     * @return
     */
    private ParseUser findParseUsername(String username) {
        return DataParseHelper.findUserName(username);
    }
}

