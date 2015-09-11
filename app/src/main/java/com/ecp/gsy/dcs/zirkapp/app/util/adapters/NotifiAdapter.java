package com.ecp.gsy.dcs.zirkapp.app.util.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ecp.gsy.dcs.zirkapp.app.GlobalApplication;
import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.models.ParseZNotifi;
import com.ecp.gsy.dcs.zirkapp.app.util.picasso.CircleTransform;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by Elder on 04/05/2015.
 */
public class NotifiAdapter extends BaseAdapter {

    private List<ParseZNotifi> notificationArrayList;
    private Context context;
    private int cantNotiNoRead = 0;

    public NotifiAdapter(Context context, List<ParseZNotifi> notifiList) {
        this.notificationArrayList = notifiList;
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
        ParseZNotifi item = notificationArrayList.get(position);
        //2. Manipular UI
        LinearLayout contentItem = (LinearLayout) vista.findViewById(R.id.layoutItemNoti);
        ImageView avatar = (ImageView) vista.findViewById(R.id.imgIconNoti);
        TextView summary = (TextView) vista.findViewById(R.id.lblSummaryNoti);
        TextView detail = (TextView) vista.findViewById(R.id.lblDetailNoti);
        TextView createdAt = (TextView) vista.findViewById(R.id.lblCreatedAt);
        //3. Asignar Valores
        /*switch (item.getTypeNoti()) {
            case SendPushTask.PUSH_CHAT:
                icon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_icon_response));
                break;
            case SendPushTask.PUSH_COMMENT:
                icon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_icon_review_comment));
                break;
            case SendPushTask.PUSH_ZIMESS:
                icon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_icon_location));
                break;
        }*/
        //La imagen debe ser cuadrada
        if (item.getSenderUser() != null && item.getSenderUser().getParseFile("avatar") != null) {
            Picasso.with(context)
                    .load(item.getSenderUser().getParseFile("avatar").getUrl())
                    .transform(new CircleTransform())
                    .into(avatar);
            //imgAvatar.setImageDrawable(GlobalApplication.getAvatar(currentUser));
        }
        //avatar.setImageDrawable(GlobalApplication.getAvatar(item.getSenderUser()));
        summary.setText(item.getSummaryNoti());
        detail.setText(item.getDetailNoti());
        createdAt.setText(new SimpleDateFormat("dd/MM/yyyy hh:mm a").format(item.getCreatedAt()));
        if (!item.isReadNoti()) {
            contentItem.setBackgroundColor(context.getResources().getColor(R.color.light_primary_color));
        } else {
            contentItem.setBackgroundColor(context.getResources().getColor(R.color.text_primary_color));
        }
        return vista;
    }
}
