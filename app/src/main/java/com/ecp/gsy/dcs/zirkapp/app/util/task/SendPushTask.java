package com.ecp.gsy.dcs.zirkapp.app.util.task;

import android.os.AsyncTask;
import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseInstallation;
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
     * @param receptorId
     * @param typeNotify
     */
    public SendPushTask(String targetId, String receptorId, String senderId, String title, String message, int typeNotify) {
        this.targetId = targetId;
        this.receptorId = receptorId;
        this.senderId = senderId;
        this.title = title;
        this.message = message;
        this.typeNotify = typeNotify;
    }

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
     * Envia una notificacion
     *
     * @param title
     * @param message
     * @param receptorId
     * @param senderId
     * @param pushPairs
     * @param typeNotify
     */
    public SendPushTask(String targetId, String receptorId, String senderId, String title, String message, List<PushPair> pushPairs, int typeNotify) {
        this.targetId = targetId;
        this.receptorId = receptorId;
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
            if (receptorId != null) {
                //1. Tomar el usuario a notificar
                ParseQuery userQuery = ParseUser.getQuery();
                userQuery.whereEqualTo("objectId", receptorId);
                //2a. Tomar las instalaciones del usuario a notificar
                query.whereMatchesQuery("user", userQuery);
            }

            //Como se envia el usuario, no es necesario buscarlo en parse
            if (receptorUser != null) {
                receptorId = receptorUser.getObjectId();
                //2b. Tomar las instalaciones del usuario a notificar
                query.whereEqualTo("user", receptorUser);
            }

            //3. Establecer query de filtro
            ParsePush parsePush = new ParsePush();
            parsePush.setQuery(query);

            //Alert
            String messageBody = "%s -:-%d";

            try {
                JSONObject data = new JSONObject();
                data.put("alert", String.format(messageBody, message.length() < 81 ? message : message.substring(0, 80).concat("..."), typeNotify));
                data.put("badge", "Increment");
                data.put("sound", "default"); //Todo obtener Tono de preferencias
                data.put("type", typeNotify);
                //Pasar sender como titulo
                data.put("title", title);
                //Datos para el manejo de la notificacion
                if (targetId != null) data.put("targetId", targetId); //Objeto afectado
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
            } catch (JSONException e) {
                Log.e("json.exception", e.getMessage());
            }
        }

        return null;
    }
}
