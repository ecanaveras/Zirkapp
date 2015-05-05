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
import com.ecp.gsy.dcs.zirkapp.app.util.beans.ItemNotification;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.DataParseHelper;
import com.ecp.gsy.dcs.zirkapp.app.util.task.SendPushTask;
import com.parse.ParseObject;
import com.parse.ParsePushBroadcastReceiver;
import com.parse.ParseUser;
import com.parse.codec.binary.Base64;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Elder on 24/04/2015.
 */
public class ParsePushReceiver extends ParsePushBroadcastReceiver {

    private NotificationManager notificationManager;
    public static final int NOTIFICATION_ID = 1;
    private Bitmap imgLargeIcon;
    private String targetId;
    private String receptorId;
    private String senderId;
    private ItemNotification itemNotifi;

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
        int typeNotification = 0;
        if (messages.length > 1)
            typeNotification = Integer.parseInt(messages[messages.length - 1]);
        //Notificacion
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        String titleNoti = "%s de %s";
        String bodyNoti = "%s %s";
        String summary = null, senderName = null, typeNotiString = null;
        switch (typeNotification) {
            case SendPushTask.PUSH_CHAT:
                intent.setAction("OPEN_FRAGMENT_USER"); //Notificacion desde chat
                intent.putExtra("targetId", targetId);
                intent.putExtra("receptorId", receptorId);
                intent.putExtra("senderId", senderId);
                //Manejar Noti
                itemNotifi = new ItemNotification();
                itemNotifi.setTypeNoti(SendPushTask.PUSH_CHAT);
                summary = "Nuevo mensaje privado";
                typeNotiString = "[Chat]";
                //Buscar Nombre de usuario
                senderName = findNameUser(senderId);
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
                typeNotiString = "[Comentario]";
                //Buscar Nombre de usuario
                senderName = findNameUser(senderId);
                break;

            default:
                itemNotifi = null;
                break;
        }

        if (itemNotifi != null) {
            itemNotifi.setTargetId(targetId);
            itemNotifi.setSenderId(senderId);
            itemNotifi.setReceptorId(receptorId);
            itemNotifi.setSummaryNoti(String.format(titleNoti, summary, senderName));
            itemNotifi.setDetailNoti(msg);
            //Guardar la Noti
            saveNotificacion(itemNotifi);
        }

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_zirkapp_noti)
                .setLargeIcon(imgLargeIcon != null ? imgLargeIcon : BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher))
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentTitle(title != null ? title : "Zirkapp")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(String.format(bodyNoti, typeNotiString, msg)))
                .setContentText(String.format(bodyNoti, typeNotiString, msg))
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis());

        nBuilder.setContentIntent(contentIntent);
        notificationManager.notify(NOTIFICATION_ID, nBuilder.build());
    }

    /**
     * Guarda la notificacion
     *
     * @param item
     */
    private void saveNotificacion(ItemNotification item) {
        ParseObject noti = new ParseObject("ParseZNotifi");
        noti.put("senderId", item.getSenderId());
        noti.put("receptorId", item.getReceptorId());
        noti.put("targetId", item.getTargetId());
        noti.put("detailNoti", item.getDetailNoti());
        noti.put("summaryNoti", item.getSummaryNoti());
        noti.put("typeNoti", item.getTypeNoti());
        noti.put("readNoti", false);
        noti.saveInBackground();
    }

    /**
     * Busca los datos del usuario que envia la notificacion
     *
     * @param objectId
     * @return
     */
    private String findNameUser(String objectId) {
        ParseUser senderUser = DataParseHelper.findUser(objectId);
        return senderUser.getString("name") != null ? senderUser.getString("name") : senderUser.getUsername();
    }
}

