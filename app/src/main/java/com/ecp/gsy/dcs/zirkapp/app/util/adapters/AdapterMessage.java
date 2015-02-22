package com.ecp.gsy.dcs.zirkapp.app.util.adapters;

import android.app.Activity;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ecp.gsy.dcs.zirkapp.app.R;
import com.sinch.android.rtc.messaging.WritableMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Elder on 21/02/2015.
 */
public class AdapterMessage extends BaseAdapter {

    public static final int DIRECTION_INCOMING = 0;
    public static final int DIRECTION_OUTGOING = 1;

    private String userSendMessageIncoming;
    private String userSendMessageOutgoing;

    private List<Pair<WritableMessage, Integer>> messages;
    private LayoutInflater layoutInflater;

    public AdapterMessage(Activity activity) {
        layoutInflater = activity.getLayoutInflater();
        messages = new ArrayList<Pair<WritableMessage, Integer>>();
    }

    public void addMessage(WritableMessage message, Integer direccion) {
        messages.add(new Pair(message, direccion));
        notifyDataSetChanged();
    }

    public void addMessage(WritableMessage message, Integer direccion, String userSendMessage) {
        messages.add(new Pair(message, direccion));
        this.userSendMessageIncoming = userSendMessage;
        this.userSendMessageOutgoing = userSendMessage;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int i) {
        return messages.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).second;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        int direccion = getItemViewType(i);
        //Mostrar mensaje a la derecha o iquierda dependiendo de la direccion
        if (view == null) {
            int res = 0;
            if (direccion == DIRECTION_INCOMING) {
                res = R.layout.message_left;
            } else if (direccion == DIRECTION_OUTGOING) {
                res = R.layout.message_right;
            }
            view = layoutInflater.inflate(res, viewGroup, false);
        }

        WritableMessage message = messages.get(i).first;

        TextView txtMessage = (TextView) view.findViewById(R.id.txtMessage);
        txtMessage.setText(message.getTextBody());

        TextView txtSender = (TextView) view.findViewById(R.id.txtSender);
        txtSender.setText(DIRECTION_INCOMING == direccion ? userSendMessageIncoming : userSendMessageOutgoing);

        return view;
    }
}
