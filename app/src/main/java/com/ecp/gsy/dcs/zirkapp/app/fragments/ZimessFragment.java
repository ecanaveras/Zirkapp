package com.ecp.gsy.dcs.zirkapp.app.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.ecp.gsy.dcs.zirkapp.app.DetailZimessActivity;
import com.ecp.gsy.dcs.zirkapp.app.NewZimessActivityParse;
import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.adapters.AdapterZimess;
import com.ecp.gsy.dcs.zirkapp.app.util.beans.ZimessNew;
import com.ecp.gsy.dcs.zirkapp.app.util.locations.ManagerGPS;
import com.ecp.gsy.dcs.zirkapp.app.util.task.GlobalApplication;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Elder on 23/02/2015.
 */
public class ZimessFragment extends Fragment {

    private String currenUserId;

    private ArrayList<ZimessNew> zimessNewArrayList;
    private AdapterZimess adapterZimessNew;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView listViewZimess;
    private Menu menuList;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_zimess, container, false);
        inicializarCompUI(view);
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onResume() {
        //Usuario actual
        final GlobalApplication globalApplication = (GlobalApplication) getActivity().getApplicationContext();
        if (globalApplication.getCurrentUser() != null) {
            currenUserId = globalApplication.getCurrentUser().getObjectId();
            findZimessAround();
        }
        super.onResume();
    }

    private void inicializarCompUI(View view) {
        listViewZimess = (ListView) view.findViewById(R.id.listZMessages);
        listViewZimess.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ZimessNew zimess = (ZimessNew) adapterView.getAdapter().getItem(i);
                gotoDetail(zimess);
            }
        });

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.zimess_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                findZimessAround();
            }
        });
    }

    private void findZimessAround() {
        zimessNewArrayList = new ArrayList<ZimessNew>();
        //Tomar ubicacion
        ManagerGPS managerGPS = new ManagerGPS(getActivity().getApplicationContext());
        //Buscar Zimess
        ParseGeoPoint parseGeoPoint = new ParseGeoPoint(managerGPS.getLatitud(), managerGPS.getLongitud());
        ParseQuery<ParseObject> query = ParseQuery.getQuery("ParseZimess");
        query.whereWithinKilometers("location", parseGeoPoint, 5); //Todo parametrizar la cantidad de Km.
        query.orderByDescending("createdAt");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {
                    for (ParseObject zimess : parseObjects) {
                        //Log.d("zimessText", zimess.get("zimessText").toString());
                        ZimessNew zimessNew = new ZimessNew();
                        zimessNew.setZimessId(zimess.getObjectId());
                        zimessNew.setUserId(zimess.get("userId").toString());
                        zimessNew.setZimessText(zimess.get("zimessText").toString());
                        zimessNew.setLocation(zimess.getParseGeoPoint("location"));
                        zimessNew.setCreateAt(zimess.getCreatedAt());
                        zimessNewArrayList.add(zimessNew);
                    }
                    adapterZimessNew = new AdapterZimess(getActivity(), zimessNewArrayList);
                    listViewZimess.setAdapter(adapterZimessNew);

                    //Update Cant Zimess cerca
                    Intent intent = new Intent("actualizarcantnotifi");
                    intent.putExtra("datos", adapterZimessNew.getCount());
                    getActivity().sendBroadcast(intent);
                } else {
                    Log.e("Parse.Zimess", "Zimess Not Found");
                }
            }
        });

        swipeRefreshLayout.setRefreshing(false);
    }

    /**
     * Vamos al detalle del Zimess
     *
     * @param zimess
     */
    private void gotoDetail(ZimessNew zimess) {
        final GlobalApplication globalApplication = (GlobalApplication) getActivity().getApplication();
        globalApplication.setTempZimess(zimess);
        ParseUser parseUser = globalApplication.getCustomParseUser(zimess.getUserId());
        String userNameZimess = parseUser != null ? parseUser.getUsername() : null;
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
