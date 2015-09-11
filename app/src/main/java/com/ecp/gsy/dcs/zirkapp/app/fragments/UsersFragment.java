package com.ecp.gsy.dcs.zirkapp.app.fragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SwitchCompat;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alertdialogpro.AlertDialogPro;
import com.ecp.gsy.dcs.zirkapp.app.GlobalApplication;
import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.activities.ChatHistoryActivity;
import com.ecp.gsy.dcs.zirkapp.app.activities.MessagingActivity;
import com.ecp.gsy.dcs.zirkapp.app.util.broadcast.CountMessagesReceiver;
import com.ecp.gsy.dcs.zirkapp.app.util.broadcast.SinchConnectReceiver;
import com.ecp.gsy.dcs.zirkapp.app.util.locations.Location;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.models.ParseZHistory;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.models.ParseZMessage;
import com.ecp.gsy.dcs.zirkapp.app.util.services.LocationService;
import com.ecp.gsy.dcs.zirkapp.app.util.task.RefreshDataUsersTask;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.Serializable;
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
    private LinearLayout layoutUsersDefault;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users, container, false);
        setHasOptionsMenu(true);

        globalApplication = (GlobalApplication) getActivity().getApplicationContext();

        currentUser = ParseUser.getCurrentUser();

        if (currentUser != null) {
            isConnectedUser = currentUser.getBoolean("online");
            callLocation();
        }

        inicializarCompUI(view);

        //Comprobar el estado del servicio Sinch
        sinchConnectReceiver = new SinchConnectReceiver(layoutInitService, listViewUserOnline);
        //Contar los mensajes recibidos
        countMessagesReceiver = new CountMessagesReceiver(listViewUserOnline);

        instance = this;

        return view;
    }

    public static boolean isRunning() {
        return instance != null;
    }

    public static UsersFragment getInstance() {
        return instance;
    }

    private void callLocation() {
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                try {
                    Thread.sleep(4000); // 4 segundos
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                findUsersOnline(getCurrentLocation());
            }
        }.execute();
    }


    private void inicializarCompUI(View view) {
        //Layout
        layoutUsersDefault = (LinearLayout) view.findViewById(R.id.layoutUsersDefault);
        layoutUsersNoFound = (LinearLayout) view.findViewById(R.id.layoutUsersNoFound);
        layoutUsersFinder = (LinearLayout) view.findViewById(R.id.layoutUsersFinder);
        layoutChatOffline = (LinearLayout) view.findViewById(R.id.layoutChatOffline);
        layoutGpsOff = (LinearLayout) view.findViewById(R.id.layoutGpsOff);
        layoutInitService = (LinearLayout) view.findViewById(R.id.layoutInitService);

        lblInfoChat = (TextView) view.findViewById(R.id.lblInfoChat);

        ImageView imageView = (ImageView) view.findViewById(R.id.imgLogoZirkapp);
        Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.fade);
        imageView.startAnimation(animation);

        //ListView
        listViewUserOnline = (ListView) view.findViewById(R.id.usersListView);
        registerForContextMenu(listViewUserOnline);
        listViewUserOnline.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ((TextView) view.findViewById(R.id.lblCantMessages)).setText(null);
                ParseUser parseUser = (ParseUser) adapterView.getAdapter().getItem(i);
                abrirConversa(parseUser);
            }
        });

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
                if (isConnectedUser) {
                    layoutUsersDefault.setVisibility(View.VISIBLE);
                } else {
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
    }

    /**
     * Actualiza la ubicacion del usuario actual y busca los usuarios en Linea y que esten en cerca
     */
    public void findUsersOnline(Location currentLocation) {
        if (isConnectedUser && currentLocation != null) {
            //Tomar valores de las preferencias de usuarios
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            int dist_max = Integer.parseInt(preferences.getString("max_dist_list", "10"));

            RefreshDataUsersTask refresDataTask = new RefreshDataUsersTask(getActivity().getApplicationContext(), currentUser, currentLocation, listViewUserOnline);
            refresDataTask.setSwipeRefreshLayout(swipeRefreshLayout);
            refresDataTask.setLayoutUsersFinder(layoutUsersFinder);
            refresDataTask.setLayoutUsersNoFound(layoutUsersNoFound);
            refresDataTask.execute(dist_max);

            //UI
            layoutChatOffline.setVisibility(View.GONE);
            layoutGpsOff.setVisibility(View.GONE);
            layoutUsersDefault.setVisibility(View.GONE);
        } else {
            //1. Layouts Invisibles
            layoutUsersNoFound.setVisibility(View.GONE);
            layoutUsersFinder.setVisibility(View.GONE);
            layoutGpsOff.setVisibility(View.GONE);
            layoutChatOffline.setVisibility(View.GONE);
            layoutUsersDefault.setVisibility(View.GONE);
            //2. Mostrar Layout correspondiente
            if (globalApplication.isConectedToInternet()) {
                if (globalApplication.isEnabledGetLocation()) {
                    if (!isConnectedUser) {
                        layoutChatOffline.setVisibility(View.VISIBLE);
                        lblInfoChat.setText("Chat Offline");
                    } else {
                        layoutUsersDefault.setVisibility(View.VISIBLE);
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
        Intent intent = new Intent(getActivity(), MessagingActivity.class);
        startActivity(intent);
    }

    /**
     * Busca y elimina la conversación del usuario selecionado
     *
     * @param userChat
     */
    private void deleteParseMessageHistory(final ParseUser userChat) {
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

                ParseUser[] userIds = {currentUser, userChat};

                //Buscar los sinchId de los mensajes de la conversacion
                ParseQuery<ParseZMessage> innerQuery = ParseQuery.getQuery(ParseZMessage.class);
                innerQuery.whereContainedIn(ParseZMessage.SENDER_ID, Arrays.asList(userIds));
                innerQuery.whereContainedIn(ParseZMessage.RECIPIENT_ID, Arrays.asList(userIds));

                //Buscar los sinchId de usuario actual
                ParseQuery<ParseZHistory> query = ParseQuery.getQuery(ParseZHistory.class);
                query.whereMatchesKeyInQuery(ParseZHistory.SINCH_ID, ParseZMessage.SINCH_ID, innerQuery);
                query.whereEqualTo(ParseZHistory.USER, currentUser);
                query.findInBackground(new FindCallback<ParseZHistory>() {
                    @Override
                    public void done(List<ParseZHistory> zHistories, ParseException e) {
                        if (e == null) {
                            ParseObject.deleteAllInBackground(zHistories);
                            Toast.makeText(getActivity(), getResources().getString(R.string.msgChatDeleteOk), Toast.LENGTH_SHORT).show();
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
                android.location.Location tmpLocation = locationService.getCurrentLocation();
                if (tmpLocation != null)
                    location = new Location(tmpLocation.getLatitude(), tmpLocation.getLongitude());
            }
        }
        return location;
    }

    @Override
    public void onResume() {
        //Registrar los Broadcast
        getActivity().registerReceiver(countMessagesReceiver, new IntentFilter(CountMessagesReceiver.ACTION_LISTENER));
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
                    deleteParseMessageHistory(parseUser);
                return true;
            case R.id.ctx_lock_user:
                Toast.makeText(getActivity(), "Proximamente...", Toast.LENGTH_SHORT).show();
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}

