package com.ecp.gsy.dcs.zirkapp.app.fragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ecp.gsy.dcs.zirkapp.app.GlobalApplication;
import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.activities.MessagingActivity;
import com.ecp.gsy.dcs.zirkapp.app.activities.UserProfileActivity;
import com.ecp.gsy.dcs.zirkapp.app.util.beans.ItemChatHistory;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.models.ParseZHistory;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.models.ParseZLastMessage;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.models.ParseZMessage;
import com.ecp.gsy.dcs.zirkapp.app.util.task.RefreshDataUsersHistoryTask;
import com.parse.FindCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Elder on 11/04/2015.
 */
public class ChatHistoryFragment extends Fragment {

    private static ChatHistoryFragment instance = null;

    private ParseUser currentUser;
    private ListView listViewHistory;
    private LinearLayout layoudHistoryFinder;
    private TextView lblChatNoFound;
    private GlobalApplication globalApplication;
    private LinearLayout layoutInternetOff;
    private boolean findHistory;

    public static ChatHistoryFragment newInstance(Bundle arguments) {
        ChatHistoryFragment chatHistoryFragment = new ChatHistoryFragment();
        if (arguments != null) {
            chatHistoryFragment.setArguments(arguments);
        }
        return chatHistoryFragment;
    }

    public static ChatHistoryFragment getInstance() {
        return instance;
    }

    public static boolean isRunning() {
        return instance != null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_history, container, false);

        globalApplication = (GlobalApplication) getActivity().getApplicationContext();

        currentUser = ParseUser.getCurrentUser();

        iniciarlizarCompUI(view);
        findParseMessageHistory();

        instance = this;

        return view;
    }

    private void iniciarlizarCompUI(View view) {
        layoutInternetOff = (LinearLayout) view.findViewById(R.id.layoutInternetOff);
        listViewHistory = (ListView) view.findViewById(R.id.historyChatListView);
        registerForContextMenu(listViewHistory);


        listViewHistory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ItemChatHistory chatHistory = (ItemChatHistory) adapterView.getAdapter().getItem(i);
                TextView textView = (TextView) view.findViewById(R.id.lblCantMessages);
                abrirConversa(chatHistory.getUserMessage());
                textView.setText(null);
                textView.setVisibility(View.GONE);
            }
        });

        layoudHistoryFinder = (LinearLayout) view.findViewById(R.id.layoudHistoryFinder);
        lblChatNoFound = (TextView) view.findViewById(R.id.lblChatNoFound);
    }

    /**
     * Busca los mensajes previos en Parse
     */
    public void findParseMessageHistory() {
        if (findHistory) {//Controla si se está buscando info
            return;
        }
        if (globalApplication.isConectedToInternet()) {
            new AsyncTask<String, String, String>() {

                @Override
                protected void onPreExecute() {
                    findHistory = true;
                    layoutInternetOff.setVisibility(View.GONE);
                    layoudHistoryFinder.setVisibility(View.VISIBLE);
                }

                @Override
                protected String doInBackground(String... params) {
                    final ArrayList<ParseUser> sendersId = new ArrayList<>();
                    final ArrayList<ItemChatHistory> chatHistories = new ArrayList<>();

                    ParseQuery<ParseZLastMessage> querySender = ParseQuery.getQuery(ParseZLastMessage.class);
                    querySender.whereEqualTo(ParseZLastMessage.SENDER_ID, currentUser);

                    ParseQuery<ParseZLastMessage> queryRecipient = ParseQuery.getQuery(ParseZLastMessage.class);
                    queryRecipient.whereEqualTo(ParseZLastMessage.RECIPIENT_ID, currentUser);

                    String[] userId = {currentUser.getObjectId()};
                    ParseQuery<ParseZLastMessage> query = ParseQuery.or(Arrays.asList(querySender, queryRecipient));
                    query.whereNotContainedIn(ParseZLastMessage.DELETE_FOR, Arrays.asList(userId));
                    query.include(ParseZLastMessage.SENDER_ID);
                    query.include(ParseZLastMessage.RECIPIENT_ID);
                    query.include(ParseZLastMessage.ZMESSAGE_ID);
                    query.orderByDescending("updatedAt");
                    query.findInBackground(new FindCallback<ParseZLastMessage>() {
                        @Override
                        public void done(List<ParseZLastMessage> zMessages, ParseException e) {
                            if (e == null) {
                                for (ParseZLastMessage parseObj : zMessages) {
                                    if (!parseObj.getSenderId().getObjectId().equals(currentUser.getObjectId()) && !sendersId.contains(parseObj.getSenderId())) {
                                        sendersId.add(parseObj.getSenderId());
                                        ItemChatHistory chatHistory = new ItemChatHistory();
                                        chatHistory.setUserMessage(parseObj.getSenderId());
                                        chatHistory.setLastMessage(parseObj.getZMessageId());
                                        chatHistory.setCantMessagesNoRead(getCantMessages(parseObj.getSenderId().getObjectId(), parseObj.getRecipientId().getObjectId()));
                                        chatHistories.add(chatHistory);
                                    }
                                    if (!parseObj.getRecipientId().getObjectId().equals(currentUser.getObjectId()) && !sendersId.contains(parseObj.getRecipientId())) {
                                        sendersId.add(parseObj.getRecipientId());
                                        ItemChatHistory chatHistory = new ItemChatHistory();
                                        chatHistory.setUserMessage(parseObj.getRecipientId());
                                        chatHistory.setLastMessage(parseObj.getZMessageId());
                                        chatHistory.setIsSender(true);
                                        chatHistories.add(chatHistory);
                                    }
                                }
                                if (chatHistories.size() > 0) {
                                    new RefreshDataUsersHistoryTask(getActivity(), chatHistories, listViewHistory, lblChatNoFound, layoudHistoryFinder).execute();
                                }
                            } else {
                                Log.e("Parse.chat.history", e.getMessage());
                            }
                        }
                    });
                    return null;
                }

                @Override
                protected void onPostExecute(String s) {
                    findHistory = false;
                    layoudHistoryFinder.setVisibility(View.GONE);
                }
            }.execute();


        } else {
            layoudHistoryFinder.setVisibility(View.GONE);
            layoutInternetOff.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Abre la conversacionde un usuario
     *
     * @param parseUserDestino
     */
    private void abrirConversa(ParseUser parseUserDestino) {
        globalApplication.setMessagingParseUser(parseUserDestino);
        Intent intent = new Intent(getActivity().getApplicationContext(), MessagingActivity.class);
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
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle);
        alert.setMessage(String.format(formatMessage, getResources().getString(R.string.msgByeChat2), nameUser));
        alert.setPositiveButton(getString(R.string.lblDelete), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Delete
                final ProgressDialog dialog = new ProgressDialog(getActivity());
                dialog.setMessage(getResources().getString(R.string.msgDeleting));
                dialog.show();

                final ParseUser[] userIds = {currentUser, userChat};

                //Buscar los sinchId de los mensajes de la conversacion
                final ParseQuery<ParseZMessage> innerQuery = ParseQuery.getQuery(ParseZMessage.class);
                innerQuery.whereContainedIn(ParseZMessage.SENDER_ID, Arrays.asList(userIds));
                innerQuery.whereContainedIn(ParseZMessage.RECIPIENT_ID, Arrays.asList(userIds));
                innerQuery.whereLessThan(ParseZMessage.CANT_HIST_DELETE, 2);
                innerQuery.setLimit(500);
                innerQuery.orderByAscending("createdAt");

                //Buscar los sinchId de usuario actual
                ParseQuery<ParseZHistory> query = ParseQuery.getQuery(ParseZHistory.class);
                query.whereMatchesKeyInQuery(ParseZHistory.SINCH_ID, ParseZMessage.SINCH_ID, innerQuery);
                query.whereEqualTo(ParseZHistory.USER, currentUser);
                query.findInBackground(new FindCallback<ParseZHistory>() {
                    @Override
                    public void done(List<ParseZHistory> zHistories, ParseException e) {
                        if (e == null) {
                            ParseObject.deleteAllInBackground(zHistories);

                            Toast.makeText(getActivity(), getResources().getString(R.string.msgChatDeleteOk), Toast.LENGTH_SHORT).show();
                            //Buscar y actualizar LastMessage
                            ParseQuery<ParseZLastMessage> lastQuery = ParseQuery.getQuery(ParseZLastMessage.class);
                            lastQuery.whereContainedIn(ParseZMessage.SENDER_ID, Arrays.asList(userIds));
                            lastQuery.whereContainedIn(ParseZMessage.RECIPIENT_ID, Arrays.asList(userIds));
                            lastQuery.findInBackground(new FindCallback<ParseZLastMessage>() {
                                @Override
                                public void done(List<ParseZLastMessage> list, ParseException e) {
                                    if (e == null) {
                                        for (int i = 0; i < list.size(); i++) {
                                            list.get(i).addDeleteFor(currentUser.getObjectId());
                                        }
                                        ParseObject.saveAllInBackground(list, new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                if (e == null) {
                                                    findHistory = false;
                                                    findParseMessageHistory();
                                                }
                                                dialog.dismiss();
                                            }
                                        });
                                    }
                                }
                            });
                        }

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

    /**
     * Invoca una funcion en Parse que devuelve la cantidad de mensajes entre los Id's enviados
     *
     * @param senderId
     * @param recipientId
     * @return
     */
    private Integer getCantMessages(String senderId, String recipientId) {
        HashMap params = new HashMap<String, Object>();
        params.put("sender", senderId);
        params.put("recipient", recipientId);
        try {
            return (Integer) ParseCloud.callFunction("getMessagesNoRead", params);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (currentUser != null)
            findParseMessageHistory();
        super.setUserVisibleHint(isVisibleToUser);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.historyChatListView) {
            AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) menuInfo;
        }
        getActivity().getMenuInflater().inflate(R.menu.menu_contextual_users, menu);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.ctx_view_profile:
                ParseUser receptorUser = ((ItemChatHistory) listViewHistory.getAdapter().getItem(acmi.position)).getUserMessage();
                Intent intent = new Intent(getActivity(), UserProfileActivity.class);
                globalApplication.setProfileParseUser(receptorUser);
                startActivity(intent);
                return true;
            case R.id.ctx_delete_chat:
                ParseUser parseUser = ((ItemChatHistory) listViewHistory.getAdapter().getItem(acmi.position)).getUserMessage();
                if (parseUser != null) {
                    deleteParseMessageHistory(parseUser);
                }
                return true;
            case R.id.ctx_lock_user:
                Toast.makeText(getActivity(), "Proximamente...", Toast.LENGTH_SHORT).show();
            default:
                return super.onContextItemSelected(item);
        }
    }
}
