package com.ecp.gsy.dcs.zirkapp.app.fragments;

import android.app.Fragment;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.ecp.gsy.dcs.zirkapp.app.util.parse.models.ParseZMessage;
import com.ecp.gsy.dcs.zirkapp.app.util.task.RefreshDataUsersHistoryTask;
import com.ecp.gsy.dcs.zirkapp.app.util.task.RefreshDataUsersTask;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

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
        if (globalApplication.isConectedToInternet()) {
            layoutInternetOff.setVisibility(View.GONE);

            final ArrayList<ParseUser> sendersId = new ArrayList<>();
            final ArrayList<ItemChatHistory> chatHistories = new ArrayList<>();

            ParseQuery<ParseZHistory> innerQuery = ParseQuery.getQuery(ParseZHistory.class);
            innerQuery.whereEqualTo(ParseZHistory.USER, currentUser);

            final ParseUser[] userIds = {currentUser};
            ParseQuery<ParseZMessage> query = ParseQuery.getQuery(ParseZMessage.class);
            query.whereMatchesKeyInQuery(ParseZMessage.SINCH_ID, ParseZHistory.SINCH_ID, innerQuery);
            query.include(ParseZMessage.SENDER_ID);
            query.include(ParseZMessage.RECIPIENT_ID);
            query.orderByDescending("createdAt");

            query.findInBackground(new FindCallback<ParseZMessage>() {
                @Override
                public void done(List<ParseZMessage> zzMessages, ParseException e) {
                    if (e == null) {
                        for (ParseZMessage parseObj : zzMessages) {

                            if (!parseObj.getSenderId().equals(currentUser) && !sendersId.contains(parseObj.getSenderId())) {
                                sendersId.add(parseObj.getSenderId());
                                ItemChatHistory chatHistory = new ItemChatHistory();
                                chatHistory.setUserMessage(parseObj.getSenderId());
                                chatHistory.setLastMessage(parseObj);
                                chatHistory.setCantMessagesNoRead(!parseObj.isMessageRead() ? getCantMessages(parseObj.getSenderId().getObjectId(), parseObj.getRecipientId().getObjectId()) : null);
                                chatHistories.add(chatHistory);
                            }
                            if (!parseObj.getRecipientId().equals(currentUser) && !sendersId.contains(parseObj.getRecipientId())) {
                                sendersId.add(parseObj.getRecipientId());
                                ItemChatHistory chatHistory = new ItemChatHistory();
                                chatHistory.setUserMessage(parseObj.getRecipientId());
                                chatHistory.setLastMessage(parseObj);
                                chatHistories.add(chatHistory);
                            }
                        }
                        new RefreshDataUsersHistoryTask(getActivity(), chatHistories, listViewHistory, lblChatNoFound, layoudHistoryFinder).execute();
                    } else {
                        Log.e("Parse.chat.history", e.getMessage());
                    }
                }
            });
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
                            Toast.makeText(getActivity(), getResources().getString(R.string.msgChatDeleteOk), Toast.LENGTH_SHORT).show();
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
                ParseUser receptorUser = (ParseUser) listViewHistory.getAdapter().getItem(acmi.position);
                Intent intent = new Intent(getActivity(), UserProfileActivity.class);
                globalApplication.setProfileParseUser(receptorUser);
                startActivity(intent);
                return true;
            case R.id.ctx_delete_chat:
                ParseUser parseUser = (ParseUser) listViewHistory.getAdapter().getItem(acmi.position);
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
