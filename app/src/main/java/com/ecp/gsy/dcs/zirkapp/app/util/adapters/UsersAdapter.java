package com.ecp.gsy.dcs.zirkapp.app.util.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.task.GlobalApplication;
import com.parse.ParseUser;

import java.util.List;

/**
 * Created by Elder on 18/03/2015.
 */
public class UsersAdapter extends BaseAdapter {

    private List<ParseUser> parseUserList;
    private Context context;

    public UsersAdapter(Context context, List<ParseUser> parseUserList) {
        this.context = context;
        this.parseUserList = parseUserList;
    }

    @Override
    public int getCount() {
        return parseUserList.size();
    }

    @Override
    public Object getItem(int i) {
        return parseUserList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View vista = view;
        if (vista == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            vista = layoutInflater.inflate(R.layout.cardview_item_user, viewGroup, false);
        }

        //1. Tomar usuario
        ParseUser parseUser = parseUserList.get(i);
        //2. Iniciar UI de la lista
        ImageView imgAvatar = (ImageView) vista.findViewById(R.id.imgAvatar);
        TextView lblCommentUser = (TextView) vista.findViewById(R.id.lblUserName);
        TextView lblCommentName = (TextView) vista.findViewById(R.id.lblNombreUsuario);

        //3. Asignar valores
        lblCommentUser.setText(parseUser.getUsername());
        lblCommentName.setText(parseUser.getString("name"));
        //Estableciendo Imagen;
        imgAvatar.setImageBitmap(GlobalApplication.getAvatar(parseUser));

        return vista;
    }

}
