package com.ecp.gsy.dcs.zirkapp.app.util.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.beans.Zimess;

import java.util.ArrayList;

/**
 * Created by Elder on 24/07/2014.
 */
public class AdapterZimess extends BaseAdapter {

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
        TextView lblZimess = (TextView) vista.findViewById(R.id.lblZimess);
        //3. Establecer datos
        lblUserName.setText(zimess.getZuser());
        lblZimess.setText(zimess.getZmessage());

        return vista;
    }

    public void add(Zimess zimess){
        zimessArrayList.add(zimess);
    }
}
