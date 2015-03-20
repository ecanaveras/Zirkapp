package com.ecp.gsy.dcs.zirkapp.app.util.task;

import android.app.Activity;
import android.app.Fragment;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

import com.ecp.gsy.dcs.zirkapp.app.DetailZimessActivity;
import com.ecp.gsy.dcs.zirkapp.app.util.adapters.ZimessReciclerAdapter;
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

    private RecyclerView recyclerView;
    private Fragment fragment;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout layoudZimessNoFound;
    private LinearLayout layoudZimessFinder;
    private Location currentLocation;
    private boolean findUniqueZimess = false;
    private Zimess zimessDetail;
    private GlobalApplication globalApplication;
    private DetailZimessActivity detailZimessActivity;

    public RefreshDataZimessTask(Fragment fragment, Location currentLocation, RecyclerView recyclerView, LinearLayout layoudZimessNoFound, LinearLayout layoudZimessFinder, SwipeRefreshLayout swipeRefreshLayout) {
        this.currentLocation = currentLocation;
        this.fragment = fragment;
        this.layoudZimessNoFound = layoudZimessNoFound;
        this.swipeRefreshLayout = swipeRefreshLayout;
        this.recyclerView = recyclerView;
        this.layoudZimessFinder = layoudZimessFinder;
    }

    public RefreshDataZimessTask(Fragment fragment, Zimess zimessDetail) {
        this.fragment = fragment;
        this.zimessDetail = zimessDetail;
        this.findUniqueZimess = this.zimessDetail != null;
    }

    public RefreshDataZimessTask(DetailZimessActivity detailZimessActivity, Zimess zimessDetail) {
        this.detailZimessActivity = detailZimessActivity;
        this.zimessDetail = zimessDetail;
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
        List<Zimess> zimessList = new ArrayList<Zimess>();
        if (currentLocation != null && !findUniqueZimess) {
            for (ParseObject parseZimess : FindParseObject.findZimessLocation(currentLocation, integers[0])) {
                zimessList.add(getZimess(parseZimess));
            }
            //Cant de Zimess en el Drawer
            GlobalApplication.setCantZimess(zimessList.size());
        }

        if (findUniqueZimess) {
            ParseObject parseZimess = FindParseObject.findZimess(zimessDetail.getZimessId());
            if (parseZimess != null)
                zimessList.add(getZimess(parseZimess));
        }

        return zimessList;
    }


    @Override
    protected void onPostExecute(List<Zimess> zimessList) {
        if (!findUniqueZimess) {
            ZimessReciclerAdapter zimessReciclerAdapter = new ZimessReciclerAdapter(zimessList, fragment, currentLocation);
            if (recyclerView != null) {
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager((Activity) fragment.getActivity());
                recyclerView.setLayoutManager(linearLayoutManager);
                recyclerView.setAdapter(zimessReciclerAdapter);
            }
            zimessReciclerAdapter.notifyDataSetChanged();
        } else {
            if (zimessList.size() > 0)
                zimessDetail = zimessList.get(0);
            if (detailZimessActivity != null) {
                DetailZimessActivity detailZimessAct = (DetailZimessActivity) detailZimessActivity;
                detailZimessAct.refreshDataZimess(zimessDetail);
            }
        }

        if (layoudZimessFinder != null) {
            layoudZimessFinder.setVisibility(View.GONE);
        }

        boolean zimessFound = zimessList.size() > 0;

        if (zimessFound && layoudZimessNoFound != null) //Si hay Zimess
            layoudZimessNoFound.setVisibility(View.GONE);
        if (!zimessFound && layoudZimessNoFound != null)// No hay Zimess
            layoudZimessNoFound.setVisibility(View.VISIBLE);

        if (swipeRefreshLayout != null)
            swipeRefreshLayout.setRefreshing(false);
    }

    /**
     * Crea un Zimess
     *
     * @param zimess
     * @return
     */
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
