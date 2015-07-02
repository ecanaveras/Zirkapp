package com.ecp.gsy.dcs.zirkapp.app.util.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.ecp.gsy.dcs.zirkapp.app.R;
import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Elder on 17/04/2015.
 */
public class CountMessagesReceiver extends BroadcastReceiver {

    public static final String ACTION_LISTENER = "broadcast.counter.messages";

    private ListView listUsers;
    private String senderId;
    private String recipientId;
    private TextView lblCantMessages;

    public CountMessagesReceiver(ListView listUsers) {
        this.listUsers = listUsers;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        senderId = intent.getStringExtra("senderId");
        recipientId = intent.getStringExtra("recipientId");
        if (listUsers == null || senderId == null || recipientId == null)
            return;

        updateCantMessages();
    }

    /**
     * Actualiza un elemento de la lista de usuarios online
     */
    private void updateCantMessages() {
        Log.d("broadcast.messages", "update");
        for (int i = 0; i < listUsers.getChildCount(); i++) {
            View v = listUsers.getChildAt(i - listUsers.getFirstVisiblePosition());
            if (v != null) {
                TextView lblUserId = (TextView) v.findViewById(R.id.lblUserId);
                if (lblUserId != null && lblUserId.getText().toString().equals(senderId)) {
                    lblCantMessages = (TextView) v.findViewById(R.id.lblCantMessages);
                    break;
                }
            }
        }

        if (lblCantMessages == null) return;

        lblCantMessages.setText("New");
        //Buscar chats.
        /*ParseQuery<ParseObject> query = ParseQuery.getQuery("ParseZMessage");
        query.whereEqualTo("senderId", senderId);
        query.whereEqualTo("recipientId", recipientId);
        query.whereEqualTo("messageRead", false);
        query.fromLocalDatastore();
        query.countInBackground(new CountCallback() {
            @Override
            public void done(int count, ParseException e) {
                if (e == null) {
                    if (count > 0) {
                        lblCantMessages.setText(String.valueOf(count));
                    } else {
                        lblCantMessages.setText(null);
                    }
                }
            }
        });*/

    }
}
