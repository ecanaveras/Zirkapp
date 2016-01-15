package com.ecp.gsy.dcs.zirkapp.app.util.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ecp.gsy.dcs.zirkapp.app.fragments.ChatHistoryFragment;
import com.ecp.gsy.dcs.zirkapp.app.fragments.MainFragment;
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
            ZimessFragment.getInstance().findNotifications();
        }
        if (isMessage) {
            if (MainFragment.isRunning()) {
                MainFragment.getInstance().setupCountTabMessages();
            }
            if (ChatHistoryFragment.isRunning()) {
                ChatHistoryFragment.getInstance().findParseMessageHistory();
            }
        }
    }
}