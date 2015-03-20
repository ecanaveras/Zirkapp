package com.ecp.gsy.dcs.zirkapp.app.fragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.locations.Location;
import com.ecp.gsy.dcs.zirkapp.app.util.messages.MessagingActivity;
import com.ecp.gsy.dcs.zirkapp.app.util.services.ManagerGPS;
import com.ecp.gsy.dcs.zirkapp.app.util.task.GlobalApplication;
import com.ecp.gsy.dcs.zirkapp.app.util.task.RefreshDataUsersOnline;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by elcapi05 on 13/08/2014.
 */
public class UsersOnlineFragment extends Fragment {


    private ListView listViewUserOnline;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout layoudUsersNoFound, layoudUsersFinder;
    private ProgressDialog progressDialog;
    private BroadcastReceiver broadcastReceiver = null;
    private Switch switchConected;
    private ManagerGPS managerGPS;
    private ParseUser currentUser;
    private boolean isConnectedUser;
    private GlobalApplication globalApplication;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users_online, container, false);
        setHasOptionsMenu(true);
        //showSpinner();

        currentUser = ParseUser.getCurrentUser();

        if (currentUser != null)
            isConnectedUser = currentUser.getBoolean("online");

        inicializarCompUI(view);

        if (isConnectedUser)
            conectarChat();

        return view;
    }

    private void inicializarCompUI(View view) {
        //Layout
        layoudUsersNoFound = (LinearLayout) view.findViewById(R.id.layoudUsersNoFound);
        layoudUsersFinder = (LinearLayout) view.findViewById(R.id.layoudUsersFinder);
        //ListView
        listViewUserOnline = (ListView) view.findViewById(R.id.usersListView);
        listViewUserOnline.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ParseUser parseUser = (ParseUser) adapterView.getAdapter().getItem(i);
                abrirConversa(parseUser);
            }
        });

        //Swipe
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.user_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                buscarUsuariosOnline();
            }
        });

        listViewUserOnline.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition = (listViewUserOnline == null || listViewUserOnline.getChildCount() == 0) ? 0 : listViewUserOnline.getChildAt(0).getTop();
                swipeRefreshLayout.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
            }
        });
    }

    public void buscarUsuariosOnline() {
        if (isConnectedUser) {
            managerGPS = new ManagerGPS(getActivity());
            if (managerGPS.isOnline()) {
                if (managerGPS.isEnableGetLocation()) {
                    Location currentLocation = new Location(managerGPS.getLatitud(), managerGPS.getLongitud());
                    new RefreshDataUsersOnline(getActivity(), currentUser, currentLocation, listViewUserOnline, swipeRefreshLayout, layoudUsersNoFound, layoudUsersFinder).execute(5);
                } else {
                    managerGPS.gpsShowSettingsAlert();
                }
            } else {
                managerGPS.networkShowSettingsAlert();
            }
        } else {
            Toast.makeText(getActivity(), "No estas conectado...", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Conecta el usuario al chat
     */
    private void conectarChat() {
        managerGPS = new ManagerGPS(getActivity());
        if (managerGPS.isOnline()) {
            if (managerGPS.isEnableGetLocation()) {
                if (currentUser != null) {
                    ParseGeoPoint parseGeoPoint = new ParseGeoPoint(managerGPS.getLatitud(), managerGPS.getLongitud());
                    ParseUser parseUser = currentUser;
                    parseUser.put("location", parseGeoPoint);
                    parseUser.put("online", true);
                    parseUser.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                swipeRefreshLayout.setEnabled(true);
                                isConnectedUser = true;
                                buscarUsuariosOnline();
                            }
                        }
                    });
                }
            } else {
                managerGPS.gpsShowSettingsAlert();
            }
        } else {
            managerGPS.networkShowSettingsAlert();
        }

    }

    /**
     * Desconectado al usuario del chat
     */
    private void desconectarChat() {
        if (currentUser != null) {
            ParseUser parseUser = currentUser;
            parseUser.put("online", false);
            parseUser.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    isConnectedUser = false;
                    swipeRefreshLayout.setEnabled(false);
                }
            });
        }

    }

    private void abrirConversa(ParseUser parseUserDestino) {
        globalApplication = (GlobalApplication) getActivity().getApplicationContext();
        globalApplication.setCustomParseUser(parseUserDestino);
        Intent intent = new Intent(getActivity().getApplicationContext(), MessagingActivity.class);
//        intent.putExtra("RECIPIENT_ID", parseUserDestino.getObjectId());
//        intent.putExtra("RECIPIENT_USERNAME", parseUserDestino.getUsername());
//        intent.putExtra("RECIPIENT_NAME", parseUserDestino.getString("name"));
        startActivity(intent);
    }

    private void showSpinner() {
        progressDialog = new ProgressDialog(this.getActivity());
        //Todo usar @String
        progressDialog.setTitle("Cargando");
        progressDialog.setMessage("Espera...");
        progressDialog.show();

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Boolean success = intent.getBooleanExtra("success", false);
                progressDialog.dismiss();
                if (!success) {
                    Toast.makeText(getActivity().getApplicationContext(), "Messaging service failed to start", Toast.LENGTH_LONG).show();
                }
            }
        };

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, new IntentFilter("com.ecp.gsy.dcs.zirkapp.MainActivity"));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_users_online_fragment, menu);
//        switchConected = (Switch) menu.findItem(R.id.switchUsersOnline).getActionView().findViewById(R.id.switchForActionBar);
//        switchConected.setChecked(isConnectedUser);
//        switchConected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
//                if (checked) {
//                    conectarChat();
//                    buscarUsuariosOnline();
//                } else {
//                    desconectarChat();
//                }
//            }
//        });
        super.onCreateOptionsMenu(menu, inflater);
    }


}
