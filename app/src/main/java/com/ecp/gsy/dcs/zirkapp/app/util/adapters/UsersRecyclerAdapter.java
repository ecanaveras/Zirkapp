package com.ecp.gsy.dcs.zirkapp.app.util.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ecp.gsy.dcs.zirkapp.app.GlobalApplication;
import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.activities.MessagingActivity;
import com.ecp.gsy.dcs.zirkapp.app.activities.UserProfileActivity;
import com.ecp.gsy.dcs.zirkapp.app.util.beans.ItemChatHistory;
import com.ecp.gsy.dcs.zirkapp.app.util.listener.ItemClickListener;
import com.parse.ParseUser;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by ecanaveras on 23/10/2015.
 */
public class UsersRecyclerAdapter extends RecyclerView.Adapter<UsersRecyclerAdapter.UserViewHolder> implements ItemClickListener {

    private final List<ParseUser> userList;
    private Context context;
    private GlobalApplication application;

    public UsersRecyclerAdapter(Context context, List<ParseUser> userList) {
        this.userList = userList;
        this.context = context;
        application = (GlobalApplication) context.getApplicationContext();
    }


    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.itemlist_user_online, parent, false);
        return new UserViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(UserViewHolder holder, int position) {
        ParseUser parseUser = userList.get(position);
        application.setAvatarParse(parseUser.getParseFile("avatar"), holder.imgAvatar, false);
        String name = parseUser.getString("name") != null ? parseUser.getString("name") : parseUser.getUsername();
        holder.lblNombreUsuario.setText(name);
        int edad = calcEdad(parseUser.getDate("birthday"));
        if (edad > 0) {
            holder.lblEdad.setText(String.format(context.getString(R.string.formatOldYears), edad));
            holder.lblEdad.setVisibility(View.VISIBLE);
        } else {
            holder.lblEdad.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    @Override
    public void onItemClick(View view, int position) {
        ParseUser parseUser = userList.get(position);
        Intent intent;
        switch (view.getId()) {
            case R.id.lyUser:
                //Ir la perfil del usuario
                application.setProfileParseUser(parseUser);
                intent = new Intent(context, UserProfileActivity.class);
                context.startActivity(intent);
                break;
            case R.id.btnOpenChat:
                application.setMessagingParseUser(parseUser);
                intent = new Intent(context, MessagingActivity.class);
                context.startActivity(intent);
                break;
        }
    }

    /**
     * Calcula la lblEdad de un usuario
     *
     * @param birthday
     * @return
     */
    private int calcEdad(Date birthday) {
        Double age = 0.0;
        if (birthday != null) {
            Calendar dob = Calendar.getInstance();
            dob.setTime(birthday);
            Calendar today = Calendar.getInstance();
            age = Double.valueOf(today.get(Calendar.YEAR) - dob.get(Calendar.YEAR));
            if (today.get(Calendar.MONTH) < dob.get(Calendar.MONTH)) {
                age--;
            } else if (today.get(Calendar.MONTH) == dob.get(Calendar.MONTH)
                    && today.get(Calendar.DAY_OF_MONTH) < dob.get(Calendar.DAY_OF_MONTH)) {
                age--;
            }
        }
        return age.intValue();
    }

    public class UserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ItemClickListener listener;

        // Campos respectivos de un usuario
        public TextView lblNombreUsuario;
        public TextView lblEdad;
        public ImageView imgAvatar;
        public LinearLayout lyUser;

        public UserViewHolder(View itemView, ItemClickListener listener) {
            super(itemView);

            lyUser = (LinearLayout) itemView.findViewById(R.id.lyUser);
            lblNombreUsuario = (TextView) itemView.findViewById(R.id.lblNombreUsuario);
            lblEdad = (TextView) itemView.findViewById(R.id.lblEdad);
            imgAvatar = (ImageView) itemView.findViewById(R.id.imgAvatar);
            ImageButton btnOpenChat = (ImageButton) itemView.findViewById(R.id.btnOpenChat);

            lyUser.setOnClickListener(this);
            btnOpenChat.setOnClickListener(this);

            this.listener = listener;
        }

        @Override
        public void onClick(View v) {
            listener.onItemClick(v, getAdapterPosition());
        }
    }
}
