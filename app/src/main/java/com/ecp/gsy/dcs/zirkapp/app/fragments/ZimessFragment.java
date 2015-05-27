package com.ecp.gsy.dcs.zirkapp.app.fragments;

import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alertdialogpro.AlertDialogPro;
import com.ecp.gsy.dcs.zirkapp.app.activities.DetailZimessActivity;
import com.ecp.gsy.dcs.zirkapp.app.activities.MyZimessActivity;
import com.ecp.gsy.dcs.zirkapp.app.activities.NewZimessActivity;
import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.activities.UserProfileActivity;
import com.ecp.gsy.dcs.zirkapp.app.util.adapters.ZimessReciclerAdapter;
import com.ecp.gsy.dcs.zirkapp.app.util.beans.Zimess;
import com.ecp.gsy.dcs.zirkapp.app.util.listener.RecyclerItemListener;
import com.ecp.gsy.dcs.zirkapp.app.util.locations.Location;
import com.ecp.gsy.dcs.zirkapp.app.util.services.LocationService;
import com.ecp.gsy.dcs.zirkapp.app.GlobalApplication;
import com.ecp.gsy.dcs.zirkapp.app.util.task.RefreshDataZimessTask;

import java.util.ArrayList;

/**
 * Created by Elder on 23/02/2015.
 */
public class ZimessFragment extends Fragment {

    private static ZimessFragment instance = null;

    private SwipeRefreshLayout swipeRefreshLayout;

    private RecyclerView recyclerView;
    private ZimessReciclerAdapter zReciclerAdapter;
    public ArrayList<Zimess> zimessList = new ArrayList<Zimess>();

    private Menu menuList;
    private LinearLayout layoutZimessNoFound, layoutInternetOff, layoutZimessFinder, layoutGpsOff;
    private GlobalApplication globalApplication;

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
        layoutZimessNoFound = (LinearLayout) view.findViewById(R.id.layoutZimessNoFound);
        layoutZimessFinder = (LinearLayout) view.findViewById(R.id.layoutZimessFinder);
        layoutInternetOff = (LinearLayout) view.findViewById(R.id.layoutInternetOff);
        layoutGpsOff = (LinearLayout) view.findViewById(R.id.layoutGpsOff);

        lblRangoZimess = (TextView) view.findViewById(R.id.lblInfoRango);

        recyclerView = (RecyclerView) view.findViewById(R.id.listZMessages);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);
        recyclerView.addOnItemTouchListener(new RecyclerItemListener(getActivity(), new RecyclerItemListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (zimessList.size() > 0) {
                    final Zimess zimess = zimessList.get(position);

                    final View avatar = view.findViewById(R.id.imgAvatarItem);
                    avatar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //ir la perfil de usuario
                            String transitionName = getResources().getString(R.string.imgNameTransition);
                            ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), avatar, transitionName);
                            Intent intent = new Intent(getActivity(), UserProfileActivity.class);
                            globalApplication.setCustomParseUser(zimess.getUser());
                            ActivityCompat.startActivity(getActivity(), intent, optionsCompat.toBundle());
                            return;
                        }
                    });

                    //Ir al detalle del Zimess
                    globalApplication.setTempZimess(zimess);
                    Intent intent = new Intent(getActivity(), DetailZimessActivity.class);
                    startActivity(intent);

                } else {
                    Log.d("zimessList", "empty");
                }
            }
        }));

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.zimess_refresh_layout);
        swipeRefreshLayout.setColorScheme(R.color.primary_text_color, R.color.default_primary_color, R.color.primary_text_color, R.color.default_primary_color);
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

        if (globalApplication.isConectedToInternet()) {
            if (globalApplication.isEnabledGetLocation()) {
                layoutZimessFinder.setVisibility(View.VISIBLE);
            } else {
                layoutGpsOff.setVisibility(View.VISIBLE);
            }
        } else {
            layoutInternetOff.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Busca los Zimess Cercanos
     */
    public void findZimessAround(Location currentLocation, Integer sortZimess) {
        if (currentLocation != null && sortZimess != null) {
            layoutInternetOff.setVisibility(View.GONE);
            layoutGpsOff.setVisibility(View.GONE);
            //Tomar valores de las preferencias de usuarios
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            int dist_min = Integer.parseInt(preferences.getString("min_dist_list", "-1"));
            int dist_max = Integer.parseInt(preferences.getString("max_dist_list", "5"));

            RefreshDataZimessTask dataZimessTask = new RefreshDataZimessTask(getActivity(), currentLocation, recyclerView, zReciclerAdapter, sortZimess);
            dataZimessTask.setSwipeRefreshLayout(swipeRefreshLayout);
            dataZimessTask.setLayoutZimessNoFound(layoutZimessNoFound);
            dataZimessTask.setLayoutZimessFinder(layoutZimessFinder);
            dataZimessTask.execute(dist_min, dist_max);

            lblRangoZimess.setText("Rango actual de Zimess: " + getHomoMinDistance(dist_min) + " a " + getHomoMaxDistance(dist_max) + " metros");
        } else {
            //1. Layouts Invisibles
            layoutZimessFinder.setVisibility(View.GONE);
            layoutInternetOff.setVisibility(View.GONE);
            layoutZimessNoFound.setVisibility(View.GONE);
            layoutGpsOff.setVisibility(View.GONE);
            //2. Mostrar Layout correspondiente
            if (globalApplication.isConectedToInternet()) {
                if (globalApplication.isEnabledGetLocation()) {
                    layoutZimessNoFound.setVisibility(View.VISIBLE);
                } else {
                    layoutGpsOff.setVisibility(View.VISIBLE);
                }
            } else {
                layoutInternetOff.setVisibility(View.VISIBLE);
            }
            swipeRefreshLayout.setRefreshing(false);
        }
    }


    private void showSortDialog() {
        sortDialog = null;
        final CharSequence[] optionsSort = {getResources().getString(R.string.msgMoreRecents), getResources().getString(R.string.mgsMoreNear)};
        AlertDialogPro.Builder builder = new AlertDialogPro.Builder(getActivity());
        builder.setTitle("Ordenar...");
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
     * retorna la Ubicacion actual
     *
     * @return
     */
    private Location getCurrentLocation() {
        Location location = null;
        if (LocationService.isRunning()) {
            layoutInternetOff.setVisibility(View.GONE);
            LocationService locationService = LocationService.getInstance();
            if (locationService != null && locationService.getCurrentLocation(true) != null) {
                android.location.Location tmpLocation = locationService.getCurrentLocation(true);
                if (tmpLocation != null)
                    location = new Location(tmpLocation.getLatitude(), tmpLocation.getLongitude());
            }
        } else if (!globalApplication.isConectedToInternet()) {
            globalApplication.networkShowSettingsAlert();
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
                startActivity(intent);
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
}
