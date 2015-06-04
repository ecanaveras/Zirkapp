package com.ecp.gsy.dcs.zirkapp.app.util.broadcast;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.util.Log;

import com.ecp.gsy.dcs.zirkapp.app.activities.MainActivity;
import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.fragments.UsersFragment;
import com.ecp.gsy.dcs.zirkapp.app.util.beans.ItemNotification;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.DataParseHelper;
import com.ecp.gsy.dcs.zirkapp.app.GlobalApplication;
import com.ecp.gsy.dcs.zirkapp.app.util.task.SendPushTask;
import com.parse.ParseObject;
import com.parse.ParsePushBroadcastReceiver;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Elder on 24/04/2015.
 */
public class ParsePushReceiver extends ParsePushBroadcastReceiver {

    private NotificationManager notificationManager;
    private RoundedBitmapDrawable imgLargeIcon;
    private String targetId;
    private String receptorId;
    private String senderId;
    private ItemNotification itemNotifi;
    private ParseUser senderUser;
    private GlobalApplication globalApplication;
    private int typeNotification;
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
        String[] messages = msg.split("-:-");
        msg = msg.contains("-:-") ? msg.substring(0, msg.length() - 4) : msg; //Limpiar mensaje
        typeNotification = 0;
        if (messages.length > 1)
            typeNotification = Integer.parseInt(messages[messages.length - 1]);
        //Notificacion
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(0);//Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        String titleNoti = "%s de %s";
        String bodyNoti = "%s %s";
        String summary = null, senderName = null, typeNotiString = null;
        senderUser = findParseUser(senderId);
        if (senderId != null) {
            switch (typeNotification) {
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
                    itemNotifi = new ItemNotification();
                    itemNotifi.setTypeNoti(SendPushTask.PUSH_COMMENT);
                    summary = "Nuevo comentario";
                    typeNotiString = "[Coment]";
                    //Buscar Nombre de usuario
                    senderName = senderUser.getString("name") != null ? senderUser.getString("name") : senderUser.getUsername();
                    break;

                default:
                    typeNotiString = "[Gral]";
                    itemNotifi = null;
                    break;
            }
            //imgLargeIcon = GlobalApplication.getAvatar(senderUser);
        }

        if (notificar) {

            if (itemNotifi != null) {
                itemNotifi.setSummaryNoti(String.format(titleNoti, summary, senderName));
                itemNotifi.setDetailNoti(msg);

                //Guardar la Noti
                saveNotificacion(itemNotifi);
            }

            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.ic_zirkapp_noti)
                    .setLargeIcon(imgLargeIcon != null ? imgLargeIcon.getBitmap() : BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher))
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setContentTitle(title != null ? title : "Zirkapp")
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(String.format(bodyNoti, typeNotiString, msg)))
                    .setContentText(String.format(bodyNoti, typeNotiString, msg))
                    .setAutoCancel(true)
                    .setWhen(System.currentTimeMillis());

            nBuilder.setContentIntent(contentIntent);
            notificationManager.notify(typeNotification, nBuilder.build());
        }

    }

    /**
     * Guarda la notificacion
     *
     * @param item
     */
    private void saveNotificacion(ItemNotification item) {
        ParseUser receptorUser = findParseUser(receptorId);
        if (item != null && senderUser != null && targetId != null && receptorUser != null) {
            ParseObject noti = new ParseObject("ParseZNotifi");
            noti.put("senderUser", senderUser);
            noti.put("receptorUser", receptorUser);
            switch (item.getTypeNoti()) {
                case SendPushTask.PUSH_CHAT:
                    noti.put("userTarget", ParseObject.createWithoutData("user", targetId));
                    break;
                case SendPushTask.PUSH_COMMENT:
                    noti.put("zimessTarget", ParseObject.createWithoutData("ParseZimess", targetId));
                    break;
            }
            noti.put("detailNoti", item.getDetailNoti());
            noti.put("summaryNoti", item.getSummaryNoti());
            noti.put("typeNoti", item.getTypeNoti());
            noti.put("readNoti", false);
            noti.saveInBackground();
        }
    }

    /**
     * Busca los datos del usuario que envia la notificacion
     *
     * @param objectId
     * @return
     */
    private ParseUser findParseUser(String objectId) {
        return DataParseHelper.findUser(objectId);
    }
}

