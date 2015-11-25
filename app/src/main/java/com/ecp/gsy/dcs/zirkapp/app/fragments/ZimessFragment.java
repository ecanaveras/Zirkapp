package com.ecp.gsy.dcs.zirkapp.app.fragments;

import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ecp.gsy.dcs.zirkapp.app.GlobalApplication;
import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.activities.MyZimessActivity;
import com.ecp.gsy.dcs.zirkapp.app.activities.NewZimessActivity;
import com.ecp.gsy.dcs.zirkapp.app.util.adapters.ZimessRecyclerAdapter;
import com.ecp.gsy.dcs.zirkapp.app.util.locations.Location;
import com.ecp.gsy.dcs.zirkapp.app.util.services.LocationService;
import com.ecp.gsy.dcs.zirkapp.app.util.task.RefreshDataZimessTask;
import com.parse.ParseUser;

/**
 * Created by Elder on 23/02/2015.
 */
public class ZimessFragment extends Fragment {

    private static ZimessFragment instance = null;

    private SwipeRefreshLayout swipeRefreshLayout;

    private RecyclerView recyclerView;
    private ZimessRecyclerAdapter zReciclerAdapter;

    private Menu menuList;
    private LinearLayout layoutZimessNoFound, layoutInternetOff, layoutZimessFinder, layoutGpsOff, layoutZimessDefault;
    private GlobalApplication globalApplication;

    private AlertDialog sortDialog;
    private TextView lblRangoZimess;
    private ImageView avatar;
    private SharedPreferences preferences;
    private ParseUser currentUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_zimess, container, false);
        setHasOptionsMenu(true);

        globalApplication = (GlobalApplication) getActivity().getApplicationContext();
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        inicializarCompUI(view);

        //Actualiza la lista de Zimess por primera vez
        callLocation();

        instance = this;

        return view;
    }

    public static boolean isRunning() {
        return instance != null;
    }

    public static ZimessFragment getInstance() {
        return instance;
    }

    private void callLocation() {
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                try {
                    Thread.sleep(2000); // 2 segundos
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                findZimessAround(getCurrentLocation(), globalApplication.getSortZimess());
            }
        }.execute();
    }

    private void inicializarCompUI(View view) {
        //UI Zimess
        layoutZimessDefault = (LinearLayout) view.findViewById(R.id.layoutZimessDefault);
        layoutZimessNoFound = (LinearLayout) view.findViewById(R.id.layoutZimessNoFound);
        layoutZimessFinder = (LinearLayout) view.findViewById(R.id.layoutZimessFinder);
        layoutInternetOff = (LinearLayout) view.findViewById(R.id.layoutInternetOff);
        layoutGpsOff = (LinearLayout) view.findViewById(R.id.layoutGpsOff);

        ImageView imageView = (ImageView) view.findViewById(R.id.imgLogoZirkapp);
        Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.fade);
        imageView.startAnimation(animation);

        lblRangoZimess = (TextView) view.findViewById(R.id.lblInfoRango);

        recyclerView = (RecyclerView) view.findViewById(R.id.listZMessages);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        //recyclerView.setHasFixedSize(true);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.zimess_refresh_layout);
        swipeRefreshLayout.setColorScheme(R.color.primary_text_color, R.color.default_primary_color, R.color.primary_text_color, R.color.default_primary_color);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                findZimessAround(getCurrentLocation(), globalApplication.getSortZimess());
            }
        });

        //Corrige bug de Swipe... [Permite el scroll sin problemas]
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int firstVisibleItem, int dy) {
                int topRow = (recyclerView == null || recyclerView.getChildCount() == 0) ? 0 : recyclerView.getChildAt(0).getTop();
                swipeRefreshLayout.setEnabled(firstVisibleItem == 0 && topRow >= 0);
            }
        });

        if (globalApplication.isConectedToInternet()) {
            if (globalApplication.isEnabledGetLocation()) {
                layoutZimessDefault.setVisibility(View.VISIBLE);
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
            layoutZimessDefault.setVisibility(View.GONE);
            //Tomar valores de las preferencias de usuarios
            int dist_min = Integer.parseInt(preferences.getString("min_dist_list", "-1"));
            int dist_max = Integer.parseInt(preferences.getString("max_dist_list", "10"));

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
            layoutZimessDefault.setVisibility(View.GONE);
            //2. Mostrar Layout correspondiente
            if (globalApplication.isConectedToInternet()) {
                if (globalApplication.isEnabledGetLocation()) {
                    layoutZimessDefault.setVisibility(View.VISIBLE);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle);
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
        if (globalApplication.isConectedToInternet()) {
            if (globalApplication.isEnabledGetLocation()) {
                if (LocationService.isRunning()) {
                    layoutInternetOff.setVisibility(View.GONE);
                    LocationService locationService = LocationService.getInstance();
                    if (locationService != null) {
                        android.location.Location tmpLocation = locationService.getCurrentLocation();
                        if (tmpLocation != null) {
                            location = new Location(tmpLocation.getLatitude(), tmpLocation.getLongitude());
                            Log.d("Zimess, Latitude", tmpLocation.getLatitude() + "");
                            Log.d("Zimess, Longitude", tmpLocation.getLongitude() + "");
                        }
                    }
                } else {
                    Log.i("ZimessGetLocation", LocationService.class.getSimpleName() + " not running");
                }
            } else {
                layoutGpsOff.setVisibility(View.VISIBLE);
                globalApplication.gpsShowSettingsAlert();
            }
        } else {
            layoutInternetOff.setVisibility(View.VISIBLE);
            globalApplication.networkShowSettingsAlert();
        }
        return location;
    }

    private String getHomoMinDistance(int minDinstance) {
        String minD = "1";
        switch (minDinstance) {
            case 1:
                minD = "1000";
                break;
            case 3:
                minD = "3000";
                break;
        }
        return minD;
    }

    private String getHomoMaxDistance(int maxDinstance) {
        String maxD = "10000";
        switch (maxDinstance) {
            case 5:
                maxD = "5000";
                break;
            case 8:
                maxD = "8000";
                break;
        }
        return maxD;
    }

    @Override
    public void onResume() {
        super.onResume();
//        if (zReciclerAdapter != null && zReciclerAdapter.getItemCount() == 0) {
//            findZimessAround(getCurrentLocation(false), globalApplication.getSortZimess());
//        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_zimess_fragment, menu);
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
