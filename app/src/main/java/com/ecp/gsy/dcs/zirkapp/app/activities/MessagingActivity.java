package com.ecp.gsy.dcs.zirkapp.app.activities;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ecp.gsy.dcs.zirkapp.app.GlobalApplication;
import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.fragments.ChatFragment;
import com.ecp.gsy.dcs.zirkapp.app.fragments.ChatHistoryFragment;
import com.ecp.gsy.dcs.zirkapp.app.util.adapters.MessageAdapter;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.models.ParseZHistory;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.models.ParseZMessage;
import com.ecp.gsy.dcs.zirkapp.app.util.sinch.SinchBaseActivity;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Elder on 21/02/2015.
 */
public class MessagingActivity extends SinchBaseActivity implements MessageClientListener {

    private static final String TAG = MessagingActivity.class.getSimpleName();
    private static MessagingActivity instance = null;

    private String receptorId, receptorUsername, receptorName;
    private EditText txtMessageBodyField;
    private ParseUser currentUser, receptorUser;
    private ListView listMessage;
    private GlobalApplication globalApplication;
    private ProgressBar progressBar;
    private MessageAdapter adapterMessage;
    private ImageButton btnSendMessage;


    public static MessagingActivity getInstance() {
        return instance;
    }

    public static boolean isRunning() {
        return instance != null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        globalApplication = (GlobalApplication) getApplicationContext();
        //Usuario actual
        currentUser = ParseUser.getCurrentUser();

        //Usuario receptor
        receptorUser = globalApplication.getMessagingParseUser();

        if (receptorUser != null) {
            receptorId = receptorUser.getObjectId();
            receptorUsername = receptorUser.getUsername();
            receptorName = receptorUser.getString("name");
            Log.i("SinchReceptor", receptorUser.getObjectId());
        } else {
            finish();
        }

        if (getSinchServiceInterface() != null) {
            getSinchServiceInterface().addMessageClientListener(this);
        }

        initComponentUI();
        cancelNotification();
        findParseMessageHistory();

        instance = this;
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

        btnSendMessage = (ImageButton) findViewById(R.id.btnSendMessage);

        btnSendMessage.setOnClickListener(new View.OnClickListener() {
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
            globalApplication.setAvatarRoundedResize(receptorUser.getParseFile("avatar"), imageView, 100, 100);
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
                    globalApplication.setProfileParseUser(receptorUser);
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

        getSinchServiceInterface().sendMessage(receptorId, messageBody);
        txtMessageBodyField.setText("");
    }

    /**
     * Guarda el historial del chat de forma local
     *
     * @param message
     * @param messageDirection
     */
    private void saveParseMessage(final Message message, final Integer messageDirection) {
        //Guardar al enviar el mensaje
        if (MessageAdapter.DIRECTION_OUTGOING == messageDirection) {
            //Agrega el mensaje en parse si no existe.
            new AsyncTask<String, String, String>() {

                @Override
                protected String doInBackground(String... strings) {
                    ParseQuery<ParseZMessage> query = ParseQuery.getQuery(ParseZMessage.class);
                    query.whereEqualTo(ParseZMessage.SINCH_ID, message.getMessageId());
                    query.findInBackground(new FindCallback<ParseZMessage>() {
                        @Override
                        public void done(List<ParseZMessage> zMessages, ParseException e) {
                            if (e == null) {
                                if (zMessages.size() == 0) {
                                    final ParseZMessage parseZMessage = new ParseZMessage();
                                    parseZMessage.setSinchId(message.getMessageId());
                                    parseZMessage.setSenderId(currentUser);
                                    parseZMessage.setRecipientId(receptorUser);
                                    parseZMessage.setMessageText(message.getTextBody());
                                    parseZMessage.setMessageRead(false);
                                    parseZMessage.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            if (e == null) {
                                                saveParseHistory(parseZMessage, message, currentUser); //Usuario que envia el mensaje
                                                saveParseHistory(parseZMessage, message, receptorUser); //Usuario que recibe el mensaje
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    });
                    return null;
                }
            }.execute();

        }
    }

    /**
     * Guarda el historial en Parse
     *
     * @param zMessage
     * @param message
     * @param user
     */
    private void saveParseHistory(ParseZMessage zMessage, Message message, ParseUser user) {
        ParseZHistory parseZHistory = new ParseZHistory();
        parseZHistory.setUser(user);
        parseZHistory.setSinchId(message.getMessageId());
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
        innerQuery.setLimit(200);

        ParseUser[] userIds = {currentUser, receptorUser};
        ParseQuery<ParseZMessage> query = ParseQuery.getQuery(ParseZMessage.class);
        query.whereContainedIn(ParseZMessage.SENDER_ID, Arrays.asList(userIds));
        query.whereContainedIn(ParseZMessage.RECIPIENT_ID, Arrays.asList(userIds));
        query.whereMatchesKeyInQuery(ParseZMessage.SINCH_ID, ParseZHistory.SINCH_ID, innerQuery);
        query.setLimit(200);
        query.orderByAscending("createdAt");
        query.findInBackground(new FindCallback<ParseZMessage>() {
            @Override
            public void done(List<ParseZMessage> zMessages, ParseException e) {
                if (e == null) {
                    List<ParseObject> messageLeidos = new ArrayList<>();
                    for (ParseZMessage parseZmessa : zMessages) {
                        final ParseZMessage copyZmessa = parseZmessa;
                        final Message message = new Message() {
                            @Override
                            public String getMessageId() {
                                return copyZmessa.getSinchId();
                            }

                            @Override
                            public Map<String, String> getHeaders() {
                                return null;
                            }

                            @Override
                            public String getTextBody() {
                                return copyZmessa.getMessageText();
                            }

                            @Override
                            public List<String> getRecipientIds() {
                                return new ArrayList<>(Arrays.asList(new String[]{copyZmessa.getRecipientId().getObjectId()}));
                            }

                            @Override
                            public String getSenderId() {
                                return copyZmessa.getSenderId().getObjectId();
                            }

                            @Override
                            public Date getTimestamp() {
                                return copyZmessa.getCreatedAt();
                            }
                        };
                        if (parseZmessa.getSenderId().equals(currentUser)) {
                            adapterMessage.addMessage(message, MessageAdapter.DIRECTION_OUTGOING);
                        } else {
                            adapterMessage.addMessage(message, MessageAdapter.DIRECTION_INCOMING);
                            if (!parseZmessa.isMessageRead()) {
                                parseZmessa.setMessageRead(true);
                                messageLeidos.add(parseZmessa);
                            }
                        }
                    }
                    ParseObject.saveAllInBackground(messageLeidos);
                    if (messageLeidos.size() > 0) {
                        //Actualiza la cantidad de mensajes no leidos en el tab Mensajes - Chatfragment
                        if (ChatFragment.isRunning()) {
                            ChatFragment parent = ChatFragment.getInstance();
                            parent.setupCountTabMessages();
                        }
                        if (ChatHistoryFragment.isRunning()) {
                            ChatHistoryFragment c = ChatHistoryFragment.getInstance();
                            c.findParseMessageHistory();
                        }
                    }
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
        AlertDialog.Builder alert = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
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

    private void cancelNotification() {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) this.getSystemService(ns);
        nMgr.cancel(receptorId, 200);
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
        globalApplication.setMessagingParseUser(null);
        if (getSinchServiceInterface() != null) {
            getSinchServiceInterface().removeMessageClientListener(this);
        }
        super.onDestroy();
    }

    @Override
    public void onServiceConnected() {
        getSinchServiceInterface().addMessageClientListener(this);
        btnSendMessage.setEnabled(true);
    }

    @Override
    public void onServiceDisconnected() {
        btnSendMessage.setEnabled(false);
    }

    @Override
    public void onBackPressed() {
        globalApplication.setMessagingParseUser(null);
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

    @Override
    public void onIncomingMessage(MessageClient messageClient, Message message) {
        adapterMessage.addMessage(message, MessageAdapter.DIRECTION_INCOMING);
        /*MediaPlayer mp = MediaPlayer.create(this, R.raw.new_message);
        mp.start();*/
    }

    @Override
    public void onMessageSent(MessageClient messageClient, Message message, String s) {
        adapterMessage.addMessage(message, MessageAdapter.DIRECTION_OUTGOING);
        //Enviar notificacion
        String name = currentUser.getString("name") != null ? currentUser.getString("name") : currentUser.getUsername();
        new SendPushTask(receptorUser, currentUser.getObjectId(), name, message.getTextBody(), null, SendPushTask.PUSH_CHAT).execute();
        //Guardar historial en parse.
        saveParseMessage(message, MessageAdapter.DIRECTION_OUTGOING);
    }

    @Override
    public void onMessageFailed(MessageClient messageClient, Message message, MessageFailureInfo messageFailureInfo) {
        Toast.makeText(MessagingActivity.this, "Out!!! Tu mensaje no pudo ser enviado.", Toast.LENGTH_LONG).show();
        StringBuilder sb = new StringBuilder();
        sb.append("Sending failed: ")
                .append(messageFailureInfo.getSinchError().getMessage());
        Log.d(TAG, sb.toString());
    }

    @Override
    public void onMessageDelivered(MessageClient messageClient, MessageDeliveryInfo messageDeliveryInfo) {
        //Log.d(TAG, "onDelivered");
    }

    @Override
    public void onShouldSendPushData(MessageClient messageClient, Message message, List<PushPair> pushPairs) {
        //final String regId = new String(pushPairs.get(0).getPushData());
        //Enviar notificacion.
        if (receptorUser != null && message != null) {
            String name = currentUser.getString("name") != null ? currentUser.getString("name") : currentUser.getUsername();
            new SendPushTask(receptorUser, currentUser.getObjectId(), name, message.getTextBody(), pushPairs, SendPushTask.PUSH_CHAT).execute();
        }
        //Log.d("onShouldSendPushData", "success");
    }

    public ParseUser getReceptorUser() {
        return receptorUser;
    }
}
