package com.ecp.gsy.dcs.zirkapp.app.util.task;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ecp.gsy.dcs.zirkapp.app.util.locations.Location;
import com.ecp.gsy.dcs.zirkapp.app.util.locations.ManagerAddress;

/**
 * Created by Elder on 17/03/2015.
 */
public class RefreshDataAddressTask extends AsyncTask<String, Void, String> {

    private TextView textView;
    private ProgressBar progressBar;
    private Location location;
    private Context context;
    private boolean isHint = false;

    public RefreshDataAddressTask(Context context, Location location, TextView textView, boolean isHint) {
        this.location = location;
        this.textView = textView;
        this.context = context;
        this.isHint = isHint;
    }

    public RefreshDataAddressTask(Context context, Location location, TextView textView, ProgressBar progressBar) {
        this.context = context;
        this.location = location;
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
            if (location != null) {
                ManagerAddress ma = new ManagerAddress(context, location);
                return ma.getLocality();
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
