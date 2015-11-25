package com.ecp.gsy.dcs.zirkapp.app.fragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ecp.gsy.dcs.zirkapp.app.GlobalApplication;
import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.activities.MessagingActivity;
import com.ecp.gsy.dcs.zirkapp.app.util.broadcast.CountMessagesReceiver;
import com.ecp.gsy.dcs.zirkapp.app.util.broadcast.SinchConnectReceiver;
import com.ecp.gsy.dcs.zirkapp.app.util.listener.FragmentIterationListener;
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

import java.util.Arrays;
import java.util.List;

/**
 * Created by elcapi05 on 13/08/2014.
 */
public class UsersFragment extends Fragment {

    private static UsersFragment instance = null;

    public static final String TAG = "UsersFragment";
    private FragmentIterationListener mCallback = null;

    //private ListView listViewUserOnline;
    private RecyclerView userRecyclerView;
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
    private AlertDialog filterDialog;
    private SharedPreferences preferences;

    public static UsersFragment newInstance(Bundle arguments) {
        UsersFragment usersFragment = new UsersFragment();
        if (arguments != null) {
            usersFragment.setArguments(arguments);
        }
        return usersFragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users_online, container, false);

        globalApplication = (GlobalApplication) getActivity().getApplicationContext();

        currentUser = ParseUser.getCurrentUser();

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        if (currentUser != null) {
            isConnectedUser = currentUser.getBoolean("online");
            callLocation();
        }

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

        userRecyclerView = (RecyclerView) view.findViewById(R.id.usersRecyView);
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        userRecyclerView.setLayoutManager(layoutManager);


        /*
        Button btnFiltro = (Button) view.findViewById(R.id.btnFilter);
        btnFiltro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFilterDialog();
            }
        });

        Button btnHistory = (Button) view.findViewById(R.id.btnHistory);
        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ChatHistoryActivity.class);
                startActivity(intent);
            }
        });
        */

        lblInfoChat = (TextView) view.findViewById(R.id.lblInfoChat);

        ImageView imageView = (ImageView) view.findViewById(R.id.imgLogoZirkapp);
        Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.fade);
        imageView.startAnimation(animation);

        //ListView
        /*listViewUserOnline = (ListView) view.findViewById(R.id.usersListView);
        registerForContextMenu(listViewUserOnline);
        listViewUserOnline.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ((TextView) view.findViewById(R.id.lblCantMessages)).setText(null);
                ParseUser parseUser = (ParseUser) adapterView.getAdapter().getItem(i);
                abrirConversa(parseUser);
            }
        });*/




        /*listViewUserOnline.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition = (listViewUserOnline == null || listViewUserOnline.getChildCount() == 0) ? 0 : listViewUserOnline.getChildAt(0).getTop();
                swipeRefreshLayout.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
                if (firstVisibleItem == 0 && topRowVerticalPosition >= 0) {
                    //Show
                    layoutMenu.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
                    swipeRefreshLayout.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));

                } else {
                    //Hidden
                    layoutMenu.animate().translationY(-layoutMenu.getHeight()).setInterpolator(new AccelerateInterpolator(2));
                    swipeRefreshLayout.animate().translationY(-layoutMenu.getHeight()).setInterpolator(new AccelerateInterpolator(2));
                }
                *//*new AsyncTask<Boolean, Void, Boolean>() {

                    @Override
                    protected Boolean doInBackground(Boolean... params) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        return params[0];
                    }

                    @Override
                    protected void onPostExecute(Boolean show) {
                        if (show) {
                            layoutMenu.setVisibility(View.VISIBLE);
                        } else {
                            layoutMenu.setVisibility(View.GONE);
                        }
                    }
                }.execute(firstVisibleItem == 0 && topRowVerticalPosition >= 0);*//*

            }
        });*/


    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        //Corrige bug de Swipe... [Permite el scroll sin problemas]
        userRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int firstVisibleItem, int dy) {
                int topRow = (recyclerView == null || recyclerView.getChildCount() == 0) ? 0 : recyclerView.getChildAt(0).getTop();
                swipeRefreshLayout.setEnabled(firstVisibleItem == 0 && topRow >= 0);
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

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        try {
            mCallback = (FragmentIterationListener) activity;
        } catch (ClassCastException ex) {
            Log.e(TAG, "El activity debe implementar la interfaz FragmentIterationListener");
        }
    }


    /**
     * Actualiza la ubicacion del usuario actual y busca los usuarios en Linea y que esten en cerca
     */
    public void findUsersOnline(Location currentLocation) {
        if (isConnectedUser && currentLocation != null) {
            //Tomar valores de las preferencias de usuarios
            int dist_max = Integer.parseInt(preferences.getString("max_dist_list", "10"));
            String gender = preferences.getString("filter_user_gender", null);

            RefreshDataUsersTask refresDataTask = new RefreshDataUsersTask(getActivity(), currentUser, currentLocation, userRecyclerView, gender);
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
            userRecyclerView.setAdapter(null);
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
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle);
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
     * Guarda el filtro de genero en las preferencias.
     *
     * @param gender
     */
    private void saveGenderPreference(String gender) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("filter_user_gender", gender);
        editor.commit();
    }

    private String getGenderPreference() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return preferences.getString("filter_user_gender", null);
    }

    private void showFilterDialog() {
        filterDialog = null;
        String gender = getGenderPreference();
        int selected = 0;
        if (gender != null) {
            if (gender.equals("F")) {
                selected = 1;
            } else {
                selected = 2;
            }
        }
        final CharSequence[] optionsSort = {getResources().getString(R.string.msgViewAll), getResources().getString(R.string.mgsViewGirls), getResources().getString(R.string.mgsViewBoys)};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle);
        builder.setTitle("Filtrar...");
        builder.setSingleChoiceItems(optionsSort, selected, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getActivity(), optionsSort[which], Toast.LENGTH_SHORT).show();
                switch (which) {
                    case 0:
                        saveGenderPreference(null);
                        findUsersOnline(getCurrentLocation());
                        break;
                    case 1:
                        saveGenderPreference("F");
                        findUsersOnline(getCurrentLocation());

                        break;
                    case 2:
                        saveGenderPreference("M");
                        findUsersOnline(getCurrentLocation());
                        break;
                }
                filterDialog.dismiss();
            }
        });
        filterDialog = builder.create();
        filterDialog.show();
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
        //getActivity().registerReceiver(countMessagesReceiver, new IntentFilter(CountMessagesReceiver.ACTION_LISTENER));
        //getActivity().registerReceiver(sinchConnectReceiver, new IntentFilter("app.fragments.UsersFragment"));
        super.onResume();
    }

    @Override
    public void onPause() {
        //getActivity().unregisterReceiver(countMessagesReceiver);
        //getActivity().unregisterReceiver(sinchConnectReceiver);
        super.onPause();
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_chat_fragment, menu);
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
        switch (item.getItemId()) {
            case R.id.action_bar_filter_users:
                showFilterDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        /*if (v.getId() == R.id.usersListView) {
            AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) menuInfo;
        }
        getActivity().getMenuInflater().inflate(R.menu.menu_contextual_users, menu);*/
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
       /* AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.ctx_view_profile:
                ParseUser receptorUser = (ParseUser) listViewUserOnline.getAdapter().getItem(acmi.position);
                Intent intent = new Intent(getActivity(), UserProfileActivity.class);
                globalApplication.setCustomParseUser(receptorUser);
                startActivity(intent);
                return true;
            case R.id.ctx_delete_chat:
                ParseUser parseUser = (ParseUser) listViewUserOnline.getAdapter().getItem(acmi.position);
                if (parseUser != null)
                    deleteParseMessageHistory(parseUser);
                return true;
            case R.id.ctx_lock_user:
                Toast.makeText(getActivity(), "Proximamente...", Toast.LENGTH_SHORT).show();
            default:
                return super.onContextItemSelected(item);
        }*/

        return super.onContextItemSelected(item);
    }

    @Override
    public void onDetach() {
        mCallback = null;
        super.onDetach();
    }
}

