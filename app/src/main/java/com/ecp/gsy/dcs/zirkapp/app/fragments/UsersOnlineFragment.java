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
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.ecp.gsy.dcs.zirkapp.app.util.messages.MessagingActivity;
import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.task.GlobalApplication;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by elcapi05 on 13/08/2014.
 */
public class UsersOnlineFragment extends Fragment {

    private String currenUserId;
    private ArrayList<String> usersOnline;
    private ListView listViewUserOnline;
    private ArrayAdapter namesArrayAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressDialog progressDialog;
    private BroadcastReceiver broadcastReceiver = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users_online, container, false);

        //showSpinner();

        inicializarCompUI(view);
        return view;
    }

    private void inicializarCompUI(View view) {
        //ListView
        listViewUserOnline = (ListView) view.findViewById(R.id.usersListView);
        //Swipe
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.user_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                findUsersOnline();
            }
        });
    }

    @Override
    public void onResume() {
        //Usuario actual
        final GlobalApplication globalApplication = (GlobalApplication) getActivity().getApplicationContext();
        if (globalApplication.getCurrentUser() != null) {
            currenUserId = globalApplication.getCurrentUser().getObjectId();
            findUsersOnline();
        }
        super.onResume();
    }

    private void findUsersOnline() {
        if (currenUserId != null) {
            usersOnline = new ArrayList<String>(); //Almacena los usuarios en Linea
            ParseQuery<ParseUser> query = ParseUser.getQuery();
            query.whereNotEqualTo("objectId", currenUserId);
            query.findInBackground(new FindCallback<ParseUser>() {
                @Override
                public void done(List<ParseUser> parseUsers, ParseException e) {
                    if (e == null) { //Sin excepciones
                        for (ParseUser user : parseUsers) {
                            usersOnline.add(user.getUsername());
                        }
                        namesArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.listview_item_users, usersOnline);
                        listViewUserOnline.setAdapter(namesArrayAdapter);
                        listViewUserOnline.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                abriConversacion(usersOnline, i);
                            }
                        });
                    } else {
                        Toast.makeText(getActivity(),
                                "Error loading user list",
                                Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
        swipeRefreshLayout.setRefreshing(false);
    }

    private void abriConversacion(final ArrayList<String> usersOnline, final int pos) {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("username", usersOnline.get(pos));
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> parseUsers, ParseException e) {
                if (e == null) { //Sin Excepcion
                    Intent intent = new Intent(getActivity().getApplicationContext(), MessagingActivity.class);
                    intent.putExtra("RECIPIENT_ID", parseUsers.get(0).getObjectId());
                    intent.putExtra("RECIPIENT_USERNAME", parseUsers.get(0).getUsername().toString());
                    startActivity(intent);
                } else {
                    Toast.makeText(getActivity(),
                            "Error finding that user",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showSpinner(){
        progressDialog = new ProgressDialog(this.getActivity());
        //Todo usar @String
        progressDialog.setTitle("Cargando");
        progressDialog.setMessage("Espera...");
        progressDialog.show();
        //swipeRefreshLayout.setRefreshing(true);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Boolean success = intent.getBooleanExtra("success", false);
                progressDialog.dismiss();
                if(!success){
                    Toast.makeText(getActivity().getApplicationContext(), "Messaging service failed to start", Toast.LENGTH_LONG).show();
                }
            }
        };

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, new IntentFilter("com.ecp.gsy.dcs.zirkapp.MainActivity"));

    }


}
