package com.ecp.gsy.dcs.zirkapp.app.util.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ecp.gsy.dcs.zirkapp.app.DetailZimessActivity;
import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.beans.Zimess;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by Elder on 24/07/2014.
 */
public class AdapterZimess extends BaseAdapter implements View.OnClickListener {

    protected Activity context;
   protected ArrayList<Zimess> zimessArrayList;


    public AdapterZimess(Activity context, ArrayList<Zimess> zimessArrayList) {
        this.context = context;
        this.zimessArrayList = zimessArrayList;
    }

    @Override
    public int getCount() {
        return zimessArrayList.size();
    }

    @Override
    public Object getItem(int i) {
        return zimessArrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return zimessArrayList.get(i).getZid();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View vista = view;
        if(view == null){
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            vista = layoutInflater.inflate(R.layout.listview_item_zimess, viewGroup, false);
        }
        //1. Crear Zimess
        Zimess zimess = zimessArrayList.get(i);
        //2. Iniciar UI de la lista
        //TODO Completar datps
        TextView lblUserName = (TextView) vista.findViewById(R.id.lblUserName);
        TextView lblMessage = (TextView) vista.findViewById(R.id.lblZimess);
        TextView lblTimePass = (TextView) vista.findViewById(R.id.txtTiempo);
        LinearLayout lyContainer = (LinearLayout) vista.findViewById(R.id.lyMessage);
        //3. Establecer datos
        lblUserName.setText(zimess.getZuser());
        lblMessage.setText(zimess.getZmessage());
        lblTimePass.setText(zimess.getTimePass());

        //Manejo del click
        lyContainer.setOnClickListener(this);

        return vista;
    }

    public void add(Zimess zimess){
        zimessArrayList.add(zimess);
    }

    public void removeDuplicates(){
//        Set<Zimess> setZimess = new LinkedHashSet<Zimess>(zimessArrayList);
        HashSet hashSet = new HashSet(zimessArrayList);
        zimessArrayList.clear();
        zimessArrayList.addAll(hashSet);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.lyMessage:
                String msg = ((TextView) ((LinearLayout) view).findViewById(R.id.lblZimess)).getText().toString();
                String user = ((TextView) ((LinearLayout) view.getParent()).findViewById(R.id.lblUserName)).getText().toString();
                gotoDetail(msg, user);
                return;
            default:
                return;
        }
    }

    private void gotoDetail(String msg, String username) {
        Intent intent = new Intent(context, DetailZimessActivity.class);
        intent.putExtra("mensaje",msg);
        //TODO enviar el Zimmes completo
        intent.putExtra("username", username);
        context.startActivity(intent);
    }
}
