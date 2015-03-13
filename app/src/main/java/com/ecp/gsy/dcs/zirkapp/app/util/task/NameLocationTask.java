package com.ecp.gsy.dcs.zirkapp.app.util.task;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.locations.Location;
import com.ecp.gsy.dcs.zirkapp.app.util.locations.ManagerGPS;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by Elder on 02/03/2015.
 */
public class NameLocationTask extends AsyncTask<String, Void, String> {

    private Context context;
    private TextView textView;
    private Location currentLocation;
    private ProgressBar progressBar;

    public NameLocationTask(Context context, Location currentLocation, TextView textView) {
        this.context = context;
        this.textView = textView;
        this.currentLocation = currentLocation;
    }

    public NameLocationTask(Context context, Location currentLocation, TextView textView, ProgressBar progressBar) {
        this.context = context;
        this.textView = textView;
        this.currentLocation = currentLocation;
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
        StringBuilder builder = new StringBuilder();
        if(currentLocation != null) {
            Geocoder geoCoder = new Geocoder(context, Locale.getDefault());
            try {
                List<Address> address = geoCoder.getFromLocation(currentLocation.getLatitud(), currentLocation.getLongitud(), 1);
                if (address.size() > 0) {
                    int maxLines = address.get(0).getMaxAddressLineIndex();
                    for (int i = 0; i < maxLines; i++) {
                        if ((maxLines - 1) == i) {
                            String addressStr = address.get(0).getAddressLine(i);
                            builder.append(addressStr);
                            builder.append(" ");
                            //System.out.println("Dir " + i + " " + addressStr);
                        }
                    }
                } else {
                    builder.append(context.getString(R.string.msgLocationUnknown));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return builder.toString();
    }

    @Override
    protected void onPostExecute(String s) {
        if (progressBar != null && textView != null) {
            textView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
        }

        if (textView != null) {
            textView.setText(s);
        }
    }
}