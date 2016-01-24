package com.ecp.gsy.dcs.zirkapp.app.util.task;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

import com.ecp.gsy.dcs.zirkapp.app.GlobalApplication;
import com.ecp.gsy.dcs.zirkapp.app.util.adapters.UsersRecyclerAdapter;
import com.ecp.gsy.dcs.zirkapp.app.util.locations.Location;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.DataParseHelper;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

import java.util.List;

/**
 * Created by Elder on 18/03/2015.
 */
public class RefreshDataUsersTask extends AsyncTask<Integer, Void, List<ParseUser>> {
    private boolean searching;
    private Location currentLocation;
    private ParseUser currentUser;
    private LinearLayout layoutUsersNoFound, layoutUsersFinder;

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private Context context;
    private String gender;

    /**
     * Busca los usuarios que esten cerca y online
     *
     * @param context
     * @param currentUser
     * @param currentLocation
     * @param recyclerView
     */
    public RefreshDataUsersTask(Context context, ParseUser currentUser, Location currentLocation, RecyclerView recyclerView, String gender) {
        this.context = context;
        this.currentUser = currentUser;
        this.currentLocation = currentLocation;
        this.recyclerView = recyclerView;
        this.gender = gender;
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
        //Actualizar la ubicacion del usuario
        ParseGeoPoint parseGeoPoint = new ParseGeoPoint(currentLocation.getLatitud(), currentLocation.getLongitud());
        ParseUser parseUser = currentUser;
        parseUser.put("location", parseGeoPoint);
        parseUser.put("online", true);
        parseUser.saveInBackground();
        return DataParseHelper.findUsersLocation(currentUser, currentLocation, integers[0], gender);
    }

    @Override
    protected void onPostExecute(List<ParseUser> parseUsers) {
        if (recyclerView != null) {
            UsersRecyclerAdapter adapter = new UsersRecyclerAdapter(context, parseUsers);
            recyclerView.setAdapter(adapter);
        }

        GlobalApplication.setCantUsersOnline(parseUsers.size());

        if (swipeRefreshLayout != null)
            swipeRefreshLayout.setRefreshing(false);

        boolean usersFound = parseUsers.size() > 0;

        if (layoutUsersFinder != null)
            layoutUsersFinder.setVisibility(View.GONE);

        if (layoutUsersNoFound != null) {
            if (usersFound) { //Si hay Usuarios
                layoutUsersNoFound.setVisibility(View.GONE);
            } else { // No hay Usuarios
                layoutUsersNoFound.setVisibility(View.VISIBLE);
            }
        }

        //Mostrar notificacion cuando hay usuarios nuevos online
        new CounterUsersAroundTask(context).execute(parseUsers);

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
