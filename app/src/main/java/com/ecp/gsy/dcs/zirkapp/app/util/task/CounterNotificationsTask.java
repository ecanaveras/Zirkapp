package com.ecp.gsy.dcs.zirkapp.app.util.task;

import android.content.Context;
import android.graphics.drawable.LayerDrawable;
import android.os.AsyncTask;
import android.view.MenuItem;

import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.Utils;
import com.parse.ParseCloud;
import com.parse.ParseException;

import java.util.HashMap;

/**
 * Created by ecanaveras on 10/12/2015.
 */
public class CounterNotificationsTask extends AsyncTask<Integer, Void, Integer> {

    public static final int MENU_ITEM_NOTIFI = 0;
    public static final int MENU_ITEM_MESSAGES = 1;
    private LayerDrawable icon;
    private Context context;
    private int flag;

    public CounterNotificationsTask(Context context, LayerDrawable icon, int flag) {
        this.context = context;
        this.icon = icon;
        this.flag = flag;
    }

    @Override
    protected Integer doInBackground(Integer... params) {
        Integer cantMessages = null;
        if (icon != null && context != null) {
            switch (flag) {
                case CounterNotificationsTask.MENU_ITEM_NOTIFI:
                    cantMessages = getCantAllNotifications("getTotalNotificactionsNoRead");
                    break;
                case CounterNotificationsTask.MENU_ITEM_MESSAGES:
                    cantMessages = getCantAllNotifications("getTotalMessagesNoRead");
                    break;
            }
        }
        return cantMessages;
    }

    @Override
    protected void onPostExecute(Integer result) {
        if (icon != null && context != null && result != null) {
            //Actualizar el contador
            Utils.setBadgeCount(context, icon, result);
        }
    }

    private Integer getCantAllNotifications(String nameFunctionCloud) {
        HashMap params = new HashMap<String, Object>();
        try {
            return (Integer) ParseCloud.callFunction(nameFunctionCloud, params);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
