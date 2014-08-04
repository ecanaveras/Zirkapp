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
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ListView;

import com.ecp.gsy.dcs.zirkapp.app.DetailZimessActivity;
import com.ecp.gsy.dcs.zirkapp.app.NewZimessActivity;
import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.adapters.AdapterZimess;
import com.ecp.gsy.dcs.zirkapp.app.util.beans.Zimess;
import com.ecp.gsy.dcs.zirkapp.app.util.task.JSONApplication;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Elder on 15/07/2014.
 */
public class Fmessages extends Fragment {

    private ListView listZMessages;
    private AdapterZimess zmAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messages, container, false);
        inicializarUI(view);
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.list_zimess_activity_action, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Manejar seleccion en el menú
        switch (item.getItemId()) {
            case R.id.action_bar_search_zmess:
                downloadOrUpdateZmess();
                return true;
            case R.id.action_bar_new_zmess:
                Intent intent = new Intent(getActivity(), NewZimessActivity.class);
                startActivity(intent);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void inicializarUI(View view) {
        listZMessages = (ListView) view.findViewById(R.id.listZMessages);
        listZMessages.setDivider(null);
        listZMessages.setDividerHeight(0);

        //Creamos un adpater standar
        zmAdapter = new AdapterZimess(getActivity(), new ArrayList<Zimess>());
        //Asiganmos el adaprte al listView
        listZMessages.setAdapter(zmAdapter);
        downloadOrUpdateZmess();

        listZMessages.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                view.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.anim_click));
                Zimess zimess = (Zimess) adapterView.getAdapter().getItem(i);
                gotoDetail(zimess);
            }
        });
    }

    /**
     * Vamos al detalle del Zimess
     *
     * @param zimess
     */
    private void gotoDetail(Zimess zimess) {
        Intent intent = new Intent(getActivity(), DetailZimessActivity.class);
        intent.putExtra("zimess", (Serializable) zimess);
        getActivity().startActivity(intent);
        //Animar
        getActivity().overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
    }

    private void downloadOrUpdateZmess() {
        // Actualizamos los datos, pasamos el Context para poder mostrar un ProgressDialog
        ((JSONApplication) getActivity().getApplicationContext()).getData(getActivity(), zmAdapter);
    }


}
