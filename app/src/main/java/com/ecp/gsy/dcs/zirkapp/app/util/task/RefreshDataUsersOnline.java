package com.ecp.gsy.dcs.zirkapp.app.util.task;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.ecp.gsy.dcs.zirkapp.app.GlobalApplication;
import com.ecp.gsy.dcs.zirkapp.app.util.adapters.UsersAdapter;
import com.ecp.gsy.dcs.zirkapp.app.util.locations.Location;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.DataParseHelper;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Elder on 18/03/2015.
 */
public class RefreshDataUsersOnline extends AsyncTask<Integer, Void, List<ParseUser>> {
    private boolean searching;
    private ArrayList<String> userList;
    private Location currentLocation;
    private ParseUser currentUser;
    private LinearLayout layoudUsersNoFound, layoudUsersFinder;

    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView listUsersOnline;
    private Context context;
    private boolean isSearchHistory;
    private TextView lblChatNoFound;

    public RefreshDataUsersOnline(Context context, ParseUser currentUser, Location currentLocation, ListView listUsersOnline, SwipeRefreshLayout swipeRefreshLayout, LinearLayout layoudUsersNoFound, LinearLayout layoudUsersFinder) {
        this.context = context;
        this.currentUser = currentUser;
        this.currentLocation = currentLocation;
        this.layoudUsersNoFound = layoudUsersNoFound;
        this.layoudUsersFinder = layoudUsersFinder;
        this.listUsersOnline = listUsersOnline;
        this.swipeRefreshLayout = swipeRefreshLayout;
    }

    public RefreshDataUsersOnline(Context context, ParseUser currentUser, ArrayList<String> userList, ListView listHistory, TextView lblChatNoFound, LinearLayout layoudUsersFinder, boolean searching) {
        this.context = context;
        this.currentUser = currentUser;
        this.isSearchHistory = true;
        this.userList = userList;
        this.listUsersOnline = listHistory;
        this.lblChatNoFound = lblChatNoFound;
        this.searching = searching;
        this.layoudUsersFinder = layoudUsersFinder;
    }

    @Override
    protected void onPreExecute() {
        if (layoudUsersFinder != null)
            layoudUsersFinder.setVisibility(View.VISIBLE);
        if (layoudUsersNoFound != null)
            layoudUsersNoFound.setVisibility(View.GONE);
        searching = true;

    }

    @Override
    protected List<ParseUser> doInBackground(Integer... integers) {
        if (isSearchHistory)
            return DataParseHelper.findUsersList(userList);

        //Actualizar la ubicacion del usuario
        ParseGeoPoint parseGeoPoint = new ParseGeoPoint(currentLocation.getLatitud(), currentLocation.getLongitud());
        ParseUser parseUser = currentUser;
        parseUser.put("location", parseGeoPoint);
        parseUser.put("online", true);
        try {
            parseUser.save();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return DataParseHelper.findUsersLocation(currentUser, currentLocation, integers[0]);
    }

    @Override
    protected void onPostExecute(List<ParseUser> parseUsers) {
        UsersAdapter usersAdapter = new UsersAdapter(context, parseUsers);
        if (listUsersOnline != null)
            listUsersOnline.setAdapter(usersAdapter);
        usersAdapter.notifyDataSetChanged();
        //Cant para el drawer
        if (!isSearchHistory)
            GlobalApplication.setCantUsersOnline(parseUsers.size());

        if (swipeRefreshLayout != null)
            swipeRefreshLayout.setRefreshing(false);

        boolean usersFound = parseUsers.size() > 0;

        if (layoudUsersFinder != null)
            layoudUsersFinder.setVisibility(View.GONE);

        if (!usersFound && lblChatNoFound != null) {
            lblChatNoFound.setVisibility(View.VISIBLE);
        } else if (lblChatNoFound != null) {
            lblChatNoFound.setVisibility(View.GONE);
        }

        if (usersFound && layoudUsersNoFound != null) //Si hay Usuarios
            layoudUsersNoFound.setVisibility(View.GONE);
        if (!usersFound && layoudUsersNoFound != null)// No hay Usuarios
            layoudUsersNoFound.setVisibility(View.VISIBLE);

        searching = false;

    }
}
