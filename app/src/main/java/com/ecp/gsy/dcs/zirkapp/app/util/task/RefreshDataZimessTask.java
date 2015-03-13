package com.ecp.gsy.dcs.zirkapp.app.util.task;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.ecp.gsy.dcs.zirkapp.app.util.adapters.ZimessAdapter;
import com.ecp.gsy.dcs.zirkapp.app.util.beans.Zimess;
import com.ecp.gsy.dcs.zirkapp.app.util.locations.Location;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.FindParseObject;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Elder on 09/03/2015.
 */
public class RefreshDataZimessTask extends AsyncTask<Integer, Void, List<Zimess>> {

    private ListView listViewZimess;
    private Activity activity;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout layoudZimessNoFound;
    private LinearLayout layoudZimessFinder;
    private Location currentLocation;

    public RefreshDataZimessTask(Activity activity, Location currentLocation, ListView listViewZimess, LinearLayout layoudZimessNoFound, LinearLayout layoudZimessFinder, SwipeRefreshLayout swipeRefreshLayout) {
        this.currentLocation = currentLocation;
        this.activity = activity;
        this.layoudZimessNoFound = layoudZimessNoFound;
        this.swipeRefreshLayout = swipeRefreshLayout;
        this.listViewZimess = listViewZimess;
        this.layoudZimessFinder = layoudZimessFinder;
    }

    @Override
    protected void onPreExecute() {
        if (layoudZimessFinder != null)
            layoudZimessFinder.setVisibility(View.VISIBLE);
        if (layoudZimessNoFound != null)
            layoudZimessNoFound.setVisibility(View.GONE);

    }

    @Override
    protected List<Zimess> doInBackground(Integer... integers) {
        List<Zimess> zimessArrayList = new ArrayList<Zimess>();
        if (currentLocation != null) {
            for (ParseObject zimess : FindParseObject.findZimess(currentLocation, integers[0])) {
                //Log.d("zimessText", zimess.get("zimessText").toString());
                Zimess zimessNew = new Zimess();
                zimessNew.setZimessId(zimess.getObjectId());
                zimessNew.setUser(zimess.getParseUser("user"));
                zimessNew.setZimessText(zimess.get("zimessText").toString());
                zimessNew.setLocation(zimess.getParseGeoPoint("location"));
                zimessNew.setCreateAt(zimess.getCreatedAt());

                //Se busca el perfil
                zimessNew.setProfile(FindParseObject.findProfile(zimess.getParseUser("user")));
                //la cantidad de comentarios
                zimessNew.setCantComment(zimess.getInt("cant_comment"));

                zimessArrayList.add(zimessNew);
            }

        }
        return zimessArrayList;
    }


    @Override
    protected void onPostExecute(List<Zimess> zimessArrayList) {
        ZimessAdapter zimessAdapterNew = new ZimessAdapter(activity, zimessArrayList, currentLocation);

        listViewZimess.setAdapter(zimessAdapterNew);
        zimessAdapterNew.notifyDataSetChanged();

        //Update Cant Zimess cerca
        Intent intent = new Intent("actualizarcantnotifi");
        intent.putExtra("datos", zimessArrayList.size());
        activity.sendBroadcast(intent);

        if (layoudZimessFinder != null) {
            layoudZimessFinder.setVisibility(View.GONE);
        }

        boolean zimessFound = zimessArrayList.size() > 0;

        if (zimessFound && layoudZimessNoFound != null) //Si hay Zimess
            layoudZimessNoFound.setVisibility(View.GONE);
        if (!zimessFound && layoudZimessNoFound != null)// No hay Zimess
            layoudZimessNoFound.setVisibility(View.VISIBLE);

        if (swipeRefreshLayout != null)
            swipeRefreshLayout.setRefreshing(false);
    }
}
