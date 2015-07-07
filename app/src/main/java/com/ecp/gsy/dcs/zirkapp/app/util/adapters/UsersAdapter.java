package com.ecp.gsy.dcs.zirkapp.app.util.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ecp.gsy.dcs.zirkapp.app.GlobalApplication;
import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.models.ParseZMessage;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Elder on 18/03/2015.
 */
public class UsersAdapter extends BaseAdapter {

    private List<ParseUser> parseUserList;
    private ArrayList<ChatCount> chatCounts = new ArrayList<>();
    private Context context;

    public UsersAdapter(Context context, List<ParseUser> parseUserList) {
        this.context = context;
        this.parseUserList = parseUserList;
        countMessagesForUsers();
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
        TextView lblUsername = (TextView) vista.findViewById(R.id.lblUserName);
        TextView lblNameUsuario = (TextView) vista.findViewById(R.id.lblNombreUsuario);
        TextView lblEstado = (TextView) vista.findViewById(R.id.lblEstado);
        TextView lblCantMessages = (TextView) vista.findViewById(R.id.lblCantMessages);

        //3. Asignar valores
        lblUserId.setText(parseUser.getObjectId());
        lblUsername.setText(parseUser.getUsername());
        lblNameUsuario.setText(parseUser.getString("name") != null ? parseUser.getString("name") : parseUser.getUsername());
        lblEstado.setText(parseUser.getString("wall") != null ? parseUser.getString("wall") : "I'm using Zirkapp!");
        Integer cant = findCantMessages(parseUser);
        if (cant != null)
            lblCantMessages.setText(String.valueOf(cant));
        //Estableciendo Imagen;
        imgAvatar.setImageDrawable(GlobalApplication.getAvatar(parseUser));
        return vista;
    }

    private void countMessagesForUsers() {
        if (parseUserList != null && parseUserList.size() > 0) {
            ParseQuery<ParseZMessage> query = ParseQuery.getQuery(ParseZMessage.class);
            query.whereContainedIn(ParseZMessage.SENDER_ID, parseUserList);
            query.whereEqualTo(ParseZMessage.RECIPIENT_ID, ParseUser.getCurrentUser());
            query.whereEqualTo(ParseZMessage.MESSAGE_READ, false);
            query.orderByAscending(ParseZMessage.SENDER_ID);
            try {
                List<ParseZMessage> zMessages = query.find();
                for (ParseUser user : parseUserList) {
                    int count = 0;
                    for (ParseZMessage m : zMessages) {
                        if (user.equals(m.getSenderId())) {
                            count++;
                        }
                    }
                    if (count > 0)
                        chatCounts.add(new ChatCount(user, count));
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private Integer findCantMessages(ParseUser user) {
        for (ChatCount c : chatCounts) {
            if (c.getUser().equals(user)) {
                return c.getCantMessages();
            }
        }
        return null;
    }

    private class ChatCount {

        private ParseUser user;
        private Integer cantMessages;

        public ChatCount(ParseUser user, Integer cantMessages) {
            this.user = user;
            this.cantMessages = cantMessages;
        }

        public ParseUser getUser() {
            return user;
        }

        public void setUser(ParseUser user) {
            this.user = user;
        }

        public Integer getCantMessages() {
            return cantMessages;
        }

        public void setCantMessages(Integer cantMessages) {
            this.cantMessages = cantMessages;
        }
    }

}
