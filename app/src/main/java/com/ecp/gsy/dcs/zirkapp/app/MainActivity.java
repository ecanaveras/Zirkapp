package com.ecp.gsy.dcs.zirkapp.app;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ecp.gsy.dcs.zirkapp.app.fragments.NotificationsFragment;
import com.ecp.gsy.dcs.zirkapp.app.util.adapters.NavigationAdapter;
import com.ecp.gsy.dcs.zirkapp.app.util.beans.ItemListDrawer;
import com.ecp.gsy.dcs.zirkapp.app.util.services.LocationService;
import com.ecp.gsy.dcs.zirkapp.app.util.services.MessageService;
import com.ecp.gsy.dcs.zirkapp.app.util.task.GlobalApplication;
import com.ecp.gsy.dcs.zirkapp.app.util.task.RefreshDataProfileTask;
import com.ecp.gsy.dcs.zirkapp.app.util.task.RegisterGcmTask;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.parse.ParseException;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;

public class MainActivity extends ActionBarActivity {

    //KEY FRAGMENT
    private static final int HOME = 0; //Disabled
    private static final int ZIMESS = 0;
    private static final int CHAT = 1;
    private static final int NOTI = 2;

    //TOTAL FRAGMENTS
    private static final int FRAGMENT_COUNT = 3;

    //ARRAY FRAGMENTS
    private Fragment[] fragments = new Fragment[FRAGMENT_COUNT];

    //Toolbar
    private Toolbar toolbar;
    //Drawer
    private DrawerLayout drawerNavigation;
    private ActionBarDrawerToggle drawerToggle;
    private ListView navListView;
    private String[] navTitles;
    private TypedArray navIcons;
    private ArrayList<ItemListDrawer> navItems;
    private NavigationAdapter navAdapter;
    private View headerDrawer;
    private ImageView avatar;
    //Fragments
    private int indexBackOrDefaultFragment;
    //Usuario de Parse
    private ParseUser currentUser = null;
    //Respuesta del welcome
    private int inputWelcomeRequestCode = 10;
    //Respuesta del edit profile
    private int inputEditProfileRequestCode = 20;

    GlobalApplication globalApplication;

    //GCM
    private GoogleCloudMessaging gcm;
    private String regId;
    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Iniciar servicio de ubicacion
        Intent intentService = new Intent(this, LocationService.class);
        startService(intentService);

        globalApplication = (GlobalApplication) getApplicationContext();

        //User Parse
        currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            new RegisterGcmTask(gcm, this).execute();
            ParsePush.subscribeInBackground("", new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        Log.d("com.parse.push", "successfully subscribed to the broadcast channel.");
                    } else {
                        Log.e("com.parse.push", "failed to subscribe for push", e);
                    }
                }
            });
        }

        //Manipulando Fragments
        FragmentManager fm = getFragmentManager();
        //fragments[HOME] = fm.findFragmentById(R.id.fhome);
        fragments[ZIMESS] = fm.findFragmentById(R.id.f_zimess);
        fragments[CHAT] = fm.findFragmentById(R.id.f_usuariosOnline);
        fragments[NOTI] = fm.findFragmentById(R.id.f_notificaciones);

        FragmentTransaction ft = fm.beginTransaction();
        for (int i = 0; i < fragments.length; i++) {
            ft.hide(fragments[i]);
        }
        ft.commit();

        //Fragment a Mostrar en caso de un CALL a la Activity
        indexBackOrDefaultFragment = getIntent().getIntExtra("posicion", 1);

        //Crea el menú Lateral
        createOrUpdateDrawer();

        //Fragment por Default
        if (savedInstanceState == null) {
            selectItemDrawer(0);
        } else {
            selectItemDrawer(indexBackOrDefaultFragment);
        }

        if (currentUser != null)
            refreshDatosDrawer();
    }


    private void createOrUpdateDrawer() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        //Layout Header Y Footer para la lista en Drawer
        headerDrawer = getLayoutInflater().inflate(R.layout.header_drawer_menu, null);
        //Config Edid Profile
        headerDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), EditProfileActivity.class);
                startActivityForResult(intent, inputEditProfileRequestCode);
            }
        });

        //Lista de Navegacion
        navListView = (ListView) findViewById(R.id.left_drawer);
        //Establecemos el header
        navListView.addHeaderView(headerDrawer);
        //Crea un nuevo Navigation Adapter
        refreshDrawerAdapter();

        navListView.setOnItemClickListener(new DrawerItemClickListener());
        drawerNavigation = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(this, drawerNavigation, toolbar, R.string.app_name, R.string.lblCancel) {
            @Override
            public void onDrawerOpened(View drawerView) {
                toolbar.setTitle(R.string.app_name);
                refreshDrawerAdapter();
                invalidateOptionsMenu();
                syncState();
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                invalidateOptionsMenu();
                syncState();
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                if (slideOffset < 0.6)
                    toolbar.setAlpha(1 - slideOffset);
            }
        };
        drawerToggle.setDrawerIndicatorEnabled(true);
        drawerNavigation.setDrawerListener(drawerToggle);
        drawerToggle.syncState();
    }

    private void refreshDrawerAdapter() {
        //UI
        //Obtener los titulos para el Drawer
        navTitles = getResources().getStringArray(R.array.options_drawer);
        //Obetner las url de las imagenes para el Drawer
        navIcons = getResources().obtainTypedArray(R.array.navigations_icons);

        //Listado de titulos e iconos  para el Drawer
        navItems = new ArrayList<ItemListDrawer>();
        //Zimess
        navItems.add(new ItemListDrawer(navTitles[0], navIcons.getResourceId(0, -1), GlobalApplication.getCantZimess()));
        //Chat
        navItems.add(new ItemListDrawer(navTitles[1], navIcons.getResourceId(1, -1), GlobalApplication.getCantUsersOnline()));
        //Notificaciones
        navItems.add(new ItemListDrawer(navTitles[2], navIcons.getResourceId(2, -1), GlobalApplication.getCantNotifications()));
        //Configurar
        navItems.add(new ItemListDrawer(navTitles[3], navIcons.getResourceId(3, -1), "Opciones".toUpperCase()));
        //Compartir Zirkapp
        navItems.add(new ItemListDrawer(navTitles[4], navIcons.getResourceId(4, -1), "Apoyanos".toUpperCase()));
        //Calificar Zirkapp
        navItems.add(new ItemListDrawer(navTitles[5], navIcons.getResourceId(5, -1)));

        //Adapter
        navAdapter = new NavigationAdapter(this, navItems);

        navListView.setAdapter(navAdapter);
    }

    private void refreshDatosDrawer() {
        //Personalizar el header.
        avatar = (ImageView) headerDrawer.findViewById(R.id.imgAvatar);
        TextView lblUsername = (TextView) headerDrawer.findViewById(R.id.lblUserName);
        TextView lblNombreUsuario = (TextView) headerDrawer.findViewById(R.id.lblNombreUsuario);
        TextView lblUsermail = (TextView) headerDrawer.findViewById(R.id.lblUserEmail);
        lblUsername.setText(currentUser.getUsername());
        lblUsermail.setText(currentUser.getEmail());
        lblNombreUsuario.setText(currentUser.getString("name"));
        avatar.setImageDrawable(GlobalApplication.getAvatar(currentUser));
        //Buscar en segundo plano
        new RefreshDataProfileTask(avatar, lblNombreUsuario).execute(currentUser); //TODO psoiblemente no necesario
    }


    /**
     * Reemplaza el contenido principal del Drawer
     *
     * @param position
     */
    public void selectItemDrawer(int position) {
        //Reemplazar el content_frame
        //fragmentManager.beginTransaction().replace(R.id.content_frame, fragmentAdapter.getItem(position)).commit();
        invalidateOptionsMenu();
        switch (position) {
            case 0:
                if (getIntent().getAction() != null && getIntent().getAction().equals("OPEN_FRAGMENT_USER")) {
                    toolbar.setTitle(R.string.title_fragment_chat);
                    showFragment(CHAT, false);
                } else {
                    toolbar.setTitle(R.string.title_fragment_zimess);
                    showFragment(ZIMESS, false);
                }
                break;
            case 1:
                toolbar.setTitle(R.string.title_fragment_zimess);
                showFragment(ZIMESS, false);
                break;
            case 2:
                toolbar.setTitle(R.string.title_fragment_chat);
                showFragment(CHAT, false);
                break;
            case 3:
                toolbar.setTitle(R.string.title_fragment_notifications);
                showFragment(NOTI, false);
                break;
            case 4: //Ajustes
                Intent intent = new Intent(this, CustomSettingsActivity.class);
                startActivity(intent);
                break;
            case 5: //Calificar
                //Log.i("package.name", this.getApplicationContext().getPackageName());
                Uri uri = Uri.parse("market://details?id=" + this.getApplicationContext().getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.urlPlayStore))));
                }
                break;
            case 6: //Compartir
                String msg1 = getResources().getString(R.string.msgShareApp);
                String urlPS = getResources().getString(R.string.urlPlayStore);
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, String.format("%s: %s", msg1, urlPS));
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.msgShareTo)));
                break;
//            default:
//                showFragment(ZIMESS, false);
//                break;
        }
        //Establece la posicion
        navListView.setItemChecked(position, true);
        //actionBar.setTitle(position > 0 ? navTitles[position - 1] : navTitles[0]);
        drawerNavigation.closeDrawer(navListView);

    }

    private void showFragment(int indexFragment, boolean addToBackStack) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        for (int i = 0; i < fragments.length; i++) {
            if (i == indexFragment) {
                ft.show(fragments[i]);
            } else {
                ft.hide(fragments[i]);
            }
        }
        if (addToBackStack) {
            ft.addToBackStack(null);
        }
        ft.commit();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getAction() != null) {
            if (intent.getAction().equals("OPEN_FRAGMENT_USER") && globalApplication.getCustomParseUser() != null) {
                //globalApplication.setCustomParseUser(parseUserDestino); Seteado en la notifcacion
                Intent intent1 = new Intent(this, MessagingActivity.class);
                startActivity(intent1);
                //showFragment(CHAT, false);
            }
            if (intent.getAction().equals("OPEN_FRAGMENT_NOTI")) {
                NotificationsFragment frag = (NotificationsFragment) fragments[NOTI];
                frag.findNotifications(currentUser);
                showFragment(NOTI, false);
            }
            Log.d("onNewIntent", intent.getAction());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //globalApplication.checkPlayServices(this);
        globalApplication.setListeningNotifi(false);

        //FACEBOOK
        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        globalApplication.setListeningNotifi(true);

        //FACEBOOK
        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        drawerToggle.syncState();
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        drawerToggle.onConfigurationChanged(newConfig);
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean drawerOpen = drawerNavigation.isDrawerOpen(navListView);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        //Mis acciones
        switch (item.getItemId()) {
            default:
                break;
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
        globalApplication.setCustomParseUser(null);
        stopService(new Intent(this, MessageService.class));
        super.onDestroy();
    }

    /**
     * CLASE PARA LISTVIEW CLICK
     */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            selectItemDrawer(position);
        }
    }
}

//Generar HashKey onCreate
//        try {
//            PackageInfo info = getPackageManager().getPackageInfo("com.ecp.gsy.dcs.zirkapp", PackageManager.GET_SIGNATURES);
//            for (Signature signature : info.signatures) {
//                MessageDigest md = MessageDigest.getInstance("SHA");
//                md.update(signature.toByteArray());
//                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
//            }
//        } catch (PackageManager.NameNotFoundException e) {
//            Log.e("KeyHash NameNotFoundException",e.getMessage());
//        } catch (NoSuchAlgorithmException e) {
//            Log.e("KeyHash NoSuchAlgorithmException",e.getMessage());
//        }