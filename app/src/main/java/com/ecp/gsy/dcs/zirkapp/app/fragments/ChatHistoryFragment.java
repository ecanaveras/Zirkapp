package com.ecp.gsy.dcs.zirkapp.app.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.task.RefreshDataUsersOnline;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Elder on 11/04/2015.
 */
public class ChatHistoryFragment extends Fragment {

    private ParseUser currentUser;
    private ListView listViewHistory;
    private LinearLayout layoudHistoryFinder;
    private TextView lblChatNoFound;
    private boolean searching;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_history, container, false);

        currentUser = ParseUser.getCurrentUser();

        iniciarlizarCompUI(view);
        findLocalMessageHistory();

        return view;
    }

    private void iniciarlizarCompUI(View view) {
        listViewHistory = (ListView) view.findViewById(R.id.historyChatListView);
        layoudHistoryFinder = (LinearLayout) view.findViewById(R.id.layoudHistoryFinder);
        lblChatNoFound = (TextView) view.findViewById(R.id.lblChatNoFound);
    }

    /**
     * Busca los mensajes previos en Local
     */
    private void findLocalMessageHistory() {
        if (searching)
            return;

        searching = true;

        final ArrayList<String> sendersId = new ArrayList<>();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("ParseMessage");
        query.whereEqualTo("recipientId", currentUser.getObjectId());
        query.orderByAscending("createdAt");
        query.fromLocalDatastore();
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {
                    for (ParseObject parseObj : parseObjects) {
                        sendersId.add(parseObj.getString("senderId"));
                    }
                    Set<String> uniqueSenders = new HashSet<String>(sendersId);
                    new RefreshDataUsersOnline(getActivity(), currentUser, new ArrayList<String>(uniqueSenders), listViewHistory, lblChatNoFound, layoudHistoryFinder, searching).execute();
                } else {
                    Log.e("Parse.chat.history", e.getMessage());
                }
            }
        });
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (currentUser != null)
            findLocalMessageHistory();
        super.setUserVisibleHint(isVisibleToUser);
    }
}
