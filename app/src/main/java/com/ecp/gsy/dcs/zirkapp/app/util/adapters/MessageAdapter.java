package com.ecp.gsy.dcs.zirkapp.app.util.adapters;

import android.app.Activity;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ecp.gsy.dcs.zirkapp.app.R;
import com.sinch.android.rtc.messaging.Message;
import com.sinch.android.rtc.messaging.WritableMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Elder on 21/02/2015.
 */
public class MessageAdapter extends BaseAdapter {

    public static final int DIRECTION_INCOMING = 0;
    public static final int DIRECTION_OUTGOING = 1;

    private List<Pair<Message, Integer>> messages;
    private LayoutInflater layoutInflater;

    public MessageAdapter(Activity activity) {
        layoutInflater = activity.getLayoutInflater();
        messages = new ArrayList<Pair<Message, Integer>>();
    }

    public void addMessage(Message message, Integer direccion) {
        //Verificar que no exista el mensaje, para no duplicar
        for (Pair<Message, Integer> m : messages) {
            if (m.first.getMessageId().equals(message.getMessageId())) {
                return;
            }
        }
        messages.add(new Pair(message, direccion));
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

        Message message = messages.get(i).first;

        TextView txtMessage = (TextView) view.findViewById(R.id.txtMessage);
        txtMessage.setText(message.getTextBody());

        return view;
    }
}
