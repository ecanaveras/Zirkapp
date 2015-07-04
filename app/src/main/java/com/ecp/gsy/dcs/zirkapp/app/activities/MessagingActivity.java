package com.ecp.gsy.dcs.zirkapp.app.activities;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
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

import com.alertdialogpro.AlertDialogPro;
import com.ecp.gsy.dcs.zirkapp.app.GlobalApplication;
import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.adapters.MessageAdapter;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.models.ParseZHistory;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.models.ParseZMessage;
import com.ecp.gsy.dcs.zirkapp.app.util.services.MessageService;
import com.ecp.gsy.dcs.zirkapp.app.util.task.SendPushTask;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.messaging.Message;
import com.sinch.android.rtc.messaging.MessageClient;
import com.sinch.android.rtc.messaging.MessageClientListener;
import com.sinch.android.rtc.messaging.MessageDeliveryInfo;
import com.sinch.android.rtc.messaging.MessageFailureInfo;
import com.sinch.android.rtc.messaging.WritableMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Elder on 21/02/2015.
 */
public class MessagingActivity extends ActionBarActivity {

    private String receptorId, receptorUsername, receptorName;
    private EditText txtMessageBodyField;
    private ParseUser currentUser, receptorUser;
    private ListView listMessage;
    private GlobalApplication globalApplication;
    private ProgressBar progressBar;
    private WritableMessage writableMessage;
    private MessageAdapter adapterMessage;

    //Sinch
    private MessageService.MessageServiceInterface messageService;
    private ServiceConnection serviceConnection = new MyServiceConnection();
    private MyMessageClientListener messageClientListener = new MyMessageClientListener();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

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

        findParseMessageHistory();
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
                    view.startAnimation(AnimationUtils.loadAnimation(MessagingActivity.this, R.anim.anim_image_click));
                    Intent intent = new Intent(MessagingActivity.this, UserProfileActivity.class);
                    //intent.putExtra("activityfrom", MessagingActivity.class.getSimpleName());
                    globalApplication.setCustomParseUser(receptorUser);
                    MessagingActivity.this.startActivity(intent);
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
    private void saveParseMessage(final Message message, final WritableMessage writableMessage, final String senderId, final Integer messageDirection) {
        adapterMessage.addMessage(writableMessage, messageDirection, receptorId);

        //Guardar al enviar el mensaje
        if (MessageAdapter.DIRECTION_OUTGOING == messageDirection) {
            //Agrega el mensaje en parse si no existe.
            ParseQuery<ParseZMessage> query = ParseQuery.getQuery(ParseZMessage.class);
            query.whereEqualTo(ParseZMessage.SINCH_ID, message.getMessageId());
            query.findInBackground(new FindCallback<ParseZMessage>() {
                @Override
                public void done(List<ParseZMessage> zMessages, ParseException e) {
                    if (e == null) {
                        if (zMessages.size() == 0) {
                            final ParseZMessage parseZMessage = new ParseZMessage();
                            parseZMessage.setSinchId(writableMessage.getMessageId());
                            parseZMessage.setSenderId(currentUser);
                            parseZMessage.setRecipientId(receptorUser);
                            parseZMessage.setMessageText(writableMessage.getTextBody());
                            parseZMessage.setMessageRead(false);
                            parseZMessage.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        saveParseHistory(parseZMessage, writableMessage, currentUser); //Usuario que envia el mensaje
                                        saveParseHistory(parseZMessage, writableMessage, receptorUser); //Usuario que recibe el mensaje
                                    }
                                }
                            });
                        }
                    }
                }
            });
        }
    }

    /**
     * Guarda el historial en Parse
     *
     * @param zMessage
     * @param writableMessage
     * @param user
     */
    private void saveParseHistory(ParseZMessage zMessage, WritableMessage writableMessage, ParseUser user) {
        ParseZHistory parseZHistory = new ParseZHistory();
        parseZHistory.setUser(user);
        parseZHistory.setSinchId(writableMessage.getMessageId());
        parseZHistory.setZMessageId(zMessage);
        parseZHistory.saveInBackground();
    }

    /**
     * Busca los mensajes previos en parse
     */
    private void findParseMessageHistory() {
        progressBar.setVisibility(View.VISIBLE);

        //Buscar los sinchId del usuario actual
        ParseQuery<ParseZHistory> innerQuery = ParseQuery.getQuery(ParseZHistory.class);
        innerQuery.whereEqualTo(ParseZHistory.USER, currentUser);

        ParseUser[] userIds = {currentUser, receptorUser};
        ParseQuery<ParseZMessage> query = ParseQuery.getQuery(ParseZMessage.class);
        query.whereContainedIn(ParseZMessage.SENDER_ID, Arrays.asList(userIds));
        query.whereContainedIn(ParseZMessage.RECIPIENT_ID, Arrays.asList(userIds));
        query.whereMatchesKeyInQuery(ParseZMessage.SINCH_ID, ParseZHistory.SINCH_ID, innerQuery);
        query.setLimit(100);
        query.orderByAscending("createdAt");
        query.findInBackground(new FindCallback<ParseZMessage>() {
            @Override
            public void done(List<ParseZMessage> zMessages, ParseException e) {
                if (e == null) {
                    List<ParseObject> messageLeidos = new ArrayList<>();
                    for (ParseZMessage parseZmessa : zMessages) {
                        WritableMessage message = new WritableMessage(parseZmessa.getRecipientId().getObjectId(), parseZmessa.getMessageText());
                        if (parseZmessa.getSenderId().equals(currentUser)) {
                            adapterMessage.addMessage(message, MessageAdapter.DIRECTION_OUTGOING, currentUser.getUsername());
                        } else {
                            adapterMessage.addMessage(message, MessageAdapter.DIRECTION_INCOMING, receptorUsername);
                            parseZmessa.setMessageRead(true);
                            messageLeidos.add(parseZmessa);
                        }
                    }
                    ParseObject.saveAllInBackground(messageLeidos);
                } else {
                    Log.e("Parse.chat.history", e.getMessage());
                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Elimina los mensajes almacenados en parse
     */
    private void deleteParseMessageHistory() {
        AlertDialogPro.Builder alert = new AlertDialogPro.Builder(this);
        alert.setMessage(getString(R.string.msgByeChat));
        alert.setPositiveButton(getString(R.string.lblDelete), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Delete
                final ProgressDialog dialog = new ProgressDialog(MessagingActivity.this);
                dialog.setMessage(getResources().getString(R.string.msgDeleting));
                dialog.show();

                ParseUser[] userIds = {currentUser, receptorUser};

                //Buscar los sinchId de los mensajes de la conversacion
                ParseQuery<ParseZMessage> innerQuery = ParseQuery.getQuery(ParseZMessage.class);
                innerQuery.whereContainedIn(ParseZMessage.SENDER_ID, Arrays.asList(userIds));
                innerQuery.whereContainedIn(ParseZMessage.RECIPIENT_ID, Arrays.asList(userIds));

                //Buscar los sinchId de usuario actual
                ParseQuery<ParseZHistory> query = ParseQuery.getQuery(ParseZHistory.class);
                query.whereMatchesKeyInQuery(ParseZHistory.SINCH_ID, ParseZMessage.SINCH_ID, innerQuery);
                query.whereEqualTo(ParseZHistory.USER, currentUser);
                query.findInBackground(new FindCallback<ParseZHistory>() {
                    @Override
                    public void done(List<ParseZHistory> zHistories, ParseException e) {
                        if (e == null) {
                            ParseObject.deleteAllInBackground(zHistories);
                            Toast.makeText(MessagingActivity.this, getResources().getString(R.string.msgChatDeleteOk), Toast.LENGTH_SHORT).show();
                        }
                        dialog.dismiss();
                        onBackPressed();
                    }
                });
            }
        });

        alert.setNegativeButton(getString(R.string.lblCancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        alert.show();
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
            case R.id.action_bar_delete_chat:
                deleteParseMessageHistory();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_messaging, menu);
        return true;
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

                adapterMessage.addMessage(writableMessage, MessageAdapter.DIRECTION_INCOMING, receptorId);
                //Log.i("incoming.message", message.getTextBody());
            }
        }

        @Override
        public void onMessageSent(MessageClient messageClient, Message message, String s) {
            writableMessage = new WritableMessage(message.getRecipientIds().get(0), message.getTextBody());

            //Guardar historial en parse.
            saveParseMessage(message, writableMessage, currentUser.getObjectId(), MessageAdapter.DIRECTION_OUTGOING);

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
            //Entregado
        }

        @Override
        public void onShouldSendPushData(MessageClient messageClient, final Message message, final List<PushPair> pushPairs) {

            //Enviar notificacion.
//            if (receptorId != null && pushPairs.size() > 0 && message != null) {
//                String name = currentUser.getString("name") != null ? currentUser.getString("name") : currentUser.getUsername();
//                new SendPushTask(currentUser.getObjectId(), receptorId, currentUser.getObjectId(), name, message.getTextBody(), pushPairs, SendPushTask.PUSH_CHAT).execute();
//            }

        }
    }
}
