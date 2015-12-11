package com.ecp.gsy.dcs.zirkapp.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ecp.gsy.dcs.zirkapp.app.GlobalApplication;
import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.adapters.ZimessRecyclerAdapter;
import com.ecp.gsy.dcs.zirkapp.app.util.locations.Location;
import com.ecp.gsy.dcs.zirkapp.app.util.services.LocationService;
import com.ecp.gsy.dcs.zirkapp.app.util.task.RefreshDataZimessTask;
import com.parse.ParseUser;

/**
 * Created by Elder on 21/03/2015.
 */
public class MyZimessActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ZimessRecyclerAdapter zReciclerAdapter;
    private static MyZimessActivity instance;

    private LinearLayout layoutZimessNoFound, layoutZimessFinder, layoutZimessDefault;
    private ParseUser currentUser;
    private Toolbar toolbar;
    public int requestCodeUpdateZimess = 105;
    public int requestCodeDeleteZimess = 115;
    private GlobalApplication globalApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_zimess);

        globalApplication = (GlobalApplication) this.getApplicationContext();

        currentUser = ParseUser.getCurrentUser();

        inicializarCompUI();

        findZimessCurrentUser();

        instance = this;
    }

    public static boolean isRunning() {
        return instance != null;
    }

    public static MyZimessActivity getInstance() {
        return instance;
    }

    private void inicializarCompUI() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        //Layouts
        layoutZimessDefault = (LinearLayout) findViewById(R.id.layoutZimessDefault);
        layoutZimessNoFound = (LinearLayout) findViewById(R.id.layoutZimessNoFound);
        layoutZimessFinder = (LinearLayout) findViewById(R.id.layoutZimessFinder);

        ImageView imageView = (ImageView) findViewById(R.id.imgLogoZirkapp);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.fade);
        animation.setRepeatCount(5);
        imageView.startAnimation(animation);

        //Mensaje personalizado
        TextView textView = (TextView) findViewById(R.id.lblMyZimessNoFound);
        textView.setText(R.string.lblMyZimessNoFound);

        recyclerView = (RecyclerView) findViewById(R.id.listZMessages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        //recyclerView.setHasFixedSize(true);
    }


    /**
     * Busca los Zimess del Usuario
     */
    public void findZimessCurrentUser() {
        Location currentLocation = getCurrentLocation();
        if (currentLocation != null) {
            layoutZimessDefault.setVisibility(View.GONE);
            RefreshDataZimessTask dataZimessTask = new RefreshDataZimessTask(this, currentUser, currentLocation, recyclerView, zReciclerAdapter);
            dataZimessTask.setLayoutZimessNoFound(layoutZimessNoFound);
            dataZimessTask.setLayoutZimessFinder(layoutZimessFinder);
            dataZimessTask.execute();
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
            android.location.Location tmpLocation = locationService.getCurrentLocation();
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
        //getMenuInflater().inflate(R.menu.menu_activity_my_zimess, menu);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == requestCodeUpdateZimess && data != null) {
            boolean updateZimessOk = data.getBooleanExtra("updateZimessOk", false);
            if (resultCode == Activity.RESULT_OK && updateZimessOk)
                findZimessCurrentUser();
        }

        if (requestCode == requestCodeDeleteZimess && data != null) {
            boolean deleteZimessOk = data.getBooleanExtra("deleteZimessOk", false);
            if (resultCode == Activity.RESULT_OK && deleteZimessOk)
                findZimessCurrentUser();
        }
    }
}
