package com.ecp.gsy.dcs.zirkapp.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.ecp.gsy.dcs.zirkapp.app.GlobalApplication;
import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.models.ParseZMessage;
import com.ecp.gsy.dcs.zirkapp.app.util.task.RefreshDataUsersTask;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Elder on 18/04/2015.
 */
public class ChatHistoryActivity extends ActionBarActivity {

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

        ParseQuery<ParseZMessage> query = ParseQuery.getQuery(ParseZMessage.class);
        query.whereEqualTo(ParseZMessage.RECIPIENT_ID, currentUser);
        query.orderByAscending("createdAt");
        query.findInBackground(new FindCallback<ParseZMessage>() {
            @Override
            public void done(List<ParseZMessage> zMessages, ParseException e) {
                if (e == null) {
                    for (ParseZMessage parseObj : zMessages) {
                        sendersId.add(parseObj.getSenderId().getObjectId());
                    }
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
}
