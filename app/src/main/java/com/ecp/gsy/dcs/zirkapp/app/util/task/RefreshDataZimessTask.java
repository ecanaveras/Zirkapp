package com.ecp.gsy.dcs.zirkapp.app.util.task;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

import com.ecp.gsy.dcs.zirkapp.app.GlobalApplication;
import com.ecp.gsy.dcs.zirkapp.app.activities.DetailZimessActivity;
import com.ecp.gsy.dcs.zirkapp.app.util.adapters.ZimessRecyclerAdapter;
import com.ecp.gsy.dcs.zirkapp.app.util.locations.Location;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.DataParseHelper;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.models.ParseZimess;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Elder on 09/03/2015.
 */
public class RefreshDataZimessTask extends AsyncTask<Integer, Void, List<ParseZimess>> {

    public static final int RECIENTE = 0;
    public static final int CERCA = 1;
    public static final int LEJOS = 2;

    private Context context;

    private Location currentLocation;
    private boolean findUniqueZimess = false;
    private ParseZimess zimessDetail;
    private DetailZimessActivity detailZimessActivity;
    private boolean findForUser = false;
    private ParseUser parseUser;
    private int sortZimess;

    private RecyclerView recyclerView;
    private ZimessRecyclerAdapter zReciclerAdapter;

    //layout
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout layoutZimessNoFound;
    private LinearLayout layoutZimessFinder;

    /**
     * Busca los Zimes Cerca
     *
     * @param context
     * @param currentLocation
     * @param adapter
     */
    public RefreshDataZimessTask(Context context, Location currentLocation, RecyclerView recyclerView, ZimessRecyclerAdapter adapter) {
        this.context = context;
        this.currentLocation = currentLocation;
        this.zReciclerAdapter = adapter;
        this.recyclerView = recyclerView;
    }

    /**
     * Busca los Zimes Cerca y ordena
     *
     * @param context
     * @param currentLocation
     * @param adapter
     */
    public RefreshDataZimessTask(Context context, Location currentLocation, RecyclerView recyclerView, ZimessRecyclerAdapter adapter, int sortZimess) {
        this.context = context;
        this.currentLocation = currentLocation;
        this.zReciclerAdapter = adapter;
        this.recyclerView = recyclerView;
        this.sortZimess = sortZimess;
    }

    /**
     * Busca los Zimess de un Usuario.
     *
     * @param context
     * @param currentLocation
     * @param adapter
     * @param parseUser
     */
    public RefreshDataZimessTask(Context context, ParseUser parseUser, Location currentLocation, RecyclerView recyclerView, ZimessRecyclerAdapter adapter) {
        this.context = context;
        this.currentLocation = currentLocation;
        this.zReciclerAdapter = adapter;
        this.recyclerView = recyclerView;
        this.parseUser = parseUser;
        findForUser = this.parseUser != null;
    }

    /**
     * Busca un unico Zimess (Detail)
     *
     * @param detailZimessActivity
     * @param zimessDetail
     */
    public RefreshDataZimessTask(DetailZimessActivity detailZimessActivity, ParseZimess zimessDetail) {
        this.detailZimessActivity = detailZimessActivity;
        this.zimessDetail = zimessDetail;
        this.findUniqueZimess = this.zimessDetail != null;
    }

    @Override
    protected void onPreExecute() {
        if (layoutZimessFinder != null)
            layoutZimessFinder.setVisibility(View.VISIBLE);
        if (layoutZimessNoFound != null)
            layoutZimessNoFound.setVisibility(View.GONE);

    }

    @Override
    protected List<ParseZimess> doInBackground(Integer... integers) {
        List<ParseZimess> zimessResult = new ArrayList<>();
        //Buscar por ubicacion
        if (currentLocation != null && !findForUser && !findUniqueZimess) {
            for (ParseZimess parseZimess : DataParseHelper.findZimessLocation(currentLocation, integers[0], integers[1], sortZimess)) {
                zimessResult.add(parseZimess);
            }
            //Cant de Zimess en el Drawer
            GlobalApplication.setCantZimess(zimessResult.size());
        }

        //Buscar por Usuario
        if (findForUser) {
            for (ParseZimess parseZimess : DataParseHelper.findZimess(parseUser)) {
                zimessResult.add(parseZimess);
            }
        }

        //Buscar unico Zimess
        if (findUniqueZimess) {
            ParseZimess parseZimess = DataParseHelper.findZimess(zimessDetail.getObjectId());
            if (parseZimess != null)
                zimessResult.add(parseZimess);
        }

        return zimessResult;
    }

    @Override
    protected void onPostExecute(List<ParseZimess> zimessList) {
        if (!findUniqueZimess && context != null) {
            zReciclerAdapter = new ZimessRecyclerAdapter(context, zimessList, currentLocation);
            recyclerView.setAdapter(zReciclerAdapter);
            zReciclerAdapter.notifyDataSetChanged();
        } else {
            if (zimessList.size() > 0)
                zimessDetail = zimessList.get(0);
            if (detailZimessActivity != null) {
                DetailZimessActivity detailZimessAct = (DetailZimessActivity) detailZimessActivity;
                detailZimessAct.refreshDataZimess(zimessDetail);
            }
        }

        if (layoutZimessFinder != null) {
            layoutZimessFinder.setVisibility(View.GONE);
        }

        boolean zimessFound = zimessList.size() > 0;

        if (layoutZimessNoFound != null) {
            if (zimessFound) {//Si hay Zimess
                layoutZimessNoFound.setVisibility(View.GONE);
            } else {// No hay Zimess
                layoutZimessNoFound.setVisibility(View.VISIBLE);
            }
        }

        if (swipeRefreshLayout != null)
            swipeRefreshLayout.setRefreshing(false);
    }

    public void setSwipeRefreshLayout(SwipeRefreshLayout swipeRefreshLayout) {
        this.swipeRefreshLayout = swipeRefreshLayout;
    }

    public void setLayoutZimessNoFound(LinearLayout layoutZimessNoFound) {
        this.layoutZimessNoFound = layoutZimessNoFound;
    }

    public void setLayoutZimessFinder(LinearLayout layoutZimessFinder) {
        this.layoutZimessFinder = layoutZimessFinder;
    }
}
