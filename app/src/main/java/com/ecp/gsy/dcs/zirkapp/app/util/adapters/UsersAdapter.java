package com.ecp.gsy.dcs.zirkapp.app.util.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.GlobalApplication;
import com.parse.CountCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

/**
 * Created by Elder on 18/03/2015.
 */
public class UsersAdapter extends BaseAdapter {

    private List<ParseUser> parseUserList;
    private Context context;
    private int cantMessages;

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
        TextView lblUserId = (TextView) vista.findViewById(R.id.lblUserId);
        ImageView imgAvatar = (ImageView) vista.findViewById(R.id.imgAvatar);
        TextView lblCommentUser = (TextView) vista.findViewById(R.id.lblUserName);
        TextView lblCommentName = (TextView) vista.findViewById(R.id.lblNombreUsuario);
        TextView lblCantMessages = (TextView) vista.findViewById(R.id.lblCantMessages);


        //3. Asignar valores
        lblUserId.setText(parseUser.getObjectId());
        lblCommentUser.setText(parseUser.getUsername());
        lblCommentName.setText(parseUser.getString("name") != null ? parseUser.getString("name") : parseUser.getUsername());
        Integer cant = findCantParseMessages(parseUser.getObjectId());
        if (cant != null)
            lblCantMessages.setText(String.valueOf(cant));
        //Estableciendo Imagen;
        imgAvatar.setImageDrawable(GlobalApplication.getAvatar(parseUser));
        return vista;
    }

    private Integer findCantParseMessages(String senderId) {
        cantMessages = 0;
        ParseUser currentUser = ParseUser.getCurrentUser();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("ParseZMessage");
        query.whereEqualTo("senderId", senderId);
        query.whereEqualTo("recipientId", currentUser.getObjectId());
        query.whereEqualTo("messageRead", false);
        query.countInBackground(new CountCallback() {
            @Override
            public void done(int count, ParseException e) {
                if (e == null && count > 0) {
                    cantMessages = count;
                }
            }
        });
        return cantMessages != 0 ? cantMessages : null;
    }

}
