package com.ecp.gsy.dcs.zirkapp.app.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.ecp.gsy.dcs.zirkapp.app.NewZimessActivity;
import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.task.JSONApplication;

import java.util.ArrayList;

/**
 * Created by Elder on 15/07/2014.
 */
public class Fmessages extends Fragment {

    private ListView listZMessages;
    private ArrayAdapter zmAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messages, container, false);
        inicializarUI(view);
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.messages_activity_action, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Manejar seleccion en el menú
        switch (item.getItemId()){
            case R.id.action_bar_search_zmess :
                downloadOrUpdateZmess();
                return true;
            case R.id.action_bar_new_zmess :
                Intent intent = new Intent(getActivity(), NewZimessActivity.class);
                startActivity(intent);
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void inicializarUI(View view) {
        listZMessages = (ListView) view.findViewById(R.id.listZMessages);
        //Creamos un adpater standar
        zmAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, new ArrayList());
        //Asiganmos el adaprte al listView
        listZMessages.setAdapter(zmAdapter);
        downloadOrUpdateZmess();
    }

    private void downloadOrUpdateZmess(){
        // Actualizamos los datos, pasamos el Context para poder mostrar un ProgressDialog
        ((JSONApplication) getActivity().getApplicationContext()).getData(getActivity(), zmAdapter);
    }


}
