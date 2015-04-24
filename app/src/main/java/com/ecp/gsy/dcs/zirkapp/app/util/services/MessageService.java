package com.ecp.gsy.dcs.zirkapp.app.util.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.parse.ParseUser;
import com.sinch.android.rtc.ClientRegistration;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchClientListener;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.messaging.MessageClient;
import com.sinch.android.rtc.messaging.MessageClientListener;
import com.sinch.android.rtc.messaging.WritableMessage;

public class MessageService extends Service implements SinchClientListener {

    private static final String APP_KEY = "308f3e50-dfb0-4855-9935-08b75028c06e";
    private static final String APP_SECRET = "UQmDdJm8nkSBUFntG4GPYQ==";
    private static final String ENVIRONMENT = "sandbox.sinch.com";
    private final MessageServiceInterface serviceInterface = new MessageServiceInterface();
    private SinchClient sinchClient = null;
    private MessageClient messageClient = null;
    private ParseUser currentUser = null;
    private Intent broadcastIntent = new Intent("app.fragments.UsersOnlineFragment");

    private String regId;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null)
            regId = intent.getStringExtra("regId");

        //Tomar el UserId de Parse
        currentUser = ParseUser.getCurrentUser();
        if (currentUser != null && !isSinchClientStarted()) {
            //TODO MENSAJERIA DISABLED
            startSinchClient(currentUser.getObjectId());
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void startSinchClient(String username) {
        sinchClient = Sinch.getSinchClientBuilder()
                .context(this)
                .userId(username)
                .applicationKey(APP_KEY)
                .applicationSecret(APP_SECRET)
                .environmentHost(ENVIRONMENT)
                .build();

        sinchClient.addSinchClientListener(this);

        //Mensajes encendidos
        sinchClient.setSupportMessaging(true);
        sinchClient.setSupportActiveConnectionInBackground(true);
        sinchClient.setSupportPushNotifications(true);

        sinchClient.checkManifest();
        sinchClient.start();
        if (regId != null)
            sinchClient.registerPushNotificationData(regId.getBytes());
    }

    private boolean isSinchClientStarted() {
        return sinchClient != null && sinchClient.isStarted();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return serviceInterface;
    }

    @Override
    public void onClientStarted(SinchClient sinchClient) {
        broadcastIntent.putExtra("success", true);
        sendBroadcast(broadcastIntent);

        sinchClient.startListeningOnActiveConnection();
        messageClient = sinchClient.getMessageClient();
    }

    @Override
    public void onClientStopped(SinchClient sinchClient) {
        sinchClient = null;
    }

    @Override
    public void onClientFailed(SinchClient sinchClient, SinchError sinchError) {
        broadcastIntent.putExtra("success", false);
        sendBroadcast(broadcastIntent);
        sinchClient = null;
    }

    @Override
    public void onRegistrationCredentialsRequired(SinchClient sinchClient, ClientRegistration clientRegistration) {
    }

    @Override
    public void onLogMessage(int i, String s, String s2) {
    }

    private void sendMessage(String recipientUserId, String textBody) {
        if (messageClient != null) {
            WritableMessage message = new WritableMessage(recipientUserId, textBody);
            messageClient.send(message);
        }
    }

    private void addMessageClientListener(MessageClientListener listener) {
        if (messageClient != null) {
            messageClient.addMessageClientListener(listener);
        }
    }

    private void removeMessageClientListener(MessageClientListener listener) {
        if (messageClient != null) {
            messageClient.removeMessageClientListener(listener);
        }
    }

    @Override
    public void onDestroy() {
        if (ParseUser.getCurrentUser() == null) {
            sinchClient.unregisterPushNotificationData();
        }
        if (sinchClient != null) {
            sinchClient.stopListeningOnActiveConnection();
            sinchClient.terminate();
        }
    }

    public class MessageServiceInterface extends Binder {

        public void sendMessage(String recipientUserId, String textBody) {
            MessageService.this.sendMessage(recipientUserId, textBody);
        }

        public void addMessageClientListener(MessageClientListener listener) {
            MessageService.this.addMessageClientListener(listener);
        }

        public void removeMessageClientListener(MessageClientListener listener) {
            MessageService.this.removeMessageClientListener(listener);
        }

        public boolean isSinchClientStarted() {
            return MessageService.this.isSinchClientStarted();
        }
    }


}
