package com.ecp.gsy.dcs.zirkapp.app.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.ecp.gsy.dcs.zirkapp.app.DetailZimessActivity;
import com.ecp.gsy.dcs.zirkapp.app.NewZimessActivityParse;
import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.adapters.ZimessAdapter;
import com.ecp.gsy.dcs.zirkapp.app.util.beans.Zimess;
import com.ecp.gsy.dcs.zirkapp.app.util.locations.Location;
import com.ecp.gsy.dcs.zirkapp.app.util.locations.ManagerGPS;
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
    private ListView listViewZimess;
    private Menu menuList;
    private ManagerGPS managerGPS;
    private LinearLayout layoudZimessNoFound;
    private GlobalApplication globalApplication;
    private LinearLayout layoudZimessFinder;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_zimess, container, false);
        inicializarCompUI(view);
        setHasOptionsMenu(true);

        managerGPS = new ManagerGPS(getActivity().getApplicationContext());
        globalApplication = (GlobalApplication) getActivity().getApplicationContext();

        return view;
    }

    @Override
    public void onResume() {
        //Usuario actual
        if (globalApplication.getCurrentUser() != null) {
            currenUserId = globalApplication.getCurrentUser().getObjectId();
            findZimessAround();
        }
        super.onResume();
    }

    private void inicializarCompUI(View view) {
        layoudZimessNoFound = (LinearLayout) view.findViewById(R.id.layoudZimessNoFound);
        layoudZimessFinder = (LinearLayout) view.findViewById(R.id.layoudZimessFinder);
        listViewZimess = (ListView) view.findViewById(R.id.listZMessages);
        listViewZimess.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Zimess zimess = (Zimess) adapterView.getAdapter().getItem(i);
                gotoDetail(zimess);
            }
        });

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
    }

    private void findZimessAround() {
        managerGPS.obtenertUbicacion();
        Location currentLocation = new Location(managerGPS.getLatitud(), managerGPS.getLongitud());
        new RefreshDataZimessTask(this.getActivity(), currentLocation, listViewZimess, layoudZimessNoFound, layoudZimessFinder, swipeRefreshLayout).execute(5); //Todo parametrizar KMs
    }

    /**
     * Vamos al detalle del Zimess
     *
     * @param zimess
     */
    private void gotoDetail(Zimess zimess) {
        globalApplication.setTempZimess(zimess);
        String userNameZimess = zimess.getUser().getUsername();
        Intent intent = new Intent(getActivity(), DetailZimessActivity.class);
        intent.putExtra("usernameZimess", userNameZimess);
        getActivity().startActivity(intent);
        //Animar
        //getActivity().overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
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
                startActivity(intent);
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
}
