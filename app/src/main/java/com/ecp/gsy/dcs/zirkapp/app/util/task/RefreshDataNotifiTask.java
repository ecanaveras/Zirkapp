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
import com.ecp.gsy.dcs.zirkapp.app.util.beans.ItemNotification;
import com.ecp.gsy.dcs.zirkapp.app.util.beans.Zimess;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.DataParseHelper;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;

/**
 * Created by Elder on 04/05/2015.
 */
public class RefreshDataNotifiTask extends AsyncTask<String, Void, ArrayList<ItemNotification>> {

    private ListView listView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Context context;
    private ProgressBar progressBar;
    private ParseUser receptorUser;
    private TextView lblNotiNotFound;
    private int noLeidas = 0;

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
    protected ArrayList<ItemNotification> doInBackground(String... params) {
        ArrayList<ItemNotification> arrayList = new ArrayList<>();
        for (ParseObject parseObject : DataParseHelper.findNotifications(receptorUser)) {
            ItemNotification item = new ItemNotification();
            item.setNotiId(parseObject.getObjectId());
            item.setTypeNoti(parseObject.getInt("typeNoti"));
            switch (item.getTypeNoti()) {
                case SendPushTask.PUSH_CHAT:
                    item.setUserTarget(parseObject.getParseUser("userTarget"));
                    break;
                case SendPushTask.PUSH_COMMENT:
                    item.setZimessTarget(getZimess(parseObject.getParseObject("zimessTarget")));
                    break;
            }
            item.setSenderUser(parseObject.getParseUser("senderUser"));
            item.setReceptorUser(parseObject.getParseUser("receptorUser"));
            item.setDetailNoti(parseObject.getString("detailNoti"));
            item.setSummaryNoti(parseObject.getString("summaryNoti"));
            item.setReadNoti(parseObject.getBoolean("readNoti"));
            item.setCreated(parseObject.getCreatedAt());
            if (!item.isReadNoti()) {
                noLeidas++;
            }
            arrayList.add(item);
        }
        return arrayList;
    }

    @Override
    protected void onPostExecute(ArrayList<ItemNotification> itemNotifications) {
        NotifiAdapter adapter = new NotifiAdapter(context, itemNotifications);
        listView.setAdapter(adapter);

        GlobalApplication.setCantNotifications(noLeidas);

        if (progressBar != null) progressBar.setVisibility(View.GONE);
        if (lblNotiNotFound != null && itemNotifications.size() == 0)
            lblNotiNotFound.setVisibility(View.VISIBLE);

        swipeRefreshLayout.setRefreshing(false);
    }

    /**
     * Crea un Zimess
     *
     * @param parseObject
     * @return
     */
    private Zimess getZimess(ParseObject parseObject) {
        if (parseObject != null) {
            Zimess zimessNew = new Zimess();
            zimessNew.setZimessId(parseObject.getObjectId());
            zimessNew.setUser(parseObject.getParseUser("user"));
            zimessNew.setZimessText(parseObject.get("zimessText").toString());
            zimessNew.setLocation(parseObject.getParseGeoPoint("location"));
            zimessNew.setCantComment(parseObject.getInt("cant_comment"));
            zimessNew.setCreateAt(parseObject.getCreatedAt());
            return zimessNew;
        }
        return null;
    }
}
