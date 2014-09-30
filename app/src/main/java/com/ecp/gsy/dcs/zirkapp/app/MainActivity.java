package com.ecp.gsy.dcs.zirkapp.app;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.ecp.gsy.dcs.zirkapp.app.util.ScreenSlidePagerAdapter;
import com.ecp.gsy.dcs.zirkapp.app.util.adapters.AdapterNavigation;
import com.ecp.gsy.dcs.zirkapp.app.util.beans.ItemListDrawer;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.ArrayList;


public class MainActivity extends FragmentActivity {

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
    //Fragments
    private FragmentManager fragmentManager;
    private ScreenSlidePagerAdapter fragmentAdapter;
    private int indexBackOrDefaultFragment;


    private ActionBar actionBar;

    private ManagerWelcome managerWelcome;

    //User login parse
    private boolean signUpParse;
    String user = "zuser1";
    String pass = "12345";


    //Respuesta del welcome
    int inputRequestCode;
    boolean runWelcome = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Manipulando Fragments
        FragmentManager fm = getFragmentManager();
        fragments[HOME] = fm.findFragmentById(R.id.fhome);
        fragments[ZIMESS] = fm.findFragmentById(R.id.fzimess);
        fragments[INBOX] = fm.findFragmentById(R.id.finbox);

        FragmentTransaction ft = fm.beginTransaction();
        for (int i = 0; i < fragments.length; i++) {
            ft.hide(fragments[i]);
        }
        ft.commit();

        //Fragment a Mostrar en caso de un CALL a la Activity
        indexBackOrDefaultFragment = getIntent().getIntExtra("posicion", 1);

//        //Iniciar Push
//        Parse.initialize(this, "VDJEJIMbyuOiis9bwBHmrOIc7XDUqYHQ0TMhA23c",
//                "9EJKzvp4LhRdnLqfH6jkHPaWd58IVXaBKAWdeItE");
//        PushService.setDefaultPushCallback(this, MainActivity.class);
//        ParseInstallation.getCurrentInstallation().saveInBackground();
//
//        ParseAnalytics.trackAppOpened(getIntent());
//
//        if (!loginParse()) {
//            signUpParse();
//            loginParse();
//        }


        try {
//            runWelcome = savedInstanceState.getBoolean("runWelcome");
        } catch (Exception e) {
            e.printStackTrace();
        }

        //UI
        //Obtener los titulos para el Drawer
        navTitles = getResources().getStringArray(R.array.options_drawer);
        //Obetner las url de las imagenes para el Drawer
        navIcons = getResources().obtainTypedArray(R.array.navigations_icons);
        //Listado de titulos e iconos  para el Drawer
        navItems = new ArrayList<ItemListDrawer>();
        //Zimess
        navItems.add(new ItemListDrawer(navTitles[1], navIcons.getResourceId(2, -1))); //TODO Corregir imagenes Drawer
        //Inbox
        navItems.add(new ItemListDrawer(navTitles[2], navIcons.getResourceId(2, -1)));
        //Lista de Navegacion
        navListView = (ListView) findViewById(R.id.left_drawer);
        //Adapter
        navAdapter = new AdapterNavigation(this, navItems);
        //Layout Header para la lista en Drawer
        View header = getLayoutInflater().inflate(R.layout.header_drawer_menu, null);
        //Establecemos el header
        navListView.addHeaderView(header);
        //navListView.setAdapter(new ArrayAdapter<String>(this, (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? android.R.layout.simple_list_item_activated_1 : android.R.layout.simple_list_item_checked), navTitles));
        //Establecemos el adapter a la lista
        navListView.setAdapter(navAdapter);
        navListView.setOnItemClickListener(new DrawerItemClickListener());
        drawerNavigation = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(this, drawerNavigation, R.drawable.ic_drawer, R.string.app_name, R.string.lblCancel) {
            @Override
            public void onDrawerOpened(View drawerView) {
                actionBar.setTitle(R.string.app_name);
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

        //Fragment por Default
        if (savedInstanceState == null) {
            selectItemDrawer(0);
        } else {
            selectItemDrawer(indexBackOrDefaultFragment);
        }
    }

    /**
     * Reemplaza el contenido principal del Drawer
     *
     * @param position
     */
    public void selectItemDrawer(int position) {
        //Reemplazar el content_frame
        //fragmentManager.beginTransaction().replace(R.id.content_frame, fragmentAdapter.getItem(position)).commit();
        switch (position){
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

    private void showFragment(int indexFragment, boolean addToBackStack){
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        for (int i = 0; i < fragments.length; i++) {
            if(i == indexFragment){
                ft.show(fragments[i]);
//                if(Session.getActiveSession().isClosed()){
//                    drawerNavigation.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
//                }
            }else {
                ft.hide(fragments[i]);
            }
        }
        if(addToBackStack){
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
        inputRequestCode = 10;
        startActivityForResult(intent, inputRequestCode);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean drawerOpen = drawerNavigation.isDrawerOpen(navListView);
        menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
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
        super.onStart();
        //TODO Consultar base de datos para indicar si se iniciar el welcome
        if (runWelcome) {
            //initWelcome(true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == inputRequestCode) {
            if (resultCode == RESULT_OK) {
                //TODO Guardar en base de datos
                runWelcome = false;
            }
        }
    }


    /**
     * PARSE
     */
    private void signUpParse() {
        ParseUser parseUser = new ParseUser();
        parseUser.setUsername(user);
        parseUser.setPassword(pass);
        parseUser.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Toast.makeText(getApplicationContext(), "No login in parse", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * PARSE
     *
     * @return
     */
    private boolean loginParse() {
        final boolean[] done = {false};
        ParseUser.logInInBackground(user, pass, new LogInCallback() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                if (parseUser == null) {
                    Toast.makeText(getApplicationContext(), "No login in parse", Toast.LENGTH_SHORT).show();
                    done[0] = false;
                } else {
                    done[0] = true;
                }
            }
        });
        return done[0];
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
