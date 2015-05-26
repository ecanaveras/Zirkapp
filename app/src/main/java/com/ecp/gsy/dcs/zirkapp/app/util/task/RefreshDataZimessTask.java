package com.ecp.gsy.dcs.zirkapp.app.util.task;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

import com.ecp.gsy.dcs.zirkapp.app.GlobalApplication;
import com.ecp.gsy.dcs.zirkapp.app.activities.DetailZimessActivity;
import com.ecp.gsy.dcs.zirkapp.app.activities.MyZimessActivity;
import com.ecp.gsy.dcs.zirkapp.app.fragments.ZimessFragment;
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

    private Context context;

    private Location currentLocation;
    private boolean findUniqueZimess = false;
    private Zimess zimessDetail;
    private DetailZimessActivity detailZimessActivity;
    private boolean findForUser = false;
    private ParseUser parseUser;
    private int sortZimess;

    private RecyclerView recyclerView;
    private ZimessReciclerAdapter zReciclerAdapter;

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
    public RefreshDataZimessTask(Context context, Location currentLocation, RecyclerView recyclerView, ZimessReciclerAdapter adapter) {
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
    public RefreshDataZimessTask(Context context, Location currentLocation, RecyclerView recyclerView, ZimessReciclerAdapter adapter, int sortZimess) {
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
    public RefreshDataZimessTask(Context context, ParseUser parseUser, Location currentLocation, RecyclerView recyclerView, ZimessReciclerAdapter adapter) {
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
    public RefreshDataZimessTask(DetailZimessActivity detailZimessActivity, Zimess zimessDetail) {
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
    protected List<Zimess> doInBackground(Integer... integers) {
        List<Zimess> zimessResult = new ArrayList<Zimess>();
        //Buscar por ubicacion
        if (currentLocation != null && !findForUser && !findUniqueZimess) {
            for (ParseObject parseZimess : DataParseHelper.findZimessLocation(currentLocation, integers[0], integers[1], sortZimess)) {
                zimessResult.add(getZimess(parseZimess));
            }
            //Cant de Zimess en el Drawer
            GlobalApplication.setCantZimess(zimessResult.size());
        }

        //Buscar por Usuario
        if (findForUser) {
            for (ParseObject parseZimess : DataParseHelper.findZimess(parseUser)) {
                zimessResult.add(getZimess(parseZimess));
            }
        }

        //Buscar unico Zimess
        if (findUniqueZimess) {
            ParseObject parseZimess = DataParseHelper.findZimess(zimessDetail.getZimessId());
            if (parseZimess != null)
                zimessResult.add(getZimess(parseZimess));
        }

        return zimessResult;
    }

    @Override
    protected void onPostExecute(List<Zimess> zimessList) {
        if (!findUniqueZimess) {
            if (ZimessFragment.isRunning() && !findForUser) {
                ZimessFragment zimessFragment = ZimessFragment.getInstance();
                zimessFragment.zimessList = (ArrayList<Zimess>) zimessList;
            }
            if (findForUser) {
                MyZimessActivity activity = (MyZimessActivity) context;
                activity.zimessList = (ArrayList<Zimess>) zimessList;
            }
            zReciclerAdapter = new ZimessReciclerAdapter(zimessList, currentLocation);
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
