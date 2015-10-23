package com.ecp.gsy.dcs.zirkapp.app.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ecp.gsy.dcs.zirkapp.app.GlobalApplication;
import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.models.ParseZHistory;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.models.ParseZMessage;
import com.ecp.gsy.dcs.zirkapp.app.util.task.RefreshDataUsersTask;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Elder on 18/04/2015.
 */
public class ChatHistoryActivity extends AppCompatActivity {

    private ParseUser currentUser;
    private ListView listViewHistory;
    private LinearLayout layoudHistoryFinder;
    private TextView lblChatNoFound;
    private boolean searching;
    private Toolbar toolbar;
    private ChatHistoryActivity activity;
    private GlobalApplication globalApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_history);

        globalApplication = (GlobalApplication) getApplicationContext();

        currentUser = ParseUser.getCurrentUser();

        activity = this;

        iniciarlizarCompUI();
        findParseMessageHistory();
    }

    private void iniciarlizarCompUI() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        listViewHistory = (ListView) findViewById(R.id.historyChatListView);
        registerForContextMenu(listViewHistory);
        listViewHistory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ParseUser parseUser = (ParseUser) adapterView.getAdapter().getItem(i);
                abrirConversa(parseUser);
            }
        });

        layoudHistoryFinder = (LinearLayout) findViewById(R.id.layoudHistoryFinder);
        lblChatNoFound = (TextView) findViewById(R.id.lblChatNoFound);
    }

    /**
     * Busca los mensajes previos en Local
     */
    private void findParseMessageHistory() {
        final ArrayList<String> sendersId = new ArrayList<>();

        ParseQuery<ParseZHistory> innerQuery = ParseQuery.getQuery(ParseZHistory.class);
        innerQuery.whereEqualTo(ParseZHistory.USER, currentUser);

        ParseUser[] userIds = {currentUser};
        ParseQuery<ParseZMessage> query = ParseQuery.getQuery(ParseZMessage.class);
        query.whereMatchesKeyInQuery(ParseZMessage.SINCH_ID, ParseZHistory.SINCH_ID, innerQuery);

        query.findInBackground(new FindCallback<ParseZMessage>() {
            @Override
            public void done(List<ParseZMessage> zzMessages, ParseException e) {
                if (e == null) {
                    for (ParseZMessage parseObj : zzMessages) {
                        sendersId.add(parseObj.getSenderId().getObjectId());
                        sendersId.add(parseObj.getRecipientId().getObjectId());
                    }
                    sendersId.removeAll(Arrays.asList(new String[]{currentUser.getObjectId()}));
                    Set<String> uniqueSenders = new HashSet<String>(sendersId);
                    new RefreshDataUsersTask(activity, currentUser, new ArrayList<String>(uniqueSenders), listViewHistory, lblChatNoFound, layoudHistoryFinder).execute();
                } else {
                    Log.e("Parse.chat.history", e.getMessage());
                }
            }
        });
    }

    /**
     * Abre la conversacionde un usuario
     *
     * @param parseUserDestino
     */
    private void abrirConversa(ParseUser parseUserDestino) {
        globalApplication = (GlobalApplication) this.getApplicationContext();
        globalApplication.setCustomParseUser(parseUserDestino);
        Intent intent = new Intent(this.getApplicationContext(), MessagingActivity.class);
        startActivity(intent);
    }

    /**
     * Busca y elimina la conversacion del usuario selecionado
     *
     * @param userChat
     */
    private void deleteParseMessageHistory(final ParseUser userChat) {
        String formatMessage = "%s \"%s\"...";
        String nameUser = userChat.getString("name") != null ? userChat.getString("name") : userChat.getUsername();
        AlertDialog.Builder alert = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
        alert.setMessage(String.format(formatMessage, getResources().getString(R.string.msgByeChat2), nameUser));
        alert.setPositiveButton(getString(R.string.lblDelete), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Delete
                final ProgressDialog dialog = new ProgressDialog(ChatHistoryActivity.this);
                dialog.setMessage(getResources().getString(R.string.msgDeleting));
                dialog.show();

                ParseUser[] userIds = {currentUser, userChat};

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
                            Toast.makeText(ChatHistoryActivity.this, getResources().getString(R.string.msgChatDeleteOk), Toast.LENGTH_SHORT).show();
                            findParseMessageHistory();
                        }
                        dialog.dismiss();
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

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.historyChatListView) {
            AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) menuInfo;
        }
        getMenuInflater().inflate(R.menu.menu_contextual_users, menu);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.ctx_view_profile:
                ParseUser receptorUser = (ParseUser) listViewHistory.getAdapter().getItem(acmi.position);
                Intent intent = new Intent(this, UserProfileActivity.class);
                globalApplication.setCustomParseUser(receptorUser);
                startActivity(intent);
                return true;
            case R.id.ctx_delete_chat:
                ParseUser parseUser = (ParseUser) listViewHistory.getAdapter().getItem(acmi.position);
                if (parseUser != null) {
                    deleteParseMessageHistory(parseUser);
                }
                return true;
            case R.id.ctx_lock_user:
                Toast.makeText(this, "Proximamente...", Toast.LENGTH_SHORT).show();
            default:
                return super.onContextItemSelected(item);
        }
    }
}
