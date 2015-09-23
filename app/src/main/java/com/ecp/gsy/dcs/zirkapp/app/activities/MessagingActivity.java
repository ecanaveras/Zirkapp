package com.ecp.gsy.dcs.zirkapp.app.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
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

import com.ecp.gsy.dcs.zirkapp.app.GlobalApplication;
import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.adapters.MessageAdapter;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.models.ParseZHistory;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.models.ParseZMessage;
import com.ecp.gsy.dcs.zirkapp.app.util.sinch.SinchBaseActivity;
import com.ecp.gsy.dcs.zirkapp.app.util.task.SendPushTask;
import com.gc.materialdesign.views.ButtonRectangle;
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

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
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

    private String receptorId, receptorUsername, receptorName;
    private EditText txtMessageBodyField;
    private ParseUser currentUser, receptorUser;
    private ListView listMessage;
    private GlobalApplication globalApplication;
    private ProgressBar progressBar;
    private MessageAdapter adapterMessage;
    private ButtonRectangle btnSendMessage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        globalApplication = (GlobalApplication) getApplicationContext();
        //Usuario actual
        currentUser = ParseUser.getCurrentUser();

        //Usuario receptor
        receptorUser = globalApplication.getCustomParseUser();
        if (receptorUser != null) {
            receptorId = receptorUser.getObjectId();
            receptorUsername = receptorUser.getUsername();
            receptorName = receptorUser.getString("name");
            Log.i("SinchReceptor", receptorUser.getObjectId());
        }

        if (getSinchServiceInterface() != null) {
            getSinchServiceInterface().addMessageClientListener(this);
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

        btnSendMessage = (ButtonRectangle) findViewById(R.id.btnSendMessage);

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
        adapterMessage.addMessage(message, messageDirection);

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
        innerQuery.setLimit(100);

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

    @Override
    public void onIncomingMessage(MessageClient messageClient, Message message) {
        adapterMessage.addMessage(message, MessageAdapter.DIRECTION_INCOMING);
    }

    @Override
    public void onMessageSent(MessageClient messageClient, Message message, String s) {
        //Guardar historial en parse.
        saveParseMessage(message, MessageAdapter.DIRECTION_OUTGOING);

        //Enviar notificacion.
        if (receptorId != null && message != null && !globalApplication.isListeningNotifi()) {
            String name = currentUser.getString("name") != null ? currentUser.getString("name") : currentUser.getUsername();
            new SendPushTask(currentUser.getObjectId(), receptorId, currentUser.getObjectId(), name, message.getTextBody(), SendPushTask.PUSH_CHAT).execute();
        }
    }

    @Override
    public void onMessageFailed(MessageClient messageClient, Message message, MessageFailureInfo messageFailureInfo) {
        Toast.makeText(MessagingActivity.this, "Tu mensaje no pudo ser enviado.", Toast.LENGTH_LONG).show();
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
        final String regId = new String(pushPairs.get(0).getPushData());
        //use an async task to make the http request
        class SendPushTask extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... voids) {
                HttpClient httpclient = new DefaultHttpClient();
                //url of where your backend is hosted, can't be local!
                HttpPost httppost = new HttpPost("http://www.zirkapp.com?reg_id=" + regId);
                try {
                    HttpResponse response = httpclient.execute(httppost);
                    ResponseHandler<String> handler = new BasicResponseHandler();
                    Log.d("HttpResponse", handler.handleResponse(response));
                } catch (ClientProtocolException e) {
                    Log.d("ClientProtocolException", e.toString());
                } catch (IOException e) {
                    Log.d("IOException", e.toString());
                }
                return null;
            }
        }
        (new SendPushTask()).execute();

    }
}
