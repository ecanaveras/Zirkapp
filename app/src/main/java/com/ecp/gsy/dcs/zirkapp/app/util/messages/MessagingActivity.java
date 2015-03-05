package com.ecp.gsy.dcs.zirkapp.app.util.messages;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.adapters.MessageAdapter;
import com.ecp.gsy.dcs.zirkapp.app.util.services.MessageService;
import com.ecp.gsy.dcs.zirkapp.app.util.task.GlobalApplication;
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
public class MessagingActivity extends Activity {

    private String receptorId;
    private String receptorUsername;
    private EditText txtMessageBodyField;
    private MessageService.MessageServiceInterface messageService;
    private ParseUser currentUserId;
    private ServiceConnection serviceConnection = new MyServiceConnection();
    private MyMessageClientListener messageClientListener = new MyMessageClientListener();
    private MessageAdapter adapterMessage;
    private ListView listMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        bindService(new Intent(this, MessageService.class), serviceConnection, BIND_AUTO_CREATE);

        //Tomar receptor
        Intent intent = getIntent();
        receptorId = intent.getStringExtra("RECIPIENT_ID");
        receptorUsername = intent.getStringExtra("RECIPIENT_USERNAME");

        //Lista
        listMessage = (ListView) findViewById(R.id.listMessages);
        adapterMessage = new MessageAdapter(this);
        listMessage.setAdapter(adapterMessage);

        //Usuario actual
        final GlobalApplication globalApplication = (GlobalApplication) getApplicationContext();
        currentUserId = globalApplication.getCurrentUser();

        populateMessageHistory();

        txtMessageBodyField = (EditText) findViewById(R.id.txtMessageBodyField);

        findViewById(R.id.btnSendMessage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
    }

    private void sendMessage() {
        String messageBody = txtMessageBodyField.getText().toString();
        if (messageBody.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please enter a message", Toast.LENGTH_LONG).show();
            return;
        }

        messageService.sendMessage(receptorId, messageBody);
        txtMessageBodyField.setText("");
    }

    /**
     * Busca los mensajes previos en Parse
     */
    private void populateMessageHistory() {
        String[] userIds = {currentUserId.getObjectId(), receptorId};
        ParseQuery<ParseObject> query = ParseQuery.getQuery("ParseMessage");
        query.whereContainedIn("senderId", Arrays.asList(userIds));
        query.whereContainedIn("recipiendId", Arrays.asList(userIds));
        query.orderByAscending("createdAt");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {
                    for (ParseObject parseObj : parseObjects) {
                        WritableMessage message = new WritableMessage(parseObj.get("recipientId").toString(), parseObj.get("messageText").toString());
                        if (parseObj.get("senderId").toString().equals(currentUserId.getObjectId())) {
                            adapterMessage.addMessage(message, MessageAdapter.DIRECTION_OUTGOING, currentUserId.getUsername());
                        } else {
                            adapterMessage.addMessage(message, MessageAdapter.DIRECTION_INCOMING, receptorUsername);
                        }
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        unbindService(serviceConnection);
        messageService.removeMessageClientListener(messageClientListener);
        super.onDestroy();
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
                WritableMessage writableMessage = new WritableMessage(message.getRecipientIds().get(0), message.getTextBody());
                adapterMessage.addMessage(writableMessage, MessageAdapter.DIRECTION_INCOMING, receptorId);
            }
        }

        @Override
        public void onMessageSent(MessageClient messageClient, Message message, String s) {
            final WritableMessage writableMessage = new WritableMessage(message.getRecipientIds().get(0), message.getTextBody());

            //Agregar el mensaje en parse.com si no existe.
            ParseQuery<ParseObject> query = ParseQuery.getQuery("ParseMessage");
            query.whereEqualTo("sinchId", message.getMessageId());
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> parseObjects, ParseException e) {
                    if (e == null) {
                        if (parseObjects.size() == 0) {
                            ParseObject parseMessage = new ParseObject("ParseMessage");
                            parseMessage.put("senderId", currentUserId.getObjectId());
                            parseMessage.put("recipientId", writableMessage.getRecipientIds().get(0));
                            parseMessage.put("messageText", writableMessage.getTextBody());
                            parseMessage.put("sinchId", writableMessage.getMessageId());
                            parseMessage.saveInBackground();

                            adapterMessage.addMessage(writableMessage, MessageAdapter.DIRECTION_OUTGOING, currentUserId.getUsername());
                        }
                    }
                }
            });
        }

        @Override
        public void onMessageFailed(MessageClient messageClient, Message message, MessageFailureInfo messageFailureInfo) {
            Toast.makeText(MessagingActivity.this, "Message failed to send.", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onMessageDelivered(MessageClient messageClient, MessageDeliveryInfo messageDeliveryInfo) {
        }

        @Override
        public void onShouldSendPushData(MessageClient messageClient, Message message, List<PushPair> pushPairs) {
        }
    }
}
