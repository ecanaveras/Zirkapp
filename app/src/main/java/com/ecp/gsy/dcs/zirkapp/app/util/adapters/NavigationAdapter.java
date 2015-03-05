package com.ecp.gsy.dcs.zirkapp.app.util.adapters;

import android.content.Context;
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
public class NavigationAdapter extends BaseAdapter {

    private final Context context;
    private ArrayList<ItemListDrawer> navItems;

    public NavigationAdapter(Context context, ArrayList<ItemListDrawer> navItems) {
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
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(int posicion, View view, ViewGroup viewGroup) {
        ViewHolder holder = null;
        ItemListDrawer item = navItems.get(posicion);
        if (view == null) {
            holder = new ViewHolder();
            view = LayoutInflater.from(context).inflate(R.layout.listview_item_drawer, viewGroup, false);
            //Titulo
            holder.tituloItem = (TextView) view.findViewById(R.id.title_item);
            //Icono
            holder.icono = (ImageView) view.findViewById(R.id.icon_item);
            //Cant Notificaciones
            holder.cantNotificacion = (TextView) view.findViewById(R.id.lblCantNoti);
            //Separador
            holder.separator = (TextView) view.findViewById(R.id.drawer_separator);

            view.setTag(holder);
        }

        holder = (ViewHolder) view.getTag();

        if (holder == null && view != null) {
            Object tag = view.getTag();
            if (tag instanceof ViewHolder) {
                holder = (ViewHolder) tag;
            }
        }

        if (item != null && holder != null) {

            if (holder.tituloItem != null)
                holder.tituloItem.setText(item.getTitulo());

            //Counter
            if (holder.cantNotificacion != null) {
                if (item.getCantNotificaciones() != null && item.getCantNotificaciones() > 0) {
                    holder.cantNotificacion.setVisibility(View.VISIBLE);
                    holder.cantNotificacion.setText("" + item.getCantNotificaciones());
                } else {
                    //Hide counter if == 0
                    holder.cantNotificacion.setVisibility(View.GONE);
                }
            }

            if (holder.icono != null) {
                if (item.getIcono() > 0) {
                    holder.icono.setVisibility(View.VISIBLE);
                    holder.icono.setImageResource(item.getIcono());
                } else {
                    holder.icono.setVisibility(View.GONE);
                }
            }

            if (holder.separator != null) {
                if (item.isShowSeparator()) {
                    holder.separator.setVisibility(View.VISIBLE);
                    holder.separator.setText(item.getTextSeparador());
                } else {
                    holder.separator.setVisibility(View.GONE);
                }
            }
        }

        return view;
    }

    class ViewHolder {
        public TextView tituloItem;
        public ImageView icono;
        public TextView cantNotificacion;
        public TextView separator;
    }
}
