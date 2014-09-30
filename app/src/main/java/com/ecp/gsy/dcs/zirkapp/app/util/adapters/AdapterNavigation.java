package com.ecp.gsy.dcs.zirkapp.app.util.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.beans.ItemListDrawer;

import java.util.ArrayList;

/**
 * Created by elcapi05 on 27/08/2014.
 */
public class AdapterNavigation extends BaseAdapter {
    private Activity context;
    ArrayList<ItemListDrawer> navItems;

    public AdapterNavigation(Activity context, ArrayList<ItemListDrawer> navItems) {
        this.context = context;
        this.navItems = navItems;
    }

    @Override
    public int getCount() {
        return navItems.size();
    }

    @Override
    public Object getItem(int posicion) {
        return navItems.get(posicion);
    }

    @Override
    public long getItemId(int posicion) {
        return posicion;
    }

    @Override
    public View getView(int posicion, View view, ViewGroup viewGroup) {
        Fila row;
        LayoutInflater layoutInflater = context.getLayoutInflater();
        if(view == null){
            row = new Fila();
            ItemListDrawer item = navItems.get(posicion);
            view = layoutInflater.inflate(R.layout.listview_item_drawer, null);
            //Titulo
            row.tituloItem = (TextView) view.findViewById(R.id.title_item);
            row.tituloItem.setText(item.getTitulo());
            //Icono
            row.icono = (ImageView) view.findViewById(R.id.icon_item);
            row.icono.setImageResource(item.getIcono());
            view.setTag(row);
        }else {
            row = (Fila) view.getTag();
        }
        return view;
    }

    private static class Fila{
        TextView tituloItem;
        ImageView icono;
    }
}
