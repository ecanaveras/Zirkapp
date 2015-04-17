package com.ecp.gsy.dcs.zirkapp.app.util.task;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

import com.ecp.gsy.dcs.zirkapp.app.DetailZimessActivity;
import com.ecp.gsy.dcs.zirkapp.app.util.adapters.ZimessReciclerAdapter;
import com.ecp.gsy.dcs.zirkapp.app.util.beans.Zimess;
import com.ecp.gsy.dcs.zirkapp.app.util.locations.Location;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.DataParseHelper;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Elder on 09/03/2015.
 */
public class RefreshDataZimessTask extends AsyncTask<Integer, Void, List<Zimess>> {

    public static final int RECIENTE = 0;
    public static final int CERCA = 1;
    public static final int LEJOS = 2;

    private RecyclerView recyclerView;
    private Context context;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout layoudZimessNoFound;
    private LinearLayout layoudZimessFinder;
    private Location currentLocation;
    private boolean findUniqueZimess = false;
    private Zimess zimessDetail;
    private DetailZimessActivity detailZimessActivity;
    private boolean findForUser;
    private ParseUser parseUser;
    private int sortZimess;

    /**
     * Busca los Zimess cerca
     *
     * @param fragment
     * @param currentLocation
     * @param recyclerView
     * @param layoudZimessNoFound
     * @param layoudZimessFinder
     * @param swipeRefreshLayout
     */
    public RefreshDataZimessTask(Fragment fragment, Location currentLocation, RecyclerView recyclerView, LinearLayout layoudZimessNoFound, LinearLayout layoudZimessFinder, SwipeRefreshLayout swipeRefreshLayout) {
        this.currentLocation = currentLocation;
        this.context = fragment.getActivity();
        this.layoudZimessNoFound = layoudZimessNoFound;
        this.swipeRefreshLayout = swipeRefreshLayout;
        this.recyclerView = recyclerView;
        this.layoudZimessFinder = layoudZimessFinder;
    }

    /**
     * Busca los Zimess cerca
     *
     * @param fragment
     * @param currentLocation
     * @param recyclerView
     * @param layoudZimessNoFound
     * @param layoudZimessFinder
     * @param swipeRefreshLayout
     */
    public RefreshDataZimessTask(Fragment fragment, Location currentLocation, RecyclerView recyclerView, LinearLayout layoudZimessNoFound, LinearLayout layoudZimessFinder, SwipeRefreshLayout swipeRefreshLayout, int sort) {
        this.currentLocation = currentLocation;
        this.context = fragment.getActivity();
        this.layoudZimessNoFound = layoudZimessNoFound;
        this.swipeRefreshLayout = swipeRefreshLayout;
        this.recyclerView = recyclerView;
        this.layoudZimessFinder = layoudZimessFinder;
        this.sortZimess = sort;
    }


    /**
     * Busca un unico Zimess (Detail)
     *
     * @param detailZimessActivity
     * @param zimessDetail
     */
    public RefreshDataZimessTask(DetailZimessActivity detailZimessActivity, Zimess zimessDetail) {
        this.detailZimessActivity = detailZimessActivity;
        this.zimessDetail = zimessDetail;
        this.findUniqueZimess = this.zimessDetail != null;
    }


    /**
     * Busca los Zimess de un usuario
     *
     * @param activity
     * @param parseUser
     * @param currentLocation
     * @param recyclerView
     * @param layoudZimessNoFound
     * @param layoudZimessFinder
     */
    public RefreshDataZimessTask(Activity activity, ParseUser parseUser, Location currentLocation, RecyclerView recyclerView, LinearLayout layoudZimessNoFound, LinearLayout layoudZimessFinder) {
        this.currentLocation = currentLocation;
        this.context = activity;
        this.layoudZimessNoFound = layoudZimessNoFound;
        this.recyclerView = recyclerView;
        this.layoudZimessFinder = layoudZimessFinder;
        this.parseUser = parseUser;
        findForUser = this.parseUser != null;
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
        //Buscar por ubicacion
        if (currentLocation != null && !findForUser && !findUniqueZimess) {
            for (ParseObject parseZimess : DataParseHelper.findZimessLocation(currentLocation, integers[0], integers[1], sortZimess)) {
                zimessList.add(getZimess(parseZimess));
            }
            //Cant de Zimess en el Drawer
            GlobalApplication.setCantZimess(zimessList.size());
        }

        //Buscar por Usuario
        if (findForUser) {
            for (ParseObject parseZimess : DataParseHelper.findZimess(parseUser)) {
                zimessList.add(getZimess(parseZimess));
            }
        }

        //Buscar unico Zimess
        if (findUniqueZimess) {
            ParseObject parseZimess = DataParseHelper.findZimess(zimessDetail.getZimessId());
            if (parseZimess != null)
                zimessList.add(getZimess(parseZimess));
        }

        return zimessList;
    }


    @Override
    protected void onPostExecute(List<Zimess> zimessList) {
        if (!findUniqueZimess) {
            ZimessReciclerAdapter zimessReciclerAdapter = new ZimessReciclerAdapter(zimessList, context, currentLocation);
            if (recyclerView != null) {
                recyclerView.setAdapter(zimessReciclerAdapter);
                //recyclerView.setHasFixedSize(true);
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
