package com.ecp.gsy.dcs.zirkapp.app.fragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SwitchCompat;
import android.view.ContextMenu;
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
import android.widget.TextView;
import android.widget.Toast;

import com.alertdialogpro.AlertDialogPro;
import com.ecp.gsy.dcs.zirkapp.app.activities.ChatHistoryActivity;
import com.ecp.gsy.dcs.zirkapp.app.activities.MessagingActivity;
import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.broadcast.CountMessagesReceiver;
import com.ecp.gsy.dcs.zirkapp.app.util.broadcast.SinchConnectReceiver;
import com.ecp.gsy.dcs.zirkapp.app.util.locations.Location;
import com.ecp.gsy.dcs.zirkapp.app.util.services.LocationService;
import com.ecp.gsy.dcs.zirkapp.app.GlobalApplication;
import com.ecp.gsy.dcs.zirkapp.app.util.task.RefreshDataUsersTask;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.Arrays;
import java.util.List;

/**
 * Created by elcapi05 on 13/08/2014.
 */
public class UsersFragment extends Fragment {

    private static UsersFragment instance = null;

    private ListView listViewUserOnline;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout layoutUsersNoFound, layoutUsersFinder, layoutChatOffline, layoutInitService, layoutGpsOff;

    private ParseUser currentUser;

    private GlobalApplication globalApplication;
    private Menu menu;

    private CountMessagesReceiver countMessagesReceiver;
    private SinchConnectReceiver sinchConnectReceiver;

    public boolean isConnectedUser;
    private TextView lblInfoChat;
    private AlertDialogPro sortDialog;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users_online, container, false);
        setHasOptionsMenu(true);

        globalApplication = (GlobalApplication) getActivity().getApplicationContext();

        currentUser = ParseUser.getCurrentUser();

        if (currentUser != null)
            isConnectedUser = currentUser.getBoolean("online");

        inicializarCompUI(view);

        instance = this;

        return view;
    }

    public static boolean isRunning() {
        return instance != null;
    }

    public static UsersFragment getInstance() {
        return instance;
    }

    private void inicializarCompUI(View view) {
        //Layout
        layoutUsersNoFound = (LinearLayout) view.findViewById(R.id.layoutUsersNoFound);
        layoutUsersFinder = (LinearLayout) view.findViewById(R.id.layoutUsersFinder);
        layoutChatOffline = (LinearLayout) view.findViewById(R.id.layoutChatOffline);
        layoutGpsOff = (LinearLayout) view.findViewById(R.id.layoutGpsOff);
        layoutInitService = (LinearLayout) view.findViewById(R.id.layoutInitService);

        lblInfoChat = (TextView) view.findViewById(R.id.lblInfoChat);

        //ListView
        listViewUserOnline = (ListView) view.findViewById(R.id.usersListView);
        registerForContextMenu(listViewUserOnline);
        listViewUserOnline.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ParseUser parseUser = (ParseUser) adapterView.getAdapter().getItem(i);
                abrirConversa(parseUser);
            }
        });

//        listViewUserOnline.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
//                ParseUser parseUser = (ParseUser) adapterView.getAdapter().getItem(position);
//                showOptionsDialog(parseUser.getObjectId());
//                return true;
//            }
//        });

        //Swipe
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.user_refresh_layout);
        swipeRefreshLayout.setColorScheme(R.color.primary_text_color, R.color.default_primary_color, R.color.primary_text_color, R.color.default_primary_color);

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

        if (globalApplication.isConectedToInternet()) {
            if (globalApplication.isEnabledGetLocation()) {
                if (isConnectedUser)
                    layoutUsersFinder.setVisibility(View.VISIBLE);
            } else {
                layoutGpsOff.setVisibility(View.VISIBLE);
            }
        } else {
            layoutChatOffline.setVisibility(View.VISIBLE);
            lblInfoChat.setText(getResources().getString(R.string.msgInternetOff));
        }
    }

    /**
     * Actualiza la ubicacion del usuario actual y busca los usuarios en Linea y que esten en cerca
     */
    public void findUsersOnline(Location currentLocation) {
        if (isConnectedUser && currentLocation != null) {
            layoutChatOffline.setVisibility(View.GONE);
            layoutGpsOff.setVisibility(View.GONE);
            RefreshDataUsersTask refresDataTask = new RefreshDataUsersTask(getActivity(), currentUser, currentLocation, listViewUserOnline);
            refresDataTask.setSwipeRefreshLayout(swipeRefreshLayout);
            refresDataTask.setLayoutUsersFinder(layoutUsersFinder);
            refresDataTask.setLayoutUsersNoFound(layoutUsersNoFound);
            refresDataTask.execute(5);
        } else {
            //1. Layouts Invisibles
            layoutUsersNoFound.setVisibility(View.GONE);
            layoutUsersFinder.setVisibility(View.GONE);
            layoutGpsOff.setVisibility(View.GONE);
            layoutChatOffline.setVisibility(View.GONE);
            //2. Mostrar Layout correspondiente
            if (globalApplication.isConectedToInternet()) {
                if (globalApplication.isEnabledGetLocation()) {
                    if (!isConnectedUser) {
                        layoutChatOffline.setVisibility(View.VISIBLE);
                        lblInfoChat.setText("Chat Offline");
                    }
                } else {
                    layoutGpsOff.setVisibility(View.VISIBLE);
                }
            } else {
                layoutChatOffline.setVisibility(View.VISIBLE);
                lblInfoChat.setText(getResources().getString(R.string.msgInternetOff));
            }
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    /**
     * Conecta el usuario al chat
     */
    public void conectarChat(Location currentLocation) {
        if (currentUser != null) {
            isConnectedUser = true;
            layoutChatOffline.setVisibility(View.GONE);
            findUsersOnline(currentLocation);
        } else {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    /**
     * Desconectado al usuario del chat
     */
    private void desconectarChat() {
        if (currentUser == null) {
            return;
        }
        if (globalApplication.isConectedToInternet()) {
            ParseUser parseUser = currentUser;
            parseUser.put("online", false);
            parseUser.saveInBackground();
            isConnectedUser = false;
            listViewUserOnline.setAdapter(null);
            layoutChatOffline.setVisibility(View.VISIBLE);
            lblInfoChat.setText("Chat Offline");
            layoutUsersNoFound.setVisibility(View.GONE);
        } else {
            Toast.makeText(getActivity(), getResources().getString(R.string.msgInternetOff), Toast.LENGTH_SHORT).show();
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
     * Busca y elimina la conversación del usuario selecionado
     *
     * @param userChat
     */
    private void deleteLocalMessageHistory(final ParseUser userChat) {
        String formatMessage = "%s \"%s\"...";
        String nameUser = userChat.getString("name") != null ? userChat.getString("name") : userChat.getUsername();
        AlertDialogPro.Builder alert = new AlertDialogPro.Builder(getActivity());
        alert.setMessage(String.format(formatMessage, getResources().getString(R.string.msgByeChat2), nameUser));
        alert.setPositiveButton(getString(R.string.lblDelete), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Delete
                final ProgressDialog dialog = new ProgressDialog(getActivity());
                dialog.setMessage(getResources().getString(R.string.msgDeleting));
                dialog.show();
                String[] userIds = {currentUser.getObjectId(), userChat.getObjectId()};
                ParseQuery<ParseObject> query = ParseQuery.getQuery("ParseMessage");
                query.whereContainedIn("senderId", Arrays.asList(userIds));
                query.whereContainedIn("recipientId", Arrays.asList(userIds));
                query.fromLocalDatastore();
                query.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> parseObjects, ParseException e) {
                        if (e == null) {
                            ParseObject.unpinAllInBackground(parseObjects);
                        }
                        dialog.dismiss();
                    }
                });
            }
        });

        alert.setNegativeButton(getString(R.string.lblCancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        alert.show();

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
            if (locationService != null) {
                android.location.Location tmpLocation = locationService.getCurrentLocation(true);
                if (tmpLocation != null)
                    location = new Location(tmpLocation.getLatitude(), tmpLocation.getLongitude());
            }
        }
        return location;
    }

    /*
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
    */

    @Override
    public void onResume() {
        //Comprobar el estado del servicio Sinch
        sinchConnectReceiver = new SinchConnectReceiver(layoutInitService, listViewUserOnline);
        //Contar los mensajes recibidos
        countMessagesReceiver = new CountMessagesReceiver(listViewUserOnline);

        //Registrar los Broadcast
        getActivity().registerReceiver(countMessagesReceiver, new IntentFilter("broadcast.cant_messages"));
        getActivity().registerReceiver(sinchConnectReceiver, new IntentFilter("app.fragments.UsersFragment"));
        super.onResume();
    }

    @Override
    public void onPause() {
        getActivity().unregisterReceiver(countMessagesReceiver);
        getActivity().unregisterReceiver(sinchConnectReceiver);
        super.onPause();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_users_fragment, menu);
        this.menu = menu;
        MenuItem item = menu.findItem(R.id.switchUsersOnline);
        item.setActionView(R.layout.component_switch);
        final SwitchCompat switchConected = (SwitchCompat) item.getActionView().findViewById(R.id.switch_on_off);
        switchConected.setChecked(isConnectedUser);
        switchConected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    swipeRefreshLayout.setEnabled(true);
                    conectarChat(getCurrentLocation());
                } else {
                    swipeRefreshLayout.setEnabled(false);
                    desconectarChat();
                }
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Manejar seleccion en el menú
        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_bar_history:
                intent = new Intent(getActivity(), ChatHistoryActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.usersListView) {
            AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) menuInfo;
        }
        getActivity().getMenuInflater().inflate(R.menu.menu_contextual_users, menu);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ctx_delete_chat:
                AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                ParseUser parseUser = (ParseUser) listViewUserOnline.getAdapter().getItem(acmi.position);
                if (parseUser != null)
                    deleteLocalMessageHistory(parseUser);
                return true;
            case R.id.ctx_lock_user:
                Toast.makeText(getActivity(), "Proximamente...", Toast.LENGTH_SHORT).show();
            default:
                return super.onContextItemSelected(item);
        }
    }
}

