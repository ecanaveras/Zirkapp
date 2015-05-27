package com.ecp.gsy.dcs.zirkapp.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ecp.gsy.dcs.zirkapp.app.GlobalApplication;
import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.adapters.ZimessReciclerAdapter;
import com.ecp.gsy.dcs.zirkapp.app.util.beans.Zimess;
import com.ecp.gsy.dcs.zirkapp.app.util.listener.RecyclerItemListener;
import com.ecp.gsy.dcs.zirkapp.app.util.locations.Location;
import com.ecp.gsy.dcs.zirkapp.app.util.services.LocationService;
import com.ecp.gsy.dcs.zirkapp.app.util.task.RefreshDataZimessTask;
import com.parse.ParseUser;

import java.util.ArrayList;

/**
 * Created by Elder on 21/03/2015.
 */
public class MyZimessActivity extends ActionBarActivity {

    private RecyclerView recyclerView;
    private ZimessReciclerAdapter zReciclerAdapter;
    public ArrayList<Zimess> zimessList = new ArrayList<Zimess>();

    private LinearLayout layoutZimessNoFound;
    private LinearLayout layoutZimessFinder;
    private ParseUser currentUser;
    private Toolbar toolbar;
    public int requestCodeUpdateZimess = 105;
    private GlobalApplication globalApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_zimess);

        globalApplication = (GlobalApplication) this.getApplicationContext();

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
        layoutZimessNoFound = (LinearLayout) findViewById(R.id.layoutZimessNoFound);
        ImageView imageView = (ImageView) findViewById(R.id.imgIconZimessNoFound);
        TextView textView = (TextView) findViewById(R.id.lblMyZimessNoFound);
        textView.setText(R.string.lblMyZimessNoFound);

        layoutZimessFinder = (LinearLayout) findViewById(R.id.layoutZimessFinder);
        recyclerView = (RecyclerView) findViewById(R.id.listZMessages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addOnItemTouchListener(new RecyclerItemListener(this, new RecyclerItemListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (zimessList.size() > 0) {
                    final Zimess zimess = zimessList.get(position);

                    final View avatar = view.findViewById(R.id.imgAvatarItem);
                    avatar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //ir la perfil de usuario
                            String transitionName = getResources().getString(R.string.imgNameTransition);
                            ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(MyZimessActivity.this, avatar, transitionName);
                            Intent intent = new Intent(MyZimessActivity.this, UserProfileActivity.class);
                            globalApplication.setCustomParseUser(zimess.getUser());
                            ActivityCompat.startActivity(MyZimessActivity.this, intent, optionsCompat.toBundle());
                            return;
                        }
                    });

                    globalApplication.setTempZimess(zimess);
                    Intent intent = new Intent(MyZimessActivity.this, DetailZimessActivity.class);
                    startActivity(intent);
                } else {
                    Log.d("myZimessList", "empty");
                }
            }
        }));

        layoutZimessFinder.setVisibility(View.GONE);
    }


    /**
     * Busca los Zimess del Usuario
     */
    private void findZimessCurrentUser() {
        Location currentLocation = getCurrentLocation();
        if (currentLocation != null) {
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
