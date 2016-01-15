package com.ecp.gsy.dcs.zirkapp.app.util.task;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.ecp.gsy.dcs.zirkapp.app.util.adapters.LastMessageAdapter;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.DataParseHelper;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.models.ParseZLastMessage;
import com.parse.ParseUser;

import java.util.List;

/**
 * Created by Elder on 14/01/2016.
 */
public class RefreshDataLastMessage extends AsyncTask<ParseUser, Void, List<ParseZLastMessage>> {

    private String currentUserId;
    private TextView lblChatNoFound;
    private Context context;
    private ListView listUsersLastMsg;
    private LinearLayout layoutUsersNoFound, layoutUsersFinder;

    public RefreshDataLastMessage(Context context, ListView listHistory, TextView lblChatNoFound, LinearLayout layoutUsersFinder) {
        this.context = context;
        this.listUsersLastMsg = listHistory;
        this.lblChatNoFound = lblChatNoFound;
        this.layoutUsersFinder = layoutUsersFinder;
    }

    @Override
    protected void onPreExecute() {
        if (lblChatNoFound != null)
            lblChatNoFound.setVisibility(View.GONE);
        if (layoutUsersFinder != null)
            layoutUsersFinder.setVisibility(View.VISIBLE);
        if (layoutUsersNoFound != null)
            layoutUsersNoFound.setVisibility(View.GONE);

    }

    @Override
    protected List<ParseZLastMessage> doInBackground(ParseUser... currentUser) {
        currentUserId = currentUser[0].getObjectId();
        return DataParseHelper.findLastMessage(currentUser[0]);
    }

    @Override
    protected void onPostExecute(List<ParseZLastMessage> parseZLastMessages) {
        if (listUsersLastMsg != null) {
            LastMessageAdapter adapter = new LastMessageAdapter(context, parseZLastMessages, currentUserId);
            listUsersLastMsg.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }

        boolean msgFound = parseZLastMessages.size() > 0;

        if (layoutUsersFinder != null)
            layoutUsersFinder.setVisibility(View.GONE);

        if (lblChatNoFound != null) {
            if (!msgFound) {
                lblChatNoFound.setVisibility(View.VISIBLE);
            } else {
                lblChatNoFound.setVisibility(View.GONE);
            }
        }

        if (layoutUsersNoFound != null) {
            if (msgFound) { //Si hay Usuarios
                layoutUsersNoFound.setVisibility(View.GONE);
            } else { // No hay Usuarios
                layoutUsersNoFound.setVisibility(View.VISIBLE);
            }
        }
    }
}
