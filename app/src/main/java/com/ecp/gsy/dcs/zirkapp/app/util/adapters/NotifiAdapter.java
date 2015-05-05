package com.ecp.gsy.dcs.zirkapp.app.util.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.beans.ItemNotification;
import com.ecp.gsy.dcs.zirkapp.app.util.task.SendPushTask;

import java.util.ArrayList;

/**
 * Created by Elder on 04/05/2015.
 */
public class NotifiAdapter extends BaseAdapter {

    private ArrayList<ItemNotification> notificationArrayList;
    private Context context;
    private int cantNotiNoRead = 0;

    public NotifiAdapter(Context context, ArrayList<ItemNotification> notificationArrayList) {
        this.notificationArrayList = notificationArrayList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return notificationArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return notificationArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vista = convertView;
        if (vista == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            vista = layoutInflater.inflate(R.layout.itemlist_notifi, parent, false);
        }

        //1.Tomar Item
        ItemNotification item = notificationArrayList.get(position);
        //2. Manipular UI
        ImageView icon = (ImageView) vista.findViewById(R.id.imgIconNoti);
        TextView summary = (TextView) vista.findViewById(R.id.lblSummaryNoti);
        TextView detail = (TextView) vista.findViewById(R.id.lblDetailNoti);
        TextView senderId = (TextView) vista.findViewById(R.id.lblSenderId);
        TextView receptorId = (TextView) vista.findViewById(R.id.lblReceptorid);
        TextView targetId = (TextView) vista.findViewById(R.id.lblTargetId);
        //3. Asignar Valores
        switch (item.getTypeNoti()) {
            case SendPushTask.PUSH_CHAT:
                icon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_icon_response));
                break;
            case SendPushTask.PUSH_COMMENT:
                icon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_icon_review_comment));
                break;
            case SendPushTask.PUSH_ZIMESS:
                icon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_icon_location));
                break;
        }
        summary.setText(item.getSummaryNoti());
        detail.setText(item.getDetailNoti());
        receptorId.setText(item.getReceptorId());
        senderId.setText(item.getSenderId());
        targetId.setText(item.getTargetId());
        if (!item.isReadNoti()) cantNotiNoRead++;

        return vista;
    }

    public Integer getCantNotiNoRead() {
        if (cantNotiNoRead == 0) return null;
        return cantNotiNoRead;
    }
}
