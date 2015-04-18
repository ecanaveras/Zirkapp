package com.ecp.gsy.dcs.zirkapp.app.fragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.ecp.gsy.dcs.zirkapp.app.MessagingActivity;
import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.broadcast.CountMessagesReceiver;
import com.ecp.gsy.dcs.zirkapp.app.util.locations.Location;
import com.ecp.gsy.dcs.zirkapp.app.util.services.ManagerGPS;
import com.ecp.gsy.dcs.zirkapp.app.util.task.GlobalApplication;
import com.ecp.gsy.dcs.zirkapp.app.util.task.RefreshDataUsersOnline;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by elcapi05 on 13/08/2014.
 */
public class UsersOnlineFragment extends Fragment {


    private ListView listViewUserOnline;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout layoudUsersNoFound, layoudUsersFinder;

    private BroadcastReceiver broadcastReceiver = null;

    private ManagerGPS managerGPS;
    private ParseUser currentUser;
    private boolean isConnectedUser;
    private GlobalApplication globalApplication;
    private Menu menu;
    private CountMessagesReceiver receiver;

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
            conectarChat(getCurrentLocation());

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
                if (isConnectedUser)
                    conectarChat(getCurrentLocation());
            }
        });
        swipeRefreshLayout.setEnabled(isConnectedUser);

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


    /**
     * Busca los usuarios en Linea y que esten en cerca
     */
    public void buscarUsuariosOnline() {
        Location currentLocation = getCurrentLocation();
        if (isConnectedUser && currentLocation != null) {
            new RefreshDataUsersOnline(this, currentUser, currentLocation, listViewUserOnline, swipeRefreshLayout, layoudUsersNoFound, layoudUsersFinder).execute(5);
        } else {
            Toast.makeText(getActivity(), "No estas conectado...", Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    /**
     * Conecta el usuario al chat
     */
    private void conectarChat(Location currentLocation) {
        if (currentUser != null && currentLocation != null) {
//            progressDialog = new ProgressDialog(getActivity());
//            progressDialog.setMessage("Conectando...");
//            progressDialog.show();
            ParseGeoPoint parseGeoPoint = new ParseGeoPoint(currentLocation.getLatitud(), currentLocation.getLongitud());
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
//                    progressDialog.dismiss();
                }
            });
        }
    }

    /**
     * Desconectado al usuario del chat
     */
    private void desconectarChat() {
        if (currentUser != null) {
            ParseUser parseUser = currentUser;
            parseUser.put("online", false);
            parseUser.saveInBackground();
            isConnectedUser = false;
            swipeRefreshLayout.setEnabled(false);
            listViewUserOnline.setAdapter(null);
        }

    }

    /**
     * Abre la conversacionde un usuario
     *
     * @param parseUserDestino
     */
    private void abrirConversa(ParseUser parseUserDestino) {
        globalApplication = (GlobalApplication) getActivity().getApplicationContext();
        globalApplication.setCustomParseUser(parseUserDestino);
        Intent intent = new Intent(getActivity().getApplicationContext(), MessagingActivity.class);
        startActivity(intent);
    }


    /**
     * Ovbtiene la Ubicacion actual
     *
     * @return
     */
    private Location getCurrentLocation() {
        managerGPS = new ManagerGPS(getActivity());
        if (managerGPS.isOnline()) {
            if (managerGPS.isEnableGetLocation()) {
                return new Location(managerGPS.getLatitud(), managerGPS.getLongitud());
            } else {
                managerGPS.gpsShowSettingsAlert();
            }
        } else {
            managerGPS.networkShowSettingsAlert();
        }
        return null;
    }


    public void updateCantMessagesNoRead() {
        if (listViewUserOnline.getChildCount() == 0)
            return;

        class UiMessage {
            private String senderId;
            private TextView view;

            public UiMessage(String senderId, TextView view) {
                this.senderId = senderId;
                this.view = view;
            }
        }

        final ArrayList<UiMessage> uiMessages = new ArrayList<>();
        final ArrayList<String> sendersId = new ArrayList<>();
        for (int i = 0; i < listViewUserOnline.getChildCount(); i++) {
            View v = listViewUserOnline.getChildAt(i - listViewUserOnline.getFirstVisiblePosition());
            if (v != null) {
                TextView lblCommentUser = (TextView) v.findViewById(R.id.lblUserName);
                TextView lblCantMessages = (TextView) v.findViewById(R.id.lblCantMessages);
                UiMessage uiMessage = new UiMessage(lblCommentUser.getText().toString(), lblCantMessages);
                uiMessages.add(uiMessage);
                sendersId.add(uiMessage.senderId);
            }
        }

        //Buscar chats.
        ParseQuery<ParseObject> query = ParseQuery.getQuery("ParseMessage");
        query.whereContainedIn("senderId", sendersId);
        query.whereEqualTo("messageRead", false);
        query.fromLocalDatastore();
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e == null) {
                    for (UiMessage uiMess : uiMessages) {
                        int cantMessages = 0;
                        for (ParseObject message : list) {
                            if (message.getString("senderId").equals(uiMess.senderId)) {
                                cantMessages++;
                            }
                        }
                        if (cantMessages > 0)
                            uiMess.view.setText(String.valueOf(cantMessages));
                    }
                }
            }
        });


    }

    @Override
    public void onResume() {
        receiver = new CountMessagesReceiver(listViewUserOnline);
        getActivity().registerReceiver(receiver, new IntentFilter("cant_messages"));
        super.onResume();
    }

    @Override
    public void onPause() {
        getActivity().unregisterReceiver(receiver);
        super.onPause();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_users_online_fragment, menu);
        this.menu = menu;
        MenuItem item = menu.findItem(R.id.switchUsersOnline);
        item.setActionView(R.layout.component_switch);
        SwitchCompat switchConected = (SwitchCompat) item.getActionView().findViewById(R.id.switch_on_off);
        switchConected.setChecked(isConnectedUser);
        switchConected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    conectarChat(getCurrentLocation());
                } else {
                    desconectarChat();
                }
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

}
