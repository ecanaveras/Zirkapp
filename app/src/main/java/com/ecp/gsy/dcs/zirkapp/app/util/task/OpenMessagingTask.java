package com.ecp.gsy.dcs.zirkapp.app.util.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.ecp.gsy.dcs.zirkapp.app.GlobalApplication;
import com.ecp.gsy.dcs.zirkapp.app.activities.MessagingActivity;
import com.ecp.gsy.dcs.zirkapp.app.activities.UserProfileActivity;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.DataParseHelper;
import com.parse.ParseUser;

/**
 * Created by ecanaveras on 24/11/2015.
 */
public class OpenMessagingTask extends AsyncTask<String, String, ParseUser> {

    ProgressDialog dialog;
    private Context context;
    private GlobalApplication globalApplication;

    public OpenMessagingTask(Context context) {
        this.context = context;
        this.globalApplication = (GlobalApplication) context.getApplicationContext();
    }

    @Override
    protected void onPreExecute() {
        dialog = new ProgressDialog(context);
        dialog.setMessage("Abriendo chat...");
        dialog.show();
    }

    @Override
    protected ParseUser doInBackground(String... params) {
        return DataParseHelper.findUser(params[0]);
    }

    @Override
    protected void onPostExecute(ParseUser parseUser) {
        if (parseUser != null) {
            globalApplication.setMessagingParseUser(parseUser);
            Intent intentProf = new Intent(context, MessagingActivity.class);
            context.startActivity(intentProf);
        }

        dialog.dismiss();
    }
}
