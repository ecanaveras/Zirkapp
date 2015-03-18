package com.ecp.gsy.dcs.zirkapp.app.util.task;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.ecp.gsy.dcs.zirkapp.app.DetailZimessActivity;
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
    private boolean findOneZimess = false;
    private Zimess zimessDetail;

    public RefreshDataZimessTask(Activity activity, Location currentLocation, ListView listViewZimess, LinearLayout layoudZimessNoFound, LinearLayout layoudZimessFinder, SwipeRefreshLayout swipeRefreshLayout) {
        this.currentLocation = currentLocation;
        this.activity = activity;
        this.layoudZimessNoFound = layoudZimessNoFound;
        this.swipeRefreshLayout = swipeRefreshLayout;
        this.listViewZimess = listViewZimess;
        this.layoudZimessFinder = layoudZimessFinder;
    }

    public RefreshDataZimessTask(Activity activity, Zimess zimessDetail) {
        this.activity = activity;
        this.zimessDetail = zimessDetail;
        this.findOneZimess = this.zimessDetail != null;
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
        if (currentLocation != null && !findOneZimess) {
            for (ParseObject parseZimess : FindParseObject.findZimessLocation(currentLocation, integers[0])) {
                zimessArrayList.add(getZimess(parseZimess));
            }

        }

        if (findOneZimess) {
            ParseObject parseZimess = FindParseObject.findZimess(zimessDetail.getZimessId());
            if (parseZimess != null)
                zimessArrayList.add(getZimess(parseZimess));
        }

        return zimessArrayList;
    }


    @Override
    protected void onPostExecute(List<Zimess> zimessArrayList) {
        if (!findOneZimess) {
            ZimessAdapter zimessAdapterNew = new ZimessAdapter(activity, zimessArrayList, currentLocation);

            listViewZimess.setAdapter(zimessAdapterNew);
            zimessAdapterNew.notifyDataSetChanged();

            //Update Cant Zimess cerca
            Intent intent = new Intent("actualizarcantnotifi");
            intent.putExtra("datos", zimessArrayList.size());
            activity.sendBroadcast(intent);
        } else {
            if (zimessArrayList.size() > 0)
                zimessDetail = zimessArrayList.get(0);
            DetailZimessActivity detailZimessActivity = (DetailZimessActivity) activity;
            detailZimessActivity.refreshDataZimess(zimessDetail);
        }

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

    private Zimess getZimess(ParseObject zimess) {
        if (zimess != null) {
            Zimess zimessNew = new Zimess();
            zimessNew.setZimessId(zimess.getObjectId());
            zimessNew.setUser(zimess.getParseUser("user"));
            zimessNew.setZimessText(zimess.get("zimessText").toString());
            zimessNew.setLocation(zimess.getParseGeoPoint("location"));
            zimessNew.setCantComment(zimess.getInt("cant_comment"));
            zimessNew.setCreateAt(zimess.getCreatedAt());
            return zimessNew;
        }
        return null;
    }
}
