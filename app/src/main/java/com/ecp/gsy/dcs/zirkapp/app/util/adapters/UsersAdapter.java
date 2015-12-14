package com.ecp.gsy.dcs.zirkapp.app.util.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ecp.gsy.dcs.zirkapp.app.GlobalApplication;
import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.beans.ItemChatHistory;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.models.ParseZMessage;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseUser;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by Elder on 18/03/2015.
 */
public class UsersAdapter extends BaseAdapter {

    private List<ItemChatHistory> parseUserList;
    private Context context;
    private GlobalApplication application;

    public UsersAdapter(Context context, List<ItemChatHistory> parseUserList) {
        this.context = context;
        this.parseUserList = parseUserList;
        if (context != null)
            application = (GlobalApplication) context.getApplicationContext();
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
            vista = layoutInflater.inflate(R.layout.itemlist_users, viewGroup, false);
        }

        //1. Tomar usuario && cantidad de mensajes no leidos!
        ParseUser parseUser = parseUserList.get(i).getUserMessage();
        Integer cantMessages = parseUserList.get(i).getCantMessagesNoRead();
        ParseZMessage lastMessage = parseUserList.get(i).getLastMessage();
        boolean isSender = parseUserList.get(i).isSender();
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
        lblDate.setText(getTimepass(lastMessage.getCreatedAt()));

        if (cantMessages != null && cantMessages > 0) {
            lblCantMessages.setText(String.valueOf(cantMessages));
            lblCantMessages.setVisibility(View.VISIBLE);
        } else {
            lblCantMessages.setVisibility(View.GONE);
        }
        //Estableciendo Imagen
        if (application != null)
            application.setAvatarRoundedResize(parseUser.getParseFile("avatar"), imgAvatar, 100, 100);

        return vista;
    }


    private String getTimepass(Date createAt) {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTime(createAt);
        calendar.set(Calendar.HOUR, 0);
        Date currentDate = new Date();
        Long time = (currentDate.getTime() - createAt.getTime()); //Tiempo real
        String result = "";
        DateFormat dayFormat = new SimpleDateFormat("dd");
        if (TimeUnit.DAYS.convert(time, TimeUnit.MILLISECONDS) < 2) {
            if (dayFormat.format(currentDate).equals(dayFormat.format(createAt))) {
                //HORAS
                result = new SimpleDateFormat("hh:mm a").format(createAt);
            } else {
                //UN DIA
                result = context.getString(R.string.lblYesterday).toUpperCase();
            }
        } else {
            result = new SimpleDateFormat("dd/MM/yyyy").format(createAt);
        }
        return result;
    }
}

