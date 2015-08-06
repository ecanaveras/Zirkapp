package com.ecp.gsy.dcs.zirkapp.app.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ecp.gsy.dcs.zirkapp.app.GlobalApplication;
import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.activities.DetailZimessActivity;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.models.ParseZNotifi;
import com.ecp.gsy.dcs.zirkapp.app.util.task.RefreshDataNotifiTask;
import com.ecp.gsy.dcs.zirkapp.app.util.task.SendPushTask;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Elder on 24/02/2015.
 */
public class NotificationsFragment extends Fragment {

    private static NotificationsFragment instance = null;
    private ListView listNotifi;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView lblNotiNotFound;
    private ParseUser currentUser;
    private GlobalApplication globalApplication;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);
        setHasOptionsMenu(true);

        currentUser = ParseUser.getCurrentUser();

        globalApplication = (GlobalApplication) getActivity().getApplicationContext();

        inicializarCompUI(view);
        findNotifications(currentUser);
        return view;
    }

    public static boolean isRunning() {
        return instance != null;
    }

    public static NotificationsFragment getInstance() {
        return instance;
    }

    private void inicializarCompUI(View view) {
        listNotifi = (ListView) view.findViewById(R.id.listNotifi);
        progressBar = (ProgressBar) view.findViewById(R.id.progressLoad);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshNoti);
        lblNotiNotFound = (TextView) view.findViewById(R.id.lblNotiNotFound);

        listNotifi.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ParseZNotifi item = (ParseZNotifi) parent.getAdapter().getItem(position);
                if (!item.isReadNoti()) saveReadNotificacion(item);
                goToTarget(item);
            }
        });

        listNotifi.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition = (listNotifi == null || listNotifi.getChildCount() == 0) ? 0 : listNotifi.getChildAt(0).getTop();
                swipeRefreshLayout.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                findNotifications(currentUser);
            }
        });
    }

    public void findNotifications(ParseUser recpetorUser) {
        new RefreshDataNotifiTask(getActivity(), recpetorUser, listNotifi, swipeRefreshLayout, progressBar, lblNotiNotFound).execute();
    }

    /**
     * Navega hasta el objeto afectado por la notificacion
     *
     * @param item
     */
    private void goToTarget(ParseZNotifi item) {
        if (item.getTypeNoti() == SendPushTask.PUSH_COMMENT || item.getTypeNoti() == SendPushTask.PUSH_QUOTE) {
            if (item.getZimessTarget() != null) {
                Activity activity = getActivity();
                Intent intent = new Intent(activity, DetailZimessActivity.class);
                globalApplication.setTempZimess(item.getZimessTarget());
                activity.startActivityForResult(intent, 105);
                NotificationManager manager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                manager.cancel(item.getTypeNoti());
            } else {
                Toast.makeText(getActivity(), getResources().getString(R.string.msgZimessNoFound), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Guarda el nuevo estado de la notificacion
     *
     * @param item
     */
    private void saveReadNotificacion(ParseZNotifi item) {
        if (item != null) {
            //Busca el marca como leidas todas las notificaciones que conducen al mismo Zimess
            ParseQuery<ParseZNotifi> query = ParseQuery.getQuery(ParseZNotifi.class);
            query.whereEqualTo(ParseZNotifi.ZIMESS_TARGET, item.getZimessTarget());
            query.whereEqualTo(ParseZNotifi.READ_NOTI, false);
            query.findInBackground(new FindCallback<ParseZNotifi>() {
                @Override
                public void done(List<ParseZNotifi> parseZNotifis, ParseException e) {
                    if (e == null && parseZNotifis.size() > 0) {
                        List<ParseZNotifi> listUpdate = new ArrayList<>();
                        for (ParseZNotifi zNotifi : parseZNotifis) {
                            zNotifi.put(ParseZNotifi.READ_NOTI, true);
                            listUpdate.add(zNotifi);
                            //Cancelar todas las notificaciones
                            NotificationManager manager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                            manager.cancel(zNotifi.getTypeNoti());
                        }
                        if (listUpdate.size() > 0) {
                            //Actualiza todas las notificaciones
                            ParseObject.saveAllInBackground(listUpdate);
                        }
                        findNotifications(currentUser);
                    }
                }
            });

        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }
}
