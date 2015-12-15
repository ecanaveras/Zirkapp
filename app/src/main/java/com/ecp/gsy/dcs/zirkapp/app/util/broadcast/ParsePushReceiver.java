package com.ecp.gsy.dcs.zirkapp.app.util.broadcast;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.util.Log;

import com.ecp.gsy.dcs.zirkapp.app.GlobalApplication;
import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.activities.MainActivity;
import com.ecp.gsy.dcs.zirkapp.app.activities.MessagingActivity;
import com.ecp.gsy.dcs.zirkapp.app.fragments.ChatHistoryFragment;
import com.ecp.gsy.dcs.zirkapp.app.fragments.UsersFragment;
import com.ecp.gsy.dcs.zirkapp.app.fragments.ZimessFragment;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.DataParseHelper;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.models.ParseZNotifi;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.models.ParseZimess;
import com.ecp.gsy.dcs.zirkapp.app.util.task.SendPushTask;
import com.parse.ParseException;
import com.parse.ParseFile;
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
    private int typeNotify = 0;
    private int idNotify = 100;
    private boolean notificar;
    private String message;
    private String title;

    @Override
    protected Notification getNotification(Context context, Intent intent) {
        return null;
    }

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        JSONObject data = null;
        notificar = true;
        try {
            data = new JSONObject(bundle.getString("com.parse.Data"));
            message = data.getString("alert");
            title = data.getString("title");
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
        //Manejar destino de la notificacion
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(0);//Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("typeNotify", typeNotify);
        //Broad Actualizar iconos de mensajes
        Intent broad = new Intent(CounterNotifiReceiver.ACTION_LISTENER);

        String bodyNoti = "%s %s";
        String typeNotiString = null;
        //senderUser = findParseUser(senderId);
        if (senderId != null) {
            switch (typeNotify) {
                case SendPushTask.PUSH_CHAT:
                    intent.setAction("OPEN_MESSAGING_USER"); //Notificacion desde chat
                    intent.putExtra("senderId", senderId); //IdUsuario que envia el chat
                    intent.putExtra("tab", "mensajes");
                    //Manejar Noti
                    GlobalApplication app = (GlobalApplication) context.getApplicationContext();
                    if (MessagingActivity.isRunning()) {
                        MessagingActivity m = MessagingActivity.getInstance();
                        if (m.getReceptorUser() != null && m.getReceptorUser().getObjectId().equals(senderId)) {
                            if (!app.isListeningNotifi()) {
                                notificar = false;
                            } else {
                                app.setMessagingParseUser(m.getReceptorUser());
                                intent = new Intent(context, MessagingActivity.class);
                            }
                        }
                    }
                    broad.putExtra("isMessage", true);

                    typeNotiString = "[Chat]";
                    idNotify = 200;
                    break;

                case SendPushTask.PUSH_COMMENT:
                    intent.setAction("OPEN_FRAGMENT_NOTI"); //Notificacion desde comentarios
                    intent.putExtra("targetId", targetId); //ZImess comentado
                    intent.putExtra("receptorId", receptorId); //Usuario creador de ZImess
                    intent.putExtra("senderId", senderId); //Usuario que envia el comentario

                    typeNotiString = "[Comment]";
                    break;

                case SendPushTask.PUSH_QUOTE:
                    intent.setAction("OPEN_FRAGMENT_NOTI"); //Notificacion desde comentarios
                    intent.putExtra("targetId", targetId); //Usuario o ZImess
                    intent.putExtra("receptorId", receptorId); //Usuario que recibe la respuesta
                    intent.putExtra("senderId", senderId);//USuario que envia la respuesta

                    typeNotiString = "[Resp]";
                    break;

                case SendPushTask.PUSH_ZISS:
                    intent.setAction("OPEN_PROFILE_USER");//Notificacion para ver perfil
                    intent.putExtra("targetId", targetId);//Usuario que envia el Ziss

                    typeNotiString = "[Ziss]";
                    break;

                case SendPushTask.PUSH_FAVORITE:
                    intent.setAction("OPEN_FRAGMENT_NOTI"); //Notificacion desde comentarios
                    intent.putExtra("targetId", targetId);
                    intent.putExtra("receptorId", receptorId);
                    intent.putExtra("senderId", senderId);

                    typeNotiString = "[Fav]";
                    break;

                default:
                    typeNotiString = "[Gral]";
                    break;
            }
            context.sendBroadcast(broad);
        } else {
            typeNotiString = "";
        }

        if (notificar) {
            //Bitmap bitmap = null;
            //Todo si es posible, tomar imagen del sender

            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.ic_zirkapp_noti)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher))
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setContentTitle(title != null ? title : context.getString(R.string.app_name))
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(String.format(bodyNoti, typeNotiString, msg)))
                    .setContentText(String.format(bodyNoti, typeNotiString, msg))
                    .setAutoCancel(true)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setWhen(System.currentTimeMillis());

            nBuilder.setContentIntent(contentIntent);
            if (typeNotify == SendPushTask.PUSH_CHAT) {
                notificationManager.notify(senderId, idNotify, nBuilder.build());
            } else {
                notificationManager.notify(idNotify, nBuilder.build());
            }

        }
    }
}

