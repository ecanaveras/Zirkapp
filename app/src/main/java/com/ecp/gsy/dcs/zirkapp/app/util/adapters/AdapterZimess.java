package com.ecp.gsy.dcs.zirkapp.app.util.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.beans.Zimess;
import com.ecp.gsy.dcs.zirkapp.app.util.locations.Location;
import com.ecp.gsy.dcs.zirkapp.app.util.locations.ManagerDistance;
import com.ecp.gsy.dcs.zirkapp.app.util.locations.ManagerGPS;

import java.util.ArrayList;

/**
 * Created by Elder on 24/07/2014.
 */
public class AdapterZimess extends BaseAdapter {

    protected Activity context;
    protected ArrayList<Zimess> zimessArrayList;
    private ManagerGPS managerGPS;

    public AdapterZimess(Activity context, ArrayList<Zimess> zimessArrayList) {
        this.context = context;
        this.zimessArrayList = zimessArrayList;
        managerGPS = new ManagerGPS(context);
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
        return zimessArrayList.get(i).getId();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View vista = view;
        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            vista = layoutInflater.inflate(R.layout.listview_item_zimess, viewGroup, false);
        }
        //1. Crear Zimess
        Zimess zimess = zimessArrayList.get(i);
        //2. Iniciar UI de la lista
        //TODO Completar datos
        TextView lblUserName = (TextView) vista.findViewById(R.id.lblUserName);
        TextView lblMessage = (TextView) vista.findViewById(R.id.lblZimess);
        TextView lblTimePass = (TextView) vista.findViewById(R.id.txtTiempo);
        ImageView imgAvatar = (ImageView) vista.findViewById(R.id.imgAvatarItem);
        ImageView imgOptions = (ImageView) vista.findViewById(R.id.imgOptionsItem);
        TextView lblDistance = (TextView) vista.findViewById(R.id.lblDistance);
        //LinearLayout lyContainer = (LinearLayout) vista.findViewById(R.id.lyMessage);
        //Action Avatar
        imgAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(AnimationUtils.loadAnimation(context, R.anim.anim_image_click));
                Toast.makeText(context, "Ir a perfil de usuario", Toast.LENGTH_SHORT).show();
            }
        });
        //Action Options Zimess
        imgOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(AnimationUtils.loadAnimation(context, R.anim.anim_image_click));
                showPopup(view, R.menu.option_zimess);
            }
        });
        //3. Establecer datos
        lblUserName.setText(zimess.getUsuario());
        lblMessage.setText(zimess.getZimess());
        lblTimePass.setText(zimess.getTimePass());
        //Calcular distancia del Zimess remoto
        Location currentLocation = new Location(managerGPS.getLatitud(), managerGPS.getLongitud());
        Location zimessLocation = new Location(zimess.getLatitud(), zimess.getLongitud());
        ManagerDistance mDistance = new ManagerDistance(currentLocation, zimessLocation);
        lblDistance.setText(mDistance.getDistanciaToString());

        return vista;
    }

    /**
     * Agrega un nuevo Zimess al listview
     *
     * @param zimess
     */
    public void add(Zimess zimess) {
        int exist = 0;
        for (int i = 0; i < zimessArrayList.size(); i++) {
            Zimess tmp = zimessArrayList.get(i);
                if (tmp.equals(zimess)) {
                exist++;
            }
        }
        if (exist == 0) {
            zimessArrayList.add(zimess);
        }
    }

    private void showPopup(View view, int menu) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.getMenuInflater().inflate(menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Toast.makeText(context, menuItem.getTitle(), Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        popupMenu.show();
    }
}