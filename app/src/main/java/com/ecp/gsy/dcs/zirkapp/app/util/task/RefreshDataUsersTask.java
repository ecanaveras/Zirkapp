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
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Elder on 18/03/2015.
 */
public class RefreshDataUsersTask extends AsyncTask<Integer, Void, List<ParseUser>> {
    private boolean searching;
    private ArrayList<String> userList;
    private Location currentLocation;
    private ParseUser currentUser;
    private LinearLayout layoutUsersNoFound, layoutUsersFinder;

    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView listUsersOnline;
    private Context context;
    private boolean isSearchHistory;
    private TextView lblChatNoFound;

    /**
     * Busca los usuarios que esten cerca y online
     *
     * @param context
     * @param currentUser
     * @param currentLocation
     * @param listUsersOnline
     */
    public RefreshDataUsersTask(Context context, ParseUser currentUser, Location currentLocation, ListView listUsersOnline) {
        this.context = context;
        this.currentUser = currentUser;
        this.currentLocation = currentLocation;
        this.listUsersOnline = listUsersOnline;
    }

    public RefreshDataUsersTask(Context context, ParseUser currentUser, ArrayList<String> userList, ListView listHistory, TextView lblChatNoFound, boolean searching) {
        this.context = context;
        this.currentUser = currentUser;
        this.isSearchHistory = true;
        this.userList = userList;
        this.listUsersOnline = listHistory;
        this.lblChatNoFound = lblChatNoFound;
        this.searching = searching;
    }

    @Override
    protected void onPreExecute() {
        if (layoutUsersFinder != null)
            layoutUsersFinder.setVisibility(View.VISIBLE);
        if (layoutUsersNoFound != null)
            layoutUsersNoFound.setVisibility(View.GONE);
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
        parseUser.saveInBackground();
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

        searching = false;

    }

    public void setLayoutUsersNoFound(LinearLayout layoutUsersNoFound) {
        this.layoutUsersNoFound = layoutUsersNoFound;
    }

    public void setLayoutUsersFinder(LinearLayout layoutUsersFinder) {
        this.layoutUsersFinder = layoutUsersFinder;
    }

    public void setSwipeRefreshLayout(SwipeRefreshLayout swipeRefreshLayout) {
        this.swipeRefreshLayout = swipeRefreshLayout;
    }
}
