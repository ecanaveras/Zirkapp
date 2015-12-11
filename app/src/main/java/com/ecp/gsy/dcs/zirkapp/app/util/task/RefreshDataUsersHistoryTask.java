package com.ecp.gsy.dcs.zirkapp.app.util.task;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.ecp.gsy.dcs.zirkapp.app.util.adapters.UsersAdapter;
import com.ecp.gsy.dcs.zirkapp.app.util.adapters.UsersRecyclerAdapter;
import com.ecp.gsy.dcs.zirkapp.app.util.beans.ItemChatHistory;
import com.ecp.gsy.dcs.zirkapp.app.util.locations.Location;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Elder on 18/03/2015.
 */
public class RefreshDataUsersHistoryTask extends AsyncTask<Integer, Void, List<ItemChatHistory>> {

    private ArrayList<ItemChatHistory> userList;
    private LinearLayout layoutUsersNoFound, layoutUsersFinder;

    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView listUsersOnline;
    private Context context;
    private TextView lblChatNoFound;

    public RefreshDataUsersHistoryTask(Context context, ArrayList<ItemChatHistory> userList, ListView listHistory, TextView lblChatNoFound, LinearLayout layoutUsersFinder) {
        this.context = context;
        this.userList = userList;
        this.listUsersOnline = listHistory;
        this.lblChatNoFound = lblChatNoFound;
        this.layoutUsersFinder = layoutUsersFinder;
    }

    @Override
    protected void onPreExecute() {
        if (layoutUsersFinder != null)
            layoutUsersFinder.setVisibility(View.VISIBLE);
        if (layoutUsersNoFound != null)
            layoutUsersNoFound.setVisibility(View.GONE);

    }

    @Override
    protected List<ItemChatHistory> doInBackground(Integer... integers) {
        return userList;
    }

    @Override
    protected void onPostExecute(List<ItemChatHistory> chatHistories) {
        if (listUsersOnline != null) {
            UsersAdapter usersAdapter = new UsersAdapter(context, chatHistories);
            listUsersOnline.setAdapter(usersAdapter);
            usersAdapter.notifyDataSetChanged();
        }

        if (swipeRefreshLayout != null)
            swipeRefreshLayout.setRefreshing(false);

        boolean usersFound = chatHistories != null && chatHistories.size() > 0;

        if (layoutUsersFinder != null)
            layoutUsersFinder.setVisibility(View.GONE);

        if (lblChatNoFound != null) {
            if (!usersFound) {
                lblChatNoFound.setVisibility(View.VISIBLE);
            } else {
                lblChatNoFound.setVisibility(View.GONE);
            }
        }

        if (layoutUsersNoFound != null) {
            if (usersFound) { //Si hay Usuarios
                layoutUsersNoFound.setVisibility(View.GONE);
            } else { // No hay Usuarios
                layoutUsersNoFound.setVisibility(View.VISIBLE);
            }
        }
    }
}

