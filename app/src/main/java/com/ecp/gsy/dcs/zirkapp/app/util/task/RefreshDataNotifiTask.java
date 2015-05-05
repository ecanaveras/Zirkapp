package com.ecp.gsy.dcs.zirkapp.app.util.task;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ecp.gsy.dcs.zirkapp.app.util.adapters.NotifiAdapter;
import com.ecp.gsy.dcs.zirkapp.app.util.beans.ItemNotification;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.DataParseHelper;
import com.parse.ParseObject;

import java.util.ArrayList;

/**
 * Created by Elder on 04/05/2015.
 */
public class RefreshDataNotifiTask extends AsyncTask<String, Void, ArrayList<ItemNotification>> {

    private ListView listView;
    private NotifiAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Context context;
    private ProgressBar progressBar;
    private String receptorId;
    private TextView lblNotiNotFound;

    public RefreshDataNotifiTask(Context context, String receptorId, ListView listView, SwipeRefreshLayout swipeRefreshLayout, ProgressBar progressBar, TextView lblNotiNotFound) {
        this.context = context;
        this.listView = listView;
        this.swipeRefreshLayout = swipeRefreshLayout;
        this.progressBar = progressBar;
        this.receptorId = receptorId;
        this.lblNotiNotFound = lblNotiNotFound;
    }

    @Override
    protected void onPreExecute() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (lblNotiNotFound != null) lblNotiNotFound.setVisibility(View.GONE);
    }

    @Override
    protected ArrayList<ItemNotification> doInBackground(String... params) {
        ArrayList<ItemNotification> arrayList = new ArrayList<>();
        for (ParseObject parseObject : DataParseHelper.findNotifications(receptorId)) {
            ItemNotification item = new ItemNotification();
            item.setNotiId(parseObject.getObjectId());
            item.setTargetId(parseObject.getString("targetId"));
            item.setSenderId(parseObject.getString("senderId"));
            item.setReceptorId(parseObject.getString("receptorId"));
            item.setDetailNoti(parseObject.getString("detailNoti"));
            item.setSummaryNoti(parseObject.getString("summaryNoti"));
            item.setTypeNoti(parseObject.getInt("typeNoti"));
            item.setReadNoti(parseObject.getBoolean("readNoti"));
            arrayList.add(item);
        }
        return arrayList;
    }

    @Override
    protected void onPostExecute(ArrayList<ItemNotification> itemNotifications) {
        adapter = new NotifiAdapter(context, itemNotifications);
        listView.setAdapter(adapter);

        GlobalApplication.setCantNotifications(adapter.getCantNotiNoRead());

        if (progressBar != null) progressBar.setVisibility(View.GONE);
        if (lblNotiNotFound != null && itemNotifications.size() == 0)
            lblNotiNotFound.setVisibility(View.VISIBLE);

        swipeRefreshLayout.setRefreshing(false);
    }
}
