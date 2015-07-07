package com.ecp.gsy.dcs.zirkapp.app.util.task;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ecp.gsy.dcs.zirkapp.app.GlobalApplication;
import com.ecp.gsy.dcs.zirkapp.app.util.adapters.NotifiAdapter;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.DataParseHelper;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.models.ParseZNotifi;
import com.parse.ParseUser;

import java.util.List;

/**
 * Created by Elder on 04/05/2015.
 */
public class RefreshDataNotifiTask extends AsyncTask<String, Void, List<ParseZNotifi>> {

    private ListView listView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Context context;
    private ProgressBar progressBar;
    private ParseUser receptorUser;
    private TextView lblNotiNotFound;
    private int cantNoLeidas = 0;

    public RefreshDataNotifiTask(Context context, ParseUser receptorUser, ListView listView, SwipeRefreshLayout swipeRefreshLayout, ProgressBar progressBar, TextView lblNotiNotFound) {
        this.context = context;
        this.listView = listView;
        this.swipeRefreshLayout = swipeRefreshLayout;
        this.progressBar = progressBar;
        this.receptorUser = receptorUser;
        this.lblNotiNotFound = lblNotiNotFound;
    }

    @Override
    protected void onPreExecute() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (lblNotiNotFound != null) lblNotiNotFound.setVisibility(View.GONE);
    }

    @Override
    protected List<ParseZNotifi> doInBackground(String... params) {
        List<ParseZNotifi> parseZNotifis = DataParseHelper.findNotifications(receptorUser);
        for (ParseZNotifi notifi : parseZNotifis) {
            if (!notifi.isReadNoti())
                cantNoLeidas++;
        }
        return parseZNotifis;
    }

    @Override
    protected void onPostExecute(List<ParseZNotifi> itemNotifications) {
        NotifiAdapter adapter = new NotifiAdapter(context, itemNotifications);
        listView.setAdapter(adapter);


        GlobalApplication.setCantNotifications(cantNoLeidas);

        if (progressBar != null) progressBar.setVisibility(View.GONE);
        if (lblNotiNotFound != null && itemNotifications.size() == 0)
            lblNotiNotFound.setVisibility(View.VISIBLE);

        swipeRefreshLayout.setRefreshing(false);
    }
}
