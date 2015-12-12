package com.ecp.gsy.dcs.zirkapp.app.util.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ecp.gsy.dcs.zirkapp.app.fragments.ChatFragment;
import com.ecp.gsy.dcs.zirkapp.app.fragments.ChatHistoryFragment;
import com.ecp.gsy.dcs.zirkapp.app.fragments.ZimessFragment;

/**
 * Created by ecanaveras on 12/12/2015.
 */
public class CounterNotifiReceiver extends BroadcastReceiver {

    public static final String ACTION_LISTENER = "broadcast.notifi.counter";

    public CounterNotifiReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean isMessage = intent.getBooleanExtra("isMessage", false);
        if (ZimessFragment.isRunning()) {
            ZimessFragment z = ZimessFragment.getInstance();
            z.findNotifications();
        }
        if (isMessage) {
            if (ChatFragment.isRunning()) {
                ChatFragment ch = ChatFragment.getInstance();
                ch.setupCountTabMessages();
            }
            if (ChatHistoryFragment.isRunning()) {
                ChatHistoryFragment c = ChatHistoryFragment.getInstance();
                c.findParseMessageHistory();
            }
        }
    }
}