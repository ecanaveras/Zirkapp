package com.ecp.gsy.dcs.zirkapp.app.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alertdialogpro.AlertDialogPro;
import com.ecp.gsy.dcs.zirkapp.app.activities.DetailZimessActivity;
import com.ecp.gsy.dcs.zirkapp.app.activities.MyZimessActivity;
import com.ecp.gsy.dcs.zirkapp.app.activities.NewZimessActivity;
import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.beans.Zimess;
import com.ecp.gsy.dcs.zirkapp.app.util.locations.Location;
import com.ecp.gsy.dcs.zirkapp.app.util.services.LocationService;
import com.ecp.gsy.dcs.zirkapp.app.GlobalApplication;
import com.ecp.gsy.dcs.zirkapp.app.util.task.RefreshDataZimessTask;

/**
 * Created by Elder on 23/02/2015.
 */
public class ZimessFragment extends Fragment {

    private static ZimessFragment instance = null;

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private Menu menuList;
    private LinearLayout layoudZimessNoFound;
    private GlobalApplication globalApplication;
    private LinearLayout layoudZimessFinder;
    private int requestCodeNewZimess = 100;
    public int requestCodeUpdateZimess = 105;
    private AlertDialogPro sortDialog;
    private TextView lblRangoZimess;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_zimess, container, false);
        setHasOptionsMenu(true);

        globalApplication = (GlobalApplication) getActivity().getApplicationContext();

        inicializarCompUI(view);

        instance = this;

        return view;
    }

    public static boolean isRunning() {
        return instance != null;
    }

    public static ZimessFragment getInstance() {
        return instance;
    }


    private void inicializarCompUI(View view) {
        //UI Zimess no Found
        layoudZimessNoFound = (LinearLayout) view.findViewById(R.id.layoudZimessNoFound);
        layoudZimessFinder = (LinearLayout) view.findViewById(R.id.layoudZimessFinder);

        lblRangoZimess = (TextView) view.findViewById(R.id.lblInfoRango);

        recyclerView = (RecyclerView) view.findViewById(R.id.listZMessages);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.zimess_refresh_layout);
        swipeRefreshLayout.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                findZimessAround(getCurrentLocation(), globalApplication.getSortZimess());
            }
        });

        //Corregir bug de Swipe... [Permite el scroll sin problemas]
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int firstVisibleItem, int dy) {
                int topRow = (recyclerView == null || recyclerView.getChildCount() == 0) ? 0 : recyclerView.getChildAt(0).getTop();
                swipeRefreshLayout.setEnabled(firstVisibleItem == 0 && topRow >= 0);
            }
        });
    }

    /**
     * Busca los Zimess Cercanos
     */
    public void findZimessAround(Location currentLocation, Integer sortZimess) {
        if (currentLocation != null && sortZimess != null) {
            //Tomar valores de las preferencias de usuarios
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            int dist_min = Integer.parseInt(preferences.getString("min_dist_list", "-1"));
            int dist_max = Integer.parseInt(preferences.getString("max_dist_list", "5"));
            new RefreshDataZimessTask(this, currentLocation, recyclerView, layoudZimessNoFound, layoudZimessFinder, swipeRefreshLayout, sortZimess).execute(dist_min, dist_max);
            lblRangoZimess.setText("Rango actual de Zimess: " + getHomoMinDistance(dist_min) + " a " + getHomoMaxDistance(dist_max)+" metros");
        }
    }


    private void showSortDialog() {
        sortDialog = null;
        final CharSequence[] optionsSort = {"Mas Recientes", "Mas Cerca"};//, "Mas Lejos" //Todo usar como recurso
        AlertDialogPro.Builder builder = new AlertDialogPro.Builder(getActivity());
        builder.setTitle("Ordernar...");
        builder.setSingleChoiceItems(optionsSort, globalApplication.getSortZimess(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getActivity(), optionsSort[which], Toast.LENGTH_SHORT).show();
                switch (which) {
                    case 0:
                        findZimessAround(getCurrentLocation(), RefreshDataZimessTask.RECIENTE);
                        globalApplication.setSortZimess(RefreshDataZimessTask.RECIENTE);
                        break;
                    case 1:
                        findZimessAround(getCurrentLocation(), RefreshDataZimessTask.CERCA);
                        globalApplication.setSortZimess(RefreshDataZimessTask.CERCA);
                        break;
                }
                sortDialog.dismiss();
            }
        });
        sortDialog = builder.create();
        sortDialog.show();
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

    /**
     * retorna la Ubicacion actual
     *
     * @return
     */
    private Location getCurrentLocation() {
        Location location = null;
        if (LocationService.isRunning()) {
            LocationService locationService = LocationService.getInstance();
            android.location.Location tmpLocation = locationService.getCurrentLocation(true);
            location = new Location(tmpLocation.getLatitude(), tmpLocation.getLongitude());
        }
        return location;
    }

    private String getHomoMinDistance(int minDinstance) {
        String minD = "1";
        switch (minDinstance) {
            case 0:
                minD = "500";
                break;
            case 1:
                minD = "1000";
                break;
        }
        return minD;
    }

    private String getHomoMaxDistance(int maxDinstance) {
        String maxD = "2000";
        switch (maxDinstance) {
            case 4:
                maxD = "4000";
                break;
            case 5:
                maxD = "5000";
                break;
        }
        return maxD;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_fragment_zimess, menu);
        menuList = menu;
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Manejar seleccion en el menú
        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_bar_new_zimess:
                intent = new Intent(getActivity(), NewZimessActivity.class);
                startActivityForResult(intent, requestCodeNewZimess);
                break;
            case R.id.action_bar_my_zimess:
                intent = new Intent(getActivity(), MyZimessActivity.class);
                startActivity(intent);
                break;
            case R.id.action_bar_sort_zimess:
                showSortDialog();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

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
                findZimessAround(getCurrentLocation(), globalApplication.getSortZimess());
        }

        if (requestCode == requestCodeUpdateZimess && data != null) {
            boolean updateZimessOk = data.getBooleanExtra("updateZimessOk", false);
            if (resultCode == Activity.RESULT_OK && updateZimessOk)
                findZimessAround(getCurrentLocation(), globalApplication.getSortZimess());
        }
    }
}
