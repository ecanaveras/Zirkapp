package com.ecp.gsy.dcs.zirkapp.app;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ecp.gsy.dcs.zirkapp.app.util.broadcast.UpdateDrawerReceiver;
import com.ecp.gsy.dcs.zirkapp.app.util.database.DatabaseHelper;
import com.ecp.gsy.dcs.zirkapp.app.util.adapters.ScreenSlidePagerAdapter;
import com.ecp.gsy.dcs.zirkapp.app.util.beans.Welcomedb;
import com.ecp.gsy.dcs.zirkapp.app.util.adapters.AdapterNavigation;
import com.ecp.gsy.dcs.zirkapp.app.util.beans.ItemListDrawer;
import com.ecp.gsy.dcs.zirkapp.app.util.images.RoundedImageView;
import com.ecp.gsy.dcs.zirkapp.app.util.services.MessageService;
import com.ecp.gsy.dcs.zirkapp.app.util.task.RefreshDataProfileTask;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

import bolts.Task;

public class MainActivity extends OrmLiteBaseActivity<DatabaseHelper> {

    //KEY FRAGMENT
    private static final int HOME = 0;
    private static final int ZIMESS = 1;
    private static final int INBOX = 2;
    //TOTAL FRAGMENTS
    private static final int FRAGMENT_COUNT = 3;
    //ARRAY FRAGMENTS
    private Fragment[] fragments = new Fragment[FRAGMENT_COUNT];

    //Drawer
    private DrawerLayout drawerNavigation;
    private ActionBarDrawerToggle drawerToggle;
    private ListView navListView;
    private String[] navTitles;
    private TypedArray navIcons;
    private ArrayList<ItemListDrawer> navItems;
    private AdapterNavigation navAdapter;
    private View headerDrawer;
    private RoundedImageView avatar;

    private UpdateDrawerReceiver receiver;

    //Fragments
    private FragmentManager fragmentManager;
    private ScreenSlidePagerAdapter fragmentAdapter;
    private int indexBackOrDefaultFragment;

    private ActionBar actionBar;

    private ManagerWelcome managerWelcome;

    //Usuario de Parse
    private ParseUser userZirkapp = null;

    //Respuesta del welcome
    private int inputWelcomeRequestCode = 10;
    private boolean runWelcome = true;

    //Respuesta del edit profile
    private int inputEditProfileRequestCode = 20;

    //Respuesta del Login
    private int inputLoginRequestCode = 100;

    //Manejo de la DB
    private OrmLiteBaseActivity<DatabaseHelper> getOrlOrmLiteBaseActivity() {
        Activity activity = this;
        if (activity instanceof OrmLiteBaseActivity) {
            return (OrmLiteBaseActivity<DatabaseHelper>) activity;
        }
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Generar HashKey
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


        //Manipulando Fragments
        FragmentManager fm = getFragmentManager();
        fragments[HOME] = fm.findFragmentById(R.id.fhome);
        fragments[ZIMESS] = fm.findFragmentById(R.id.fzimessNew);
        fragments[INBOX] = fm.findFragmentById(R.id.finbox);

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

        //User Parse
        userZirkapp = ParseUser.getCurrentUser();
        if (userZirkapp == null) {
            Intent login = new Intent(this, ManagerLogin.class);
            startActivityForResult(login, inputLoginRequestCode);
        } else {
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
            //initSinchService(); //TODO MENSAJERIA DISABLED
            refreshDatosDrawer();
        }
    }

    private void createOrUpdateDrawer() {

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
        //Establecemos el Footer
        //navListView.addFooterView(footer);
        //navListView.setAdapter(new ArrayAdapter<String>(this, (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? android.R.layout.simple_list_item_activated_1 : android.R.layout.simple_list_item_checked), navTitles));
        //Establecemos el adapter a la lista
        //Crea un nuevo Navigation Adapter
        refreshDrawerAdapter();

        navListView.setOnItemClickListener(new DrawerItemClickListener());
        drawerNavigation = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(this, drawerNavigation, R.drawable.ic_drawer, R.string.app_name, R.string.lblCancel) {
            @Override
            public void onDrawerOpened(View drawerView) {
                actionBar.setTitle(R.string.app_name);
                //navAdapter.UpdateNotificacion("zimess", receiver.getCantRows());
                refreshDrawerAdapter();
                invalidateOptionsMenu();
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                invalidateOptionsMenu();
                super.onDrawerClosed(drawerView);
            }
        };
        drawerToggle.setDrawerIndicatorEnabled(true);
        drawerNavigation.setDrawerListener(drawerToggle);

        //Actions in Bar
        actionBar = getActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

    }

    private void initSinchService() {
        final Intent serviceIntent = new Intent(getApplicationContext(), MessageService.class);
        startService(serviceIntent);
    }

    private void refreshDrawerAdapter() {
//        navListView.setAdapter(null);
//        navAdapter = null;
        //UI
        //Obtener los titulos para el Drawer
        navTitles = getResources().getStringArray(R.array.options_drawer);
        //Obetner las url de las imagenes para el Drawer
        navIcons = getResources().obtainTypedArray(R.array.navigations_icons);
        //Listado de titulos e iconos  para el Drawer
        navItems = new ArrayList<ItemListDrawer>();
        //Zimess
        navItems.add(new ItemListDrawer(navTitles[1], navIcons.getResourceId(0, -1), receiver != null ? receiver.getCantRows() : null)); //TODO Corregir imagenes Drawer
        //Inbox
        navItems.add(new ItemListDrawer(navTitles[2], navIcons.getResourceId(1, -1), null));
        //Zimess_new
        navItems.add(new ItemListDrawer(navTitles[3], navIcons.getResourceId(2, -1), null)); //TODO Corregir imagenes Drawer
        //Adapter
        navAdapter = new AdapterNavigation(this, navItems);

        navListView.setAdapter(navAdapter);
    }

    private void refreshDatosDrawer() {
        //Personalizar el header.
        avatar = (RoundedImageView) headerDrawer.findViewById(R.id.imgAvatar);
        TextView lblUsername = (TextView) headerDrawer.findViewById(R.id.lblUserName);
        TextView lblUsermail = (TextView) headerDrawer.findViewById(R.id.lblUserEmail);
        lblUsername.setText(userZirkapp.getUsername());
        lblUsermail.setText(userZirkapp.getEmail());

        new RefreshDataProfileTask(avatar).execute(userZirkapp);
    }


    /**
     * Reemplaza el contenido principal del Drawer
     *
     * @param position
     */
    public void selectItemDrawer(int position) {
        //Reemplazar el content_frame
        //fragmentManager.beginTransaction().replace(R.id.content_frame, fragmentAdapter.getItem(position)).commit();
        switch (position) {
            case 0:
                showFragment(HOME, false);
                break;
            case 1:
                showFragment(ZIMESS, false);
                break;
            case 2:
                showFragment(INBOX, false);
                break;
        }
        //Establece la posicion
        navListView.setItemChecked(position, true);
        actionBar.setTitle(navTitles[position]);
        drawerNavigation.closeDrawer(navListView);
    }

    private void showFragment(int indexFragment, boolean addToBackStack) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        for (int i = 0; i < fragments.length; i++) {
            if (i == indexFragment) {
                ft.show(fragments[i]);
//                if(Session.getActiveSession().isClosed()){
//                    drawerNavigation.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
//                }
            } else {
                ft.hide(fragments[i]);
            }
        }
        if (addToBackStack) {
            ft.addToBackStack(null);
        }
        ft.commit();
    }

    /**
     * WELCOME
     *
     * @param run
     */
    private void initWelcome(boolean run) {
        Intent intent = new Intent(this, ManagerWelcome.class);
        intent.putExtra("run", run);
        startActivityForResult(intent, inputWelcomeRequestCode);
    }

    @Override
    protected void onResume() {
        receiver = new UpdateDrawerReceiver();
        registerReceiver(receiver, new IntentFilter("actualizarcantnotifi"));
        super.onResume();

    }

    @Override
    protected void onPause() {
        unregisterReceiver(receiver);
        super.onPause();
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
    protected void onStart() {
        List<Welcomedb> listWdb = new ArrayList<Welcomedb>();
        if (getOrlOrmLiteBaseActivity() != null) {
            DatabaseHelper helper = getOrlOrmLiteBaseActivity().getHelper();
            RuntimeExceptionDao<Welcomedb, Integer> dao = helper.getWelcomedbRuntimeDao();
            listWdb = dao.queryForAll();
        }
        //Si existe un registro de welcolme, no se mostrará la pantalla de bienvenida
        for (Welcomedb w : listWdb) {
            runWelcome = false;
            Log.i(MainActivity.class.getSimpleName(), "onStart() " + w.getRunWelcome());
        }
        if (runWelcome) {
            initWelcome(true);
        }
        super.onStart();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Respuesta del Welcome
        if (requestCode == inputWelcomeRequestCode) { //Welcome
            boolean goLogin = data.getBooleanExtra("goLogin", false);
            if (resultCode == RESULT_OK && goLogin) {
                Welcomedb wdb = new Welcomedb("SI");
                if (getOrlOrmLiteBaseActivity() != null) {
                    DatabaseHelper helper = getOrlOrmLiteBaseActivity().getHelper();
                    RuntimeExceptionDao<Welcomedb, Integer> dao = helper.getWelcomedbRuntimeDao();
                    dao.create(wdb);
                }
                runWelcome = false;
            }
        }
        //Respuesta del Login
        if (requestCode == inputLoginRequestCode) {
            boolean loginOk = data.getBooleanExtra("loginOk", false);
            if (resultCode == RESULT_OK && loginOk) {
                userZirkapp = ParseUser.getCurrentUser();
                initSinchService();
                refreshDatosDrawer();
            } else {
                Toast.makeText(getApplicationContext(),
                        "No ha sido posible loguearse",
                        Toast.LENGTH_LONG).show();
            }
        }

        if (requestCode == inputEditProfileRequestCode) {
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
