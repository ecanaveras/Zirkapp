package com.ecp.gsy.dcs.zirkapp.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.locations.Location;
import com.ecp.gsy.dcs.zirkapp.app.util.services.LocationService;
import com.ecp.gsy.dcs.zirkapp.app.util.task.RefreshDataZimessTask;
import com.parse.ParseUser;

/**
 * Created by Elder on 21/03/2015.
 */
public class MyZimessActivity extends ActionBarActivity {

    private RecyclerView recyclerView;
    private LinearLayout layoudZimessNoFound;
    private LinearLayout layoudZimessFinder;
    private ParseUser currentUser;
    private Toolbar toolbar;
    public int requestCodeUpdateZimess = 105;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_zimess);

        currentUser = ParseUser.getCurrentUser();

        inicializarCompUI();

        findZimessCurrentUser();

    }

    private void inicializarCompUI() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        //Zimess no Found
        layoudZimessNoFound = (LinearLayout) findViewById(R.id.layoudZimessNoFound);
        ImageView imageView = (ImageView) findViewById(R.id.imgIconZimessNoFound);
        //imageView.setImageResource(R.drawable.ic_icon_radar_gray);
        TextView textView = (TextView) findViewById(R.id.lblMyZimessNoFound);
        textView.setText(R.string.lblMyZimessNoFound);

        layoudZimessFinder = (LinearLayout) findViewById(R.id.layoudZimessFinder);
        recyclerView = (RecyclerView) findViewById(R.id.listZMessages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        layoudZimessFinder.setVisibility(View.GONE);
    }


    /**
     * Busca los Zimess del Usuario
     */
    private void findZimessCurrentUser() {
        Location currentLocation = getCurrentLocation();
        if (currentLocation != null) {
            new RefreshDataZimessTask(this, currentUser, currentLocation, recyclerView, layoudZimessNoFound, layoudZimessFinder).execute();
        }
    }

    /**
     * retorna la Ubicacion actual
     *
     * @return
     */
    private Location getCurrentLocation() {
        Location location = null;
        if (LocationService.isRunning()) {
            LocationService locationService = LocationService.getInstance();
            android.location.Location tmpLocation = locationService.getCurrentLocation(true);
            location = new Location(tmpLocation.getLatitude(), tmpLocation.getLongitude());
        }
        return location;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_my_zimess, menu);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == requestCodeUpdateZimess && data != null) {
            boolean updateZimessOk = data.getBooleanExtra("updateZimessOk", false);
            if (resultCode == Activity.RESULT_OK && updateZimessOk)
                findZimessCurrentUser();
        }
    }
}
