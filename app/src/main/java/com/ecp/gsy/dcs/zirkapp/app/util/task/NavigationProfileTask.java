package com.ecp.gsy.dcs.zirkapp.app.util.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.ecp.gsy.dcs.zirkapp.app.GlobalApplication;
import com.ecp.gsy.dcs.zirkapp.app.activities.UserProfileActivity;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.DataParseHelper;
import com.parse.ParseUser;

/**
 * Created by ecanaveras on 24/11/2015.
 */
public class NavigationProfileTask extends AsyncTask<String, String, ParseUser> {

    ProgressDialog dialog;
    private Context context;
    private GlobalApplication globalApplication;

    public NavigationProfileTask(Context context) {
        this.context = context;
        this.globalApplication = (GlobalApplication) context.getApplicationContext();
    }

    @Override
    protected void onPreExecute() {
        dialog = new ProgressDialog(context);
        dialog.setMessage("Cargando perfil...");
        dialog.show();
    }

    @Override
    protected ParseUser doInBackground(String... params) {
        return DataParseHelper.findUser(params[0]);
    }

    @Override
    protected void onPostExecute(ParseUser parseUser) {
        dialog.dismiss();
        globalApplication.setCustomParseUser(parseUser);
        Intent intentProf = new Intent(context, UserProfileActivity.class);
        context.startActivity(intentProf);
    }
}
