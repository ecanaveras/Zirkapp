package com.ecp.gsy.dcs.zirkapp.app.util.task;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.activities.MainActivity;
import com.ecp.gsy.dcs.zirkapp.app.util.beans.UsersArounddb;
import com.ecp.gsy.dcs.zirkapp.app.util.database.DatabaseHelper;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.parse.ParseUser;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Elder on 05/08/2015.
 */
public class CounterUsersAroundTask extends AsyncTask<List<ParseUser>, Void, Integer> {


    private Context context;
    private DatabaseHelper databaseHelper;

    public CounterUsersAroundTask(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        if (context != null)
            this.databaseHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
    }

    @Override
    protected Integer doInBackground(List<ParseUser>... users) {
        int cantUsersNews = 0;
        if (databaseHelper != null) {
            List<UsersArounddb> listUsers = new ArrayList<>();
            Dao dao = null;
            try {
                dao = databaseHelper.getUsersarounddbDao();
                listUsers = dao.queryForAll();
            } catch (SQLException e) {
                Log.e("Ormlite", "Error buscando users_around");
            }
            //Compara los usuarios online vs los usuarios en la db para notificar los nuevos
            if (listUsers.size() > 0) {
                for (ParseUser user : users[0]) {
                    boolean save = true;
                    for (int i = 0; i < listUsers.size(); i++) {
                        if (user.getObjectId().equals(listUsers.get(i).getUserId())) {
                            save = false;
                            break;
                        }
                    }
                    if (save) {
                        cantUsersNews++;
                        createRow(dao, user.getObjectId());
                    }
                }
            } else if (dao != null) {
                for (ParseUser user : users[0]) {
                    cantUsersNews++;
                    createRow(dao, user.getObjectId());
                }
            }
        }
        return cantUsersNews;
    }

    @Override
    protected void onPostExecute(Integer integer) {
        showNotification(integer);
        closeDatabase();
    }

    private void createRow(Dao dao, String userId) {
        try {
            dao.create(new UsersArounddb(userId));
        } catch (SQLException e) {
            Log.e("Ormlite", "Error creando users_around");
        }
    }

    private void showNotification(int cantUsers) {
        if (cantUsers != 0) {
            String formatMsg = "%s %d %s";
            Intent intent = new Intent(context, MainActivity.class);
            intent.setAction("OPEN_FRAGMENT_GENTE_CERCA");
            PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);

            // build notification
            // the addAction re-use the same intent to keep the example short
            NotificationCompat.Builder noti = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.ic_zirkapp_noti)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher))
                    .setContentTitle(context.getResources().getString(R.string.app_name))
                    .setContentText(String.format(formatMsg, context.getResources().getString(R.string.msgUsersAround), cantUsers, context.getResources().getString(R.string.msgUsersAround2)))
                    .setContentIntent(pIntent)
                    .setAutoCancel(true)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setWhen(System.currentTimeMillis());

            noti.setContentIntent(pIntent);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

            notificationManager.notify(980, noti.build());
        }
    }

    private void closeDatabase() {
        if (databaseHelper != null) {
            OpenHelperManager.releaseHelper();
            databaseHelper = null;
        }
    }
}
