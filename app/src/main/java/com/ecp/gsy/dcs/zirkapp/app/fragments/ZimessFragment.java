package com.ecp.gsy.dcs.zirkapp.app.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.ecp.gsy.dcs.zirkapp.app.DetailZimessActivity;
import com.ecp.gsy.dcs.zirkapp.app.NewZimessActivityParse;
import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.adapters.ZimessAdapter;
import com.ecp.gsy.dcs.zirkapp.app.util.beans.Zimess;
import com.ecp.gsy.dcs.zirkapp.app.util.locations.Location;
import com.ecp.gsy.dcs.zirkapp.app.util.services.ManagerGPS;
import com.ecp.gsy.dcs.zirkapp.app.util.task.GlobalApplication;
import com.ecp.gsy.dcs.zirkapp.app.util.task.RefreshDataZimessTask;

import java.util.ArrayList;

/**
 * Created by Elder on 23/02/2015.
 */
public class ZimessFragment extends Fragment {

    private String currenUserId;

    private ArrayList<Zimess> zimessArrayList;
    private ZimessAdapter zimessAdapterNew;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private Menu menuList;
    private ManagerGPS managerGPS;
    private LinearLayout layoudZimessNoFound;
    private GlobalApplication globalApplication;
    private LinearLayout layoudZimessFinder;
    private int requestCodeNewZimess = 100;
    public int requestCodeUpdateZimess = 105;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_zimess, container, false);
        inicializarCompUI(view);
        setHasOptionsMenu(true);

        globalApplication = (GlobalApplication) getActivity().getApplicationContext();

        //Buscar Zimess
        findZimessAround();

        return view;
    }

    private void inicializarCompUI(View view) {
        layoudZimessNoFound = (LinearLayout) view.findViewById(R.id.layoudZimessNoFound);
        layoudZimessFinder = (LinearLayout) view.findViewById(R.id.layoudZimessFinder);
        recyclerView = (RecyclerView) view.findViewById(R.id.listZMessages);
        recyclerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Zimess zimess = (Zimess) adapterView.getAdapter().getItem(i);
//                gotoDetail(zimess);
            }
        });

        layoudZimessFinder.setVisibility(View.GONE);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.zimess_refresh_layout);
        swipeRefreshLayout.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                findZimessAround();
            }
        });
/*
        recyclerView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition = (recyclerView == null || recyclerView.getChildCount() == 0) ? 0 : recyclerView.getChildAt(0).getTop();
                swipeRefreshLayout.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
            }
        });*/
    }

    public void findZimessAround() {
        managerGPS = new ManagerGPS(getActivity());
        if (!managerGPS.isOnline()) {//Si no hay internet
            managerGPS.networkShowSettingsAlert();
        } else {
            if (managerGPS.isEnableGetLocation()) {
                Location currentLocation = new Location(managerGPS.getLatitud(), managerGPS.getLongitud());
                new RefreshDataZimessTask(this, currentLocation, recyclerView, layoudZimessNoFound, layoudZimessFinder, swipeRefreshLayout).execute(5); //Todo parametrizar KMs
            } else {
                managerGPS.gpsShowSettingsAlert();
            }
        }
    }

    /**
     * Vamos al detalle del Zimess
     *
     * @param zimess
     */
    private void gotoDetail(Zimess zimess) {
        globalApplication.setTempZimess(zimess);
        Intent intent = new Intent(getActivity(), DetailZimessActivity.class);
        startActivityForResult(intent, requestCodeUpdateZimess);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.list_zimess_activity_action, menu);
        menuList = menu;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Manejar seleccion en el menú
        switch (item.getItemId()) {
            case R.id.action_bar_new_zmess:
                Intent intent = new Intent(getActivity(), NewZimessActivityParse.class);
                startActivityForResult(intent, requestCodeNewZimess);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private MenuItem getMenuItem(int id) {
        if (menuList != null) {
            MenuItem itemf = null;
            for (int i = 0; i < menuList.size(); i++) {
                itemf = menuList.getItem(i);
                if (itemf.getItemId() == id) {
                    return itemf;
                }
            }
        }
        return null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == requestCodeNewZimess && data != null) {
            boolean newZimessOk = data.getBooleanExtra("newZimessOk", false);
            if (resultCode == Activity.RESULT_OK && newZimessOk)
                findZimessAround();
        }

        if (requestCode == requestCodeUpdateZimess && data != null) {
            boolean updateZimessOk = data.getBooleanExtra("updateZimessOk", false);
            if (resultCode == Activity.RESULT_OK && updateZimessOk)
                findZimessAround();
        }
    }
}
