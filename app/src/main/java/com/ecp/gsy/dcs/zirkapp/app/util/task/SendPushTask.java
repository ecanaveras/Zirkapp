package com.ecp.gsy.dcs.zirkapp.app.util.task;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;

import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.DataParseHelper;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.models.ParseZNotifi;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.models.ParseZimess;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SendCallback;
import com.sinch.android.rtc.PushPair;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Elder on 28/04/2015.
 */
public class SendPushTask extends AsyncTask<Void, Void, Void> {

    public static final int PUSH_CHAT = 1;
    public static final int PUSH_COMMENT = 2;
    public static final int PUSH_ZIMESS = 3;
    public static final int PUSH_QUOTE = 4;
    public static final int PUSH_ZISS = 5;
    public static final int PUSH_FAVORITE = 6;

    private List<PushPair> pushPairs;
    private String targetId;
    private String receptorId;
    private String senderId;
    private String message;
    private String title;
    private String pushPayLoad;
    private int typeNotify;
    private ParseUser receptorUser;


    /**
     * Envia una notificacion
     *
     * @param title
     * @param message
     * @param receptorUser
     * @param typeNotify
     */
    public SendPushTask(String targetId, ParseUser receptorUser, String senderId, String title, String message, int typeNotify) {
        this.targetId = targetId;
        this.receptorUser = receptorUser;
        this.senderId = senderId;
        this.title = title;
        this.message = message;
        this.typeNotify = typeNotify;
    }

    /**
     * Envia una notificacion especial para Ziss
     *
     * @param receptorUser
     * @param senderId
     * @param title
     * @param message
     * @param typeNotify
     */
    public SendPushTask(ParseUser receptorUser, String senderId, String title, String message, int typeNotify) {
        this.receptorUser = receptorUser;
        this.senderId = senderId;
        this.title = title;
        this.message = message;
        this.typeNotify = typeNotify;
    }

    /**
     * Envia una notificacion especial para CHat
     *
     * @param title
     * @param message
     * @param receptorUser
     * @param senderId
     * @param pushPairs
     * @param typeNotify
     */
    public SendPushTask(ParseUser receptorUser, String senderId, String title, String message, List<PushPair> pushPairs, int typeNotify) {
        this.targetId = senderId;
        this.receptorUser = receptorUser;
        this.senderId = senderId;
        this.title = title;
        this.message = message;
        this.pushPairs = pushPairs;
        this.typeNotify = typeNotify;
    }

    @Override
    protected Void doInBackground(Void... params) {
        Log.d("send.push.task", "started...");
        if ((receptorId != null || receptorUser != null) && message != null) {
            if (pushPairs != null && pushPairs.size() > 0) {
                PushPair pushPair = pushPairs.get(0);
                pushPayLoad = pushPair.getPushPayload();
            }


            ParseQuery query = ParseInstallation.getQuery();
            //Si viene el parseUser
            if (receptorUser != null) {
                receptorId = receptorUser.getObjectId();
                //1. Buscar las instalaciones del usuario a notificar
                query.whereEqualTo("user", receptorUser);
                Log.d("PushFindUserFast", "true");
            } else if (receptorId != null) { //Si viene el userId y no el parseUser
                //1. Buscar el usuario a notificar
                ParseQuery userQuery = ParseUser.getQuery();
                userQuery.whereEqualTo("objectId", receptorId);
                //2. Buscar las instalaciones del usuario a notificar
                query.whereMatchesQuery("user", userQuery);
            }

            //3. Establecer query de filtro
            ParsePush parsePush = new ParsePush();
            parsePush.setQuery(query);

            //Alert
            String messageBody = "%s -:-%d";

            try {
                JSONObject data = new JSONObject();
                data.put("alert", String.format(messageBody, message.length() < 100 ? message : message.substring(0, 100).concat("..."), typeNotify));
                data.put("sound", "default"); //Todo obtener Tono de preferencias
                data.put("type", typeNotify);
                //Pasar sender como titulo
                data.put("title", title);
                //Datos para el manejo de la notificacion
                if (targetId != null) {
                    data.put("targetId", targetId); //Objeto afectado
                }
                if (receptorId != null) data.put("receptorId", receptorId); //Quien recibe
                if (senderId != null)
                    data.put("senderId", senderId); //Quien produce la notificacion
                if (pushPayLoad != null) data.put("SIN", pushPayLoad);

                parsePush.setData(data);
                parsePush.sendInBackground(new SendCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            Log.i("parse.push.task", "success");
                        } else {
                            Log.i("parse.push.task", "failed");
                        }
                    }
                });

                //Guardar la notificacion
                if (typeNotify != SendPushTask.PUSH_CHAT && typeNotify != SendPushTask.PUSH_ZIMESS) {
                    ParseUser senderUser = findParseUser(senderId);
                    String senderName = senderUser.getString("name") != null ? senderUser.getString("name") : senderUser.getUsername();
                    ParseZNotifi notifi = new ParseZNotifi();
                    notifi.setTypeNoti(typeNotify);
                    notifi.setDetailNoti(message);

                    String formatTitleNoti = "%s";
                    switch (typeNotify) {
                        case SendPushTask.PUSH_COMMENT:
                            formatTitleNoti = "%s ha comentado tu Zimess";
                            break;
                        case SendPushTask.PUSH_QUOTE:
                            formatTitleNoti = "%s te ha mencionado en un comentario";
                            break;
                        case SendPushTask.PUSH_ZISS:
                            formatTitleNoti = "%s te ha dado un Ziss";
                            break;
                        case SendPushTask.PUSH_FAVORITE:
                            formatTitleNoti = "A %s le gusta tu Zimess";
                            break;
                    }
                    notifi.setSummaryNoti(String.format(formatTitleNoti, senderName));
                    //Guardar Notificacion
                    saveNotificacion(notifi, senderUser, receptorUser != null ? receptorUser : findParseUser(receptorId));
                }
            } catch (JSONException e) {
                Log.e("json.exception", e.getMessage());
            }
        }

        return null;
    }

    /**
     * Guarda la notificacion
     *
     * @param noti
     */
    private void saveNotificacion(ParseZNotifi noti, ParseUser senderUser, ParseUser receptorUser) {
        if (noti != null && targetId != null && receptorUser != null) {
            noti.put(ParseZNotifi.SENDER_USER, senderUser);
            noti.put(ParseZNotifi.RECEPTOR_USER, receptorUser);
            switch (noti.getTypeNoti()) {
                case SendPushTask.PUSH_CHAT:
                    noti.put("userTarget", ParseObject.createWithoutData("user", targetId));
                    break;
                default:
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
}
