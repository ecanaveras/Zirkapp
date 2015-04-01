package com.ecp.gsy.dcs.zirkapp.app.util.task;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ecp.gsy.dcs.zirkapp.app.util.locations.Location;
import com.ecp.gsy.dcs.zirkapp.app.util.services.ManagerGPS;

/**
 * Created by Elder on 17/03/2015.
 */
public class RefreshDataAddressTask extends AsyncTask<String, Void, String> {

    private TextView textView;
    private ProgressBar progressBar;
    private ManagerGPS managerGPS;
    private boolean isHint = false;

    public RefreshDataAddressTask(ManagerGPS managerGPS, TextView textView, boolean isHint) {
        this.managerGPS = managerGPS;
        this.textView = textView;
        this.isHint = isHint;
    }

    public RefreshDataAddressTask(ManagerGPS managerGPS, TextView textView, ProgressBar progressBar) {
        this.managerGPS = managerGPS;
        this.textView = textView;
        this.progressBar = progressBar;
    }

    @Override
    protected void onPreExecute() {
        if (progressBar != null && textView != null) {
            textView.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected String doInBackground(String... strings) {
        if (textView != null && textView.getText() != null) {
            if (managerGPS.isEnableGetLocation()) {
                return managerGPS.getLocality();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(String addressLine) {
        if (progressBar != null && textView != null) {
            textView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
        }

        if (textView != null && addressLine != null) {
            if (isHint) {
                textView.setHint(addressLine);
            } else {
                textView.setText(addressLine);
            }
        }
    }
}
