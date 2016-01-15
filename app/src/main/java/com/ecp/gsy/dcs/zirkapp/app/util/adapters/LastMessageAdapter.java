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
import com.ecp.gsy.dcs.zirkapp.app.util.parse.models.ParseZLastMessage;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.models.ParseZMessage;
import com.parse.ParseUser;

import java.util.List;

/**
 * Created by Elder on 14/01/2016.
 */
public class LastMessageAdapter extends BaseAdapter {

    private List<ParseZLastMessage> lastMessageList;
    private Context context;
    private GlobalApplication application;
    private String currentUserId;

    public LastMessageAdapter(Context context, List<ParseZLastMessage> lastMessageList, String currentUserId) {
        this.context = context;
        this.lastMessageList = lastMessageList;
        this.currentUserId = currentUserId;
        if (context != null)
            application = (GlobalApplication) context.getApplicationContext();
    }

    @Override
    public int getCount() {
        return lastMessageList.size();
    }

    @Override
    public Object getItem(int i) {
        return lastMessageList.get(i);
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
            vista = layoutInflater.inflate(R.layout.itemlist_last_message, viewGroup, false);
        }

        //1. Tomar usuario && cantidad de mensajes no leidos!
        ParseUser parseSenderUser = lastMessageList.get(i).getSenderId();
        ParseUser parseReceptorUser = lastMessageList.get(i).getRecipientId();
        ParseUser parseUser = !parseSenderUser.getObjectId().equals(currentUserId) ? parseSenderUser : parseReceptorUser;

        ParseZMessage lastMessage = lastMessageList.get(i).getZMessageId();
        boolean isSender = lastMessage.getSenderId().getObjectId().equals(currentUserId);
        //2. Iniciar UI de la lista
        TextView lblUserId = (TextView) vista.findViewById(R.id.lblUserId);
        ImageView imgAvatar = (ImageView) vista.findViewById(R.id.imgAvatar);
        TextView lblUsername = (TextView) vista.findViewById(R.id.lblUserName);
        TextView lblNameUsuario = (TextView) vista.findViewById(R.id.lblNombreUsuario);
        TextView lblLastMessage = (TextView) vista.findViewById(R.id.lblEstado);
        TextView lblCantMessages = (TextView) vista.findViewById(R.id.lblCantMessages);
        TextView lblDate = (TextView) vista.findViewById(R.id.lblDate);

        //3. Asignar valores
        lblUserId.setText(parseUser.getObjectId());
        lblUsername.setText(parseUser.getUsername());
        lblNameUsuario.setText(parseUser.getString("name") != null ? parseUser.getString("name") : parseUser.getUsername());
        lblLastMessage.setText(lastMessage != null ? ((isSender ? "Tu: " : "") + lastMessage.getMessageText()) : "I'm using Zirkapp!");
        lblDate.setText(GlobalApplication.getDescTimepass(lastMessage.getCreatedAt()));

        if (!lastMessage.isMessageRead() && !isSender) {
            lblCantMessages.setText("New");
            lblCantMessages.setVisibility(View.VISIBLE);
        } else {
            lblCantMessages.setVisibility(View.GONE);
        }
        //Estableciendo Imagen
        if (application != null)
            application.setAvatarRoundedResize(parseUser.getParseFile("avatar"), imgAvatar, 100, 100);

        return vista;
    }
}
