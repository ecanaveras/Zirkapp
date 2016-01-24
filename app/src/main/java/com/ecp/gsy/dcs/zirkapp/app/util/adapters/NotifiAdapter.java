package com.ecp.gsy.dcs.zirkapp.app.util.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
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
import com.ecp.gsy.dcs.zirkapp.app.util.task.SendPushTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by Elder on 04/05/2015.
 */
public class NotifiAdapter extends BaseAdapter {

    private GlobalApplication globalApplication = null;
    private List<ParseZNotifi> notificationArrayList;
    private Context context;
    private int cantNotiNoRead = 0;

    public NotifiAdapter(Context context, List<ParseZNotifi> notifiList) {
        this.notificationArrayList = notifiList;
        this.context = context;
        if (context != null)
            globalApplication = (GlobalApplication) context.getApplicationContext();
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
        ImageView avatar = (ImageView) vista.findViewById(R.id.imgAvatar);
        TextView summary = (TextView) vista.findViewById(R.id.lblSummaryNoti);
        TextView detail = (TextView) vista.findViewById(R.id.lblDetailNoti);
        TextView createdAt = (TextView) vista.findViewById(R.id.lblCreatedAt);
        ImageView imgIconNoti = (ImageView) vista.findViewById(R.id.imgIconNoti);
        //3. Asignar Valores
        imgIconNoti.setBackground(getIconNotifi(item.getTypeNoti()));
        globalApplication.setAvatarRoundedResize(item.getSenderUser().getParseFile("avatar"), avatar, 100, 100);

        //avatar.setImageDrawable(GlobalApplication.getAvatar(item.getUserMessage()));
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

    private Drawable getIconNotifi(int typeNotifi) {
        Drawable drawable = null;
        switch (typeNotifi) {
            case SendPushTask.PUSH_CHAT:
                drawable = context.getResources().getDrawable(R.drawable.ic_chat_white_24dp);
                break;
            case SendPushTask.PUSH_COMMENT:
                drawable = context.getResources().getDrawable(R.drawable.ic_comment_white_24dp);
                break;
            case SendPushTask.PUSH_ZIMESS:
                drawable = context.getResources().getDrawable(R.drawable.ic_note_add_white_24dp);
                break;
            case SendPushTask.PUSH_FAVORITE:
                drawable = context.getResources().getDrawable(R.drawable.ic_favorite_white_24dp);
                break;
            case SendPushTask.PUSH_ZISS:
                drawable = context.getResources().getDrawable(R.drawable.ic_ziss_on_white_24dp);
                break;
            case SendPushTask.PUSH_QUOTE:
                drawable = context.getResources().getDrawable(R.drawable.ic_quote_white_24dp);
                break;
        }
        return drawable;
    }
}
