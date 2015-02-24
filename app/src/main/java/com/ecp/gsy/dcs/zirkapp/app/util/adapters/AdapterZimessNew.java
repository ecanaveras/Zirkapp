package com.ecp.gsy.dcs.zirkapp.app.util.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.beans.ZimessNew;
import com.ecp.gsy.dcs.zirkapp.app.util.locations.Location;
import com.ecp.gsy.dcs.zirkapp.app.util.locations.ManagerDistance;
import com.ecp.gsy.dcs.zirkapp.app.util.locations.ManagerGPS;

import java.util.ArrayList;

/**
 * Created by Elder on 23/02/2015.
 */
public class AdapterZimessNew extends BaseAdapter {

    private ArrayList<ZimessNew> zimessArrayList;
    private Activity context;

    public AdapterZimessNew(Activity context, ArrayList<ZimessNew> zimessArrayList) {
        this.zimessArrayList = zimessArrayList;
        this.context = context;
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
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View vista  = view;
        if(vista == null){
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            vista = layoutInflater.inflate(R.layout.listview_item_zimess, viewGroup, false);
        }
        //1. Crear Zimess
        ZimessNew zimess = zimessArrayList.get(i);
        //2. Iniciar UI de la lista
        TextView lblMessage = (TextView) vista.findViewById(R.id.lblZimess);
        TextView lblDistance = (TextView) vista.findViewById(R.id.lblDistance);


        //Calcular distancia del Zimess remoto
        ManagerGPS managerGPS = new ManagerGPS(context);
        Location currentLocation = new Location(managerGPS.getLatitud(), managerGPS.getLongitud());

        Location zimessLocation = new Location(zimess.getLocation().getLatitude(), zimess.getLocation().getLongitude());
        ManagerDistance mDistance = new ManagerDistance(currentLocation, zimessLocation);
        lblDistance.setText(mDistance.getDistanciaToString());
        lblMessage.setText(zimess.getZimessText());

        return vista;
    }
}
