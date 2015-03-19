package com.ecp.gsy.dcs.zirkapp.app.util.messages;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.UserProfileActivity;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        activity = this;

        globalApplication = (GlobalApplication) getApplicationContext();

        bindService(new Intent(this, MessageService.class), serviceConnection, BIND_AUTO_CREATE);


        //Lista
        listMessage = (ListView) findViewById(R.id.listMessages);
        adapterMessage = new MessageAdapter(this);
        listMessage.setAdapter(adapterMessage);

        //Usuario actual
        currentUser = globalApplication.getCurrentUser();

        //Usuario receptor
        receptorUser = globalApplication.getCustomParseUser();
        if (receptorUser != null) {
            receptorId = receptorUser.getObjectId();
            receptorUsername = receptorUser.getUsername();
            receptorName = receptorUser.getString("name");
        }
//        Intent intent = getIntent();
//        receptorId = intent.getStringExtra("RECIPIENT_ID");
//        receptorUsername = intent.getStringExtra("RECIPIENT_USERNAME");
//        receptorName = intent.getStringExtra("RECIPIENT_NAME");

        //Personalizar ActionBar
        customActionBar(getActionBar());

        populateMessageHistory();

        txtMessageBodyField = (EditText) findViewById(R.id.txtMessageBodyField);

        findViewById(R.id.btnSendMessage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
    }

    private void customActionBar(ActionBar actionBar) {
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));


            View customView = getLayoutInflater().inflate(R.layout.actionbar_user_title, null);
            ImageView imageView = (ImageView) customView.findViewById(R.id.imgAvatar);
            Bitmap avatar = GlobalApplication.getAvatar(receptorUser);
            if (avatar != null) {
//                Resources resources = getResources();
//                BitmapDrawable icon = new BitmapDrawable(resources, avatar);
//                actionBar.setIcon(icon);
                imageView.setImageBitmap(avatar);
            } else {
                imageView.setImageResource(R.drawable.ic_user_male);
            }
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    view.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.anim_image_click));
                    onBackPressed();
                }
            });
            LinearLayout layoutActionBarTitle = (LinearLayout) customView.findViewById(R.id.layoutActionbarTitle);
            TextView titleBar = (TextView) customView.findViewById(R.id.actionbarTitle);
            TextView subTitleBar = (TextView) customView.findViewById(R.id.actionbarSubTitle);
            titleBar.setText(receptorName != null ? receptorName : receptorUsername);
            if (receptorName != null) {
                subTitleBar.setText(receptorUsername);
            }else{
                subTitleBar.setVisibility(View.GONE);
            }
            layoutActionBarTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    view.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.anim_image_click));
                    Intent intent = new Intent(activity, UserProfileActivity.class);
                    globalApplication.setCustomParseUser(receptorUser);
                    activity.startActivity(intent);
                }
            });
            actionBar.setCustomView(customView);
        }
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
        String[] userIds = {currentUser.getObjectId(), receptorId};
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
                        if (parseObj.get("senderId").toString().equals(currentUser.getObjectId())) {
                            adapterMessage.addMessage(message, MessageAdapter.DIRECTION_OUTGOING, currentUser.getUsername());
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

    @Override
    public void onBackPressed() {
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
                            parseMessage.put("senderId", currentUser.getObjectId());
                            parseMessage.put("recipientId", writableMessage.getRecipientIds().get(0));
                            parseMessage.put("messageText", writableMessage.getTextBody());
                            parseMessage.put("sinchId", writableMessage.getMessageId());
                            parseMessage.saveInBackground();

                            adapterMessage.addMessage(writableMessage, MessageAdapter.DIRECTION_OUTGOING, currentUser.getUsername());
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
