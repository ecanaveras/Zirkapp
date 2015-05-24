package com.ecp.gsy.dcs.zirkapp.app.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.adapters.MessageAdapter;
import com.ecp.gsy.dcs.zirkapp.app.util.services.MessageService;
import com.ecp.gsy.dcs.zirkapp.app.GlobalApplication;
import com.ecp.gsy.dcs.zirkapp.app.util.task.SendPushTask;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.messaging.Message;
import com.sinch.android.rtc.messaging.MessageClient;
import com.sinch.android.rtc.messaging.MessageClientListener;
import com.sinch.android.rtc.messaging.MessageDeliveryInfo;
import com.sinch.android.rtc.messaging.MessageFailureInfo;
import com.sinch.android.rtc.messaging.WritableMessage;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Elder on 21/02/2015.
 */
public class MessagingActivity extends ActionBarActivity {

    private String receptorId, receptorUsername, receptorName;
    private EditText txtMessageBodyField;
    private MessageService.MessageServiceInterface messageService;
    private ParseUser currentUser, receptorUser;
    private ServiceConnection serviceConnection = new MyServiceConnection();
    private MyMessageClientListener messageClientListener = new MyMessageClientListener();
    private MessageAdapter adapterMessage;
    private ListView listMessage;
    private GlobalApplication globalApplication;
    private Activity activity;
    private ProgressBar progressBar;
    private WritableMessage writableMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        activity = this;

        bindService(new Intent(this, MessageService.class), serviceConnection, BIND_AUTO_CREATE);

        globalApplication = (GlobalApplication) getApplicationContext();
        //Usuario actual
        currentUser = globalApplication.getCurrentUser();

        //Usuario receptor
        receptorUser = globalApplication.getCustomParseUser();
        if (receptorUser != null) {
            receptorId = receptorUser.getObjectId();
            receptorUsername = receptorUser.getUsername();
            receptorName = receptorUser.getString("name");
        }

        initComponentUI();

        findLocalMessageHistory();
    }

    private void initComponentUI() {
        //Personalizar ActionBar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        customActionBar(toolbar);

        //ProgressBar
        progressBar = (ProgressBar) findViewById(R.id.progressLoad);

        //Lista
        listMessage = (ListView) findViewById(R.id.listMessages);
        adapterMessage = new MessageAdapter(this);
        listMessage.setAdapter(adapterMessage);

        txtMessageBodyField = (EditText) findViewById(R.id.txtMessageBodyField);

        findViewById(R.id.btnSendMessage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

    }

    private void customActionBar(Toolbar actionBar) {
        if (actionBar != null) {
            setSupportActionBar(actionBar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayShowCustomEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));


            View customView = getLayoutInflater().inflate(R.layout.actionbar_user_title, null);
            ImageView imageView = (ImageView) customView.findViewById(R.id.imgAvatar);
            imageView.setImageDrawable(GlobalApplication.getAvatar(receptorUser));
           /* imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    view.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.anim_image_click));
                    onBackPressed();
                }
            });*/
            LinearLayout layoutActionBarTitle = (LinearLayout) customView.findViewById(R.id.layoutActionbarTitle);
            TextView titleBar = (TextView) customView.findViewById(R.id.actionbarTitle);
            TextView subTitleBar = (TextView) customView.findViewById(R.id.actionbarSubTitle);
            titleBar.setText(receptorName != null ? receptorName : receptorUsername);
            if (receptorName != null) {
                subTitleBar.setText(receptorUsername);
            } else {
                subTitleBar.setVisibility(View.GONE);
            }
            layoutActionBarTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    view.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.anim_image_click));
                    Intent intent = new Intent(activity, UserProfileActivity.class);
                    //intent.putExtra("activityfrom", MessagingActivity.class.getSimpleName());
                    globalApplication.setCustomParseUser(receptorUser);
                    activity.startActivity(intent);
                }
            });
            getSupportActionBar().setCustomView(customView);
        }
    }

    private void sendMessage() {
        String messageBody = txtMessageBodyField.getText().toString();
        if (messageBody.isEmpty()) {
            Toast.makeText(getApplicationContext(), getString(R.string.msgMessageEmpty), Toast.LENGTH_LONG).show();
            return;
        }

        messageService.sendMessage(receptorId, messageBody);
        txtMessageBodyField.setText("");
    }

    /**
     * Guarda el historial del chat de forma local
     *
     * @param message
     * @param writableMessage
     * @param senderId
     * @param messageDirection
     */
    private void saveLocalMessage(Message message, final WritableMessage writableMessage, final String senderId, final Integer messageDirection) {
        //Agregar el mensaje en el local si no existe.
        ParseQuery<ParseObject> query = ParseQuery.getQuery("ParseMessage");
        query.whereEqualTo("sinchId", message.getMessageId());
        query.fromLocalDatastore();
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {
                    if (parseObjects.size() == 0) {
                        ParseObject parseMessage = new ParseObject("ParseMessage");
                        parseMessage.put("senderId", senderId);
                        parseMessage.put("recipientId", writableMessage.getRecipientIds().get(0));
                        parseMessage.put("messageText", writableMessage.getTextBody());
                        parseMessage.put("sinchId", writableMessage.getMessageId());
                        if (messageDirection == MessageAdapter.DIRECTION_INCOMING)
                            parseMessage.put("messageRead", false);
                        parseMessage.pinInBackground();

                        adapterMessage.addMessage(writableMessage, messageDirection, receptorId);
                    }
                }
            }
        });
    }

    /**
     * Busca los mensajes previos en Local
     */
    private void findLocalMessageHistory() {
        progressBar.setVisibility(View.VISIBLE);
        String[] userIds = {currentUser.getObjectId(), receptorId};
        ParseQuery<ParseObject> query = ParseQuery.getQuery("ParseMessage");
        query.whereContainedIn("senderId", Arrays.asList(userIds));
        query.whereContainedIn("recipientId", Arrays.asList(userIds));
        query.orderByAscending("createdAt");
        query.fromLocalDatastore();
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {
                    for (ParseObject parseObj : parseObjects) {
                        WritableMessage message = new WritableMessage(parseObj.get("recipientId").toString(), parseObj.get("messageText").toString());
                        if (parseObj.get("senderId").toString().equals(currentUser.getObjectId())) {
                            adapterMessage.addMessage(message, MessageAdapter.DIRECTION_OUTGOING, currentUser.getUsername());
                        } else {
                            adapterMessage.addMessage(message, MessageAdapter.DIRECTION_INCOMING, receptorUsername);
                            parseObj.put("messageRead", true);
                            parseObj.pinInBackground();
                        }
                    }
                } else {
                    Log.e("Parse.chat.history", e.getMessage());
                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onPause() {
        globalApplication.setListeningNotifi(true);
        super.onPause();
    }

    @Override
    protected void onResume() {
        globalApplication.setListeningNotifi(false);
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        globalApplication.setListeningNotifi(true);
        globalApplication.setCustomParseUser(null);
        messageService.removeMessageClientListener(messageClientListener);
        unbindService(serviceConnection);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        globalApplication.setCustomParseUser(null);
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class MyServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            messageService = (MessageService.MessageServiceInterface) iBinder;
            messageService.addMessageClientListener(messageClientListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            messageService = null;
        }
    }

    private class MyMessageClientListener implements MessageClientListener {
        @Override
        public void onIncomingMessage(MessageClient messageClient, Message message) {
            if (message.getSenderId().equals(receptorId)) {
                final WritableMessage writableMessage = new WritableMessage(message.getRecipientIds().get(0), message.getTextBody());

                //Guardar historial local.
                saveLocalMessage(message, writableMessage, receptorId, MessageAdapter.DIRECTION_INCOMING);
                Log.i("incoming.message", message.getTextBody());
            }
        }

        @Override
        public void onMessageSent(MessageClient messageClient, Message message, String s) {
            writableMessage = new WritableMessage(message.getRecipientIds().get(0), message.getTextBody());

            //Guardar historial local.
            saveLocalMessage(message, writableMessage, currentUser.getObjectId(), MessageAdapter.DIRECTION_OUTGOING);

            //Enviar notificacion.
            if (receptorId != null && message != null && !globalApplication.isListeningNotifi()) {
                String name = currentUser.getString("name") != null ? currentUser.getString("name") : currentUser.getUsername();
                new SendPushTask(currentUser.getObjectId(), receptorId, currentUser.getObjectId(), name, message.getTextBody(), SendPushTask.PUSH_CHAT).execute();
            }
        }

        @Override
        public void onMessageFailed(MessageClient messageClient, Message message, MessageFailureInfo messageFailureInfo) {
            Toast.makeText(MessagingActivity.this, "Tu mensaje no pudo ser enviado.", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onMessageDelivered(MessageClient messageClient, MessageDeliveryInfo messageDeliveryInfo) {
            Toast.makeText(MessagingActivity.this, "Tu mensaje no fue entregado.", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onShouldSendPushData(MessageClient messageClient, final Message message, final List<PushPair> pushPairs) {

            //Enviar notificacion.
            if (receptorId != null && pushPairs.size() > 0 && message != null) {
                String name = currentUser.getString("name") != null ? currentUser.getString("name") : currentUser.getUsername();
                new SendPushTask(currentUser.getObjectId(), receptorId, currentUser.getObjectId(), name, message.getTextBody(), pushPairs, SendPushTask.PUSH_CHAT).execute();
            }

        }
    }
}
