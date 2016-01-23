package com.ecp.gsy.dcs.zirkapp.app.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ecp.gsy.dcs.zirkapp.app.GlobalApplication;
import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.fragments.MainFragment;
import com.ecp.gsy.dcs.zirkapp.app.fragments.NotificationsFragment;
import com.ecp.gsy.dcs.zirkapp.app.util.listener.FragmentIterationListener;
import com.ecp.gsy.dcs.zirkapp.app.util.services.LocationService;
import com.ecp.gsy.dcs.zirkapp.app.util.services.SinchService;
import com.ecp.gsy.dcs.zirkapp.app.util.sinch.SinchBaseActivity;
import com.ecp.gsy.dcs.zirkapp.app.util.task.NavigationProfileTask;
import com.ecp.gsy.dcs.zirkapp.app.util.task.OpenMessagingTask;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.sinch.android.rtc.SinchError;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

//import com.facebook.appevents.AppEventsLogger;

public class MainActivity extends SinchBaseActivity implements SinchService.StartFailedListener, FragmentIterationListener {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static MainActivity instance = null;

    //Toolbar
    private Toolbar toolbar;
    //Drawer
    private DrawerLayout drawerNavigation;
    private NavigationView navigationView;

    private View headerDrawer;
    private ImageView avatar;

    private int indexBackOrDefaultFragment;
    //Usuario de Parse
    private ParseUser currentUser = null;
    //Respuesta del edit profile
    private int inputEditProfileRequestCode = 20;

    GlobalApplication globalApplication;

    //GCM
    private GoogleCloudMessaging gcm;
    private String regId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //KeyHash
        //this.getKeyHash();

        globalApplication = (GlobalApplication) getApplicationContext();
        globalApplication.setContext(this);

        //User Parse
        currentUser = ParseUser.getCurrentUser();

        if (currentUser != null) {
            globalApplication.storeParseInstallation();
            ParsePush.subscribeInBackground("");
        } else {
            //Login
            startActivity(new Intent(this, ManagerWelcome.class));
        }

        initComponentsUI();

        if (getIntent() != null) {
            gotoTarget(getIntent());
        }

        instance = this;
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i("GCM Services", "Dispositivo no soportado.");
                finish();
            }
            return false;
        }
        return true;
    }

    private void initComponentsUI() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);

        drawerNavigation = (DrawerLayout) findViewById(R.id.drawer_layout);

        navigationView = (NavigationView) findViewById(R.id.nav_view);

        headerDrawer = navigationView.inflateHeaderView(R.layout.header_drawer_menu);
        headerDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EditProfileWizard.class);
                startActivity(intent);
            }
        });

        //Seleccionar Zimess por default
        selectItemDrawer(navigationView.getMenu().getItem(0));

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                selectItemDrawer(item);
                drawerNavigation.closeDrawers();
                return false;
            }
        });
    }

    private void refreshDatosDrawer() {
        //Personalizar el header.
        if (headerDrawer != null && currentUser != null) {
            avatar = (ImageView) headerDrawer.findViewById(R.id.imgAvatar);
            TextView lblUsername = (TextView) headerDrawer.findViewById(R.id.lblUserName);
            TextView lblNombreUsuario = (TextView) headerDrawer.findViewById(R.id.lblNombreUsuario);
            TextView lblUsermail = (TextView) headerDrawer.findViewById(R.id.lblUserEmail);
            lblUsername.setText(currentUser.getUsername());
            lblUsermail.setText(currentUser.getEmail());
            lblNombreUsuario.setText(currentUser.getString("name"));
            globalApplication.setAvatarRoundedResize(currentUser.getParseFile("avatar"), avatar, 120, 120);
        }
    }

    /**
     * Reemplaza el contenido principal del Drawer
     *
     * @param itemDrawer
     */
    public void selectItemDrawer(MenuItem itemDrawer) {
        selectItemDrawer(itemDrawer, null);
    }

    /**
     * Reemplaza el contenido principal del Drawer
     *
     * @param itemDrawer
     */
    public void selectItemDrawer(MenuItem itemDrawer, Integer tabSelected) {
        Fragment fragmentSelected = null;
        FragmentManager fragmentManager = getFragmentManager();
        //Reemplazar el content_frame
        switch (itemDrawer.getItemId()) {
            case R.id.item_home:
                //Titulo del fragment
                setTitle(getString(R.string.app_name));
                itemDrawer.setChecked(true);
                Bundle bundle = new Bundle();
                if (tabSelected != null) {
                    bundle.putInt("tabSelected", tabSelected);
                }
                fragmentSelected = MainFragment.newInstance(bundle);
                break;
            case R.id.item_notifi:
                //Titulo del fragment
                setTitle(itemDrawer.getTitle());
                itemDrawer.setChecked(true);
                fragmentSelected = new NotificationsFragment();
                break;
            case R.id.item_profile:
                Intent wizard = new Intent(this, EditProfileWizard.class);
                startActivity(wizard);
                break;
            case R.id.item_ajust: //Ajustes
                Intent intent = new Intent(this, CustomSettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.item_quali: //Calificar
                //Log.i("package.name", this.getApplicationContext().getPackageName());
                Uri uri = Uri.parse("market://details?id=" + this.getApplicationContext().getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.urlPlayStore))));
                }
                break;
            case R.id.item_share: //Compartir
                String msg1 = getResources().getString(R.string.msgShareApp);
                String urlPS = getResources().getString(R.string.urlPlayStore);
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, String.format("%s: %s", msg1, urlPS));
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.msgShareTo)));
                break;
        }

        if (fragmentSelected != null) {
            fragmentManager.beginTransaction()
                    .replace(R.id.contenedor_principal, fragmentSelected)
                    .commit();
        }

    }

    private void gotoTarget(Intent intent) {
        if (intent.getAction() != null) {
            if (intent.getAction().equals("OPEN_FRAGMENT_GENTE_CERCA")) {
                //Gente cerca Fragment
                selectItemDrawer(navigationView.getMenu().getItem(0), 2);
            }
            if (intent.getAction().equals("OPEN_MESSAGING_USER")) {
                if (globalApplication.isConectedToInternet()) {
                    new OpenMessagingTask(MainActivity.this).execute(intent.getStringExtra("senderId"));
                }
                //Chat Fragment
                selectItemDrawer(navigationView.getMenu().getItem(0));
            }
            if (intent.getAction().equals("OPEN_FRAGMENT_NOTI")) {
                //Noti Fragment
                if (NotificationsFragment.isRunning()) {
                    NotificationsFragment n = NotificationsFragment.getInstance();
                    n.findNotifications(currentUser);
                }
                selectItemDrawer(navigationView.getMenu().getItem(1));
            }
            if (intent.getAction().equals("OPEN_PROFILE_USER")) {
                //Ir la perfil del usuario
                if (globalApplication.isConectedToInternet()) {
                    new NavigationProfileTask(MainActivity.this).execute(intent.getStringExtra("targetId"));
                } else {
                    //Chat Fragment
                    selectItemDrawer(navigationView.getMenu().getItem(0));
                }
            }
        } else {
            if (intent.getBooleanExtra("EXIT", false)) {
                finish();
                Intent intent1 = new Intent(Intent.ACTION_MAIN);
                intent1.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent1);
            }
        }

    }

    public MenuItem getNavItem(int index) {
        if (navigationView != null) {
            return navigationView.getMenu().getItem(index);
        }
        return null;
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        gotoTarget(intent);
    }

    @Override
    public void onFragmentIteration(Bundle params) {

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        refreshDatosDrawer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        globalApplication.setListeningNotifi(true);
    }

    @Override
    public void onBackPressed() {
        if (getNavItem(0).isChecked()) {
            super.onBackPressed();
        } else {
            //Ir a Zimess
            selectItemDrawer(getNavItem(0));
        }
        //moveTaskToBack(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerNavigation.openDrawer(GravityCompat.START);
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Respuesta del Edit Profile
        if (requestCode == inputEditProfileRequestCode && data != null) {
            drawerNavigation.closeDrawers();
            if (resultCode == RESULT_OK) {
                Boolean update = data.getBooleanExtra("editprofileOk", false);
                if (update) {
                    refreshDatosDrawer();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        globalApplication.setListeningNotifi(true);
        globalApplication.setMessagingParseUser(null);
        globalApplication.setProfileParseUser(null);
        stopService(new Intent(this, LocationService.class));
        if (getSinchServiceInterface() != null) {
            getSinchServiceInterface().stopClient();
        }
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStartFailed(SinchError error) {
        Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStarted() {
    }

    @Override
    protected void onServiceConnected() {
        if (currentUser != null && !getSinchServiceInterface().isStarted()) { //&& checkPlayServices()
            gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
            regId = globalApplication.getRegistrationId(getApplicationContext(), currentUser.getUsername());
            //Si regId no existe, Registrarlo
            if (regId.isEmpty()) {
                new AsyncTask<String, Void, String>() {

                    @Override
                    protected String doInBackground(String... params) {
                        String regId = "";
                        try {
                            //Obtenemos el id de la instalacion
                            regId = gcm.register(globalApplication.SENDER_ID);
                            //Guardamos los datos de la instalacion
                            globalApplication.storeRegistrationId(MainActivity.this, regId, currentUser.getUsername());
                            //Info del gcm id
                            Log.d("GCM regID", "Registrado en GCM: registration_id=" + regId);

                        } catch (IOException e) {
                            Log.e("Error registro GCM:", e.getMessage());
                        }

                        return regId;
                    }

                    @Override
                    protected void onPostExecute(String regId) {
                        if (GlobalApplication.isChatEnabled()) {
                            getSinchServiceInterface().startClient(currentUser.getObjectId(), regId);
                            getSinchServiceInterface().setStartListener(MainActivity.this);
                        }
                    }
                }.execute();
            } else {
                if (GlobalApplication.isChatEnabled()) {
                    getSinchServiceInterface().startClient(currentUser.getObjectId(), regId);
                    getSinchServiceInterface().setStartListener(MainActivity.this);
                }
            }

        }
    }

    private void getKeyHash() {
        //Generar HashKey onCreate
        try {
            PackageInfo info = getPackageManager().getPackageInfo("com.ecp.gsy.dcs.zirkapp", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("KeyHashNameNotFound", e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            Log.e("KeyHashNoSuchAlgorithm", e.getMessage());
        }
    }
}

