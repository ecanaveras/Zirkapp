package com.ecp.gsy.dcs.zirkapp.app.util.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.ecp.gsy.dcs.zirkapp.app.DetailZimessActivity;
import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.beans.Zimess;

import java.util.ArrayList;
import java.util.HashSet;

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
        if (view == null) {
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
        ImageView imgAvatar = (ImageView) vista.findViewById(R.id.imgAvatar);
        ImageView imgOptions = (ImageView) vista.findViewById(R.id.imgOptions);
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
        lblUserName.setText(zimess.getZuser());
        lblMessage.setText(zimess.getZmessage());
        lblTimePass.setText(zimess.getTimePass());

        return vista;
    }

    public void add(Zimess zimess) {
        zimessArrayList.add(zimess);
    }

    public void removeDuplicates() {
//        Set<Zimess> setZimess = new LinkedHashSet<Zimess>(zimessArrayList);
        HashSet hashSet = new HashSet(zimessArrayList);
        zimessArrayList.clear();
        zimessArrayList.addAll(hashSet);
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