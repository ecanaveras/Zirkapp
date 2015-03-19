package com.ecp.gsy.dcs.zirkapp.app.util.task;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.ecp.gsy.dcs.zirkapp.app.util.adapters.UsersAdapter;
import com.ecp.gsy.dcs.zirkapp.app.util.locations.Location;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.FindParseObject;
import com.parse.ParseUser;

import java.util.List;

/**
 * Created by Elder on 18/03/2015.
 */
public class RefreshDataUsersOnline extends AsyncTask<Integer, Void, List<ParseUser>> {
    private Location currentLocation;
    private ParseUser currentUser;
    private LinearLayout layoudUsersNoFound, layoudUsersFinder;

    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView listUsersOnline;
    private Context context;

    public RefreshDataUsersOnline(Context context, ParseUser currentUser, Location currentLocation, ListView listUsersOnline) {
        this.context = context;
        this.currentUser = currentUser;
        this.currentLocation = currentLocation;
        this.listUsersOnline = listUsersOnline;
    }

    public RefreshDataUsersOnline(Context context, ParseUser currentUser, Location currentLocation, ListView listUsersOnline, SwipeRefreshLayout swipeRefreshLayout) {
        this.context = context;
        this.currentUser = currentUser;
        this.currentLocation = currentLocation;
        this.listUsersOnline = listUsersOnline;
        this.swipeRefreshLayout = swipeRefreshLayout;
    }

    public RefreshDataUsersOnline(Context context, ParseUser currentUser, Location currentLocation, ListView listUsersOnline, SwipeRefreshLayout swipeRefreshLayout, LinearLayout layoudUsersNoFound, LinearLayout layoudUsersFinder) {
        this.context = context;
        this.currentUser = currentUser;
        this.currentLocation = currentLocation;
        this.layoudUsersNoFound = layoudUsersNoFound;
        this.layoudUsersFinder = layoudUsersFinder;
        this.listUsersOnline = listUsersOnline;
        this.swipeRefreshLayout = swipeRefreshLayout;
    }

    @Override
    protected void onPreExecute() {
        if (layoudUsersFinder != null)
            layoudUsersFinder.setVisibility(View.VISIBLE);
        if (layoudUsersNoFound != null)
            layoudUsersNoFound.setVisibility(View.GONE);
    }

    @Override
    protected List<ParseUser> doInBackground(Integer... integers) {
        return FindParseObject.findUsersLocation(currentUser, currentLocation, integers[0]);
    }

    @Override
    protected void onPostExecute(List<ParseUser> parseUsers) {
        UsersAdapter usersAdapter = new UsersAdapter(context, parseUsers);
        if (listUsersOnline != null)
            listUsersOnline.setAdapter(usersAdapter);
        usersAdapter.notifyDataSetChanged();
        //Cant para el drawer
        GlobalApplication.setCantUsersOnline(parseUsers.size());

        if (swipeRefreshLayout != null)
            swipeRefreshLayout.setRefreshing(false);

        boolean zimessFound = parseUsers.size() > 0;

        if (layoudUsersFinder != null)
            layoudUsersFinder.setVisibility(View.GONE);

        if (zimessFound && layoudUsersNoFound != null) //Si hay Usuarios
            layoudUsersNoFound.setVisibility(View.GONE);
        if (!zimessFound && layoudUsersNoFound != null)// No hay Usuarios
            layoudUsersNoFound.setVisibility(View.VISIBLE);

    }
}
