package com.ecp.gsy.dcs.zirkapp.app;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.TypedArray;
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
import android.widget.Toast;

import com.ecp.gsy.dcs.zirkapp.app.util.adapters.NavigationAdapter;
import com.ecp.gsy.dcs.zirkapp.app.util.adapters.ScreenSlidePagerAdapter;
import com.ecp.gsy.dcs.zirkapp.app.util.beans.ItemListDrawer;
import com.ecp.gsy.dcs.zirkapp.app.util.beans.Welcomedb;
import com.ecp.gsy.dcs.zirkapp.app.util.broadcast.UpdateDrawerReceiver;
import com.ecp.gsy.dcs.zirkapp.app.util.database.DatabaseHelper;
import com.ecp.gsy.dcs.zirkapp.app.util.services.MessageService;
import com.ecp.gsy.dcs.zirkapp.app.util.task.GlobalApplication;
import com.ecp.gsy.dcs.zirkapp.app.util.task.RefreshDataProfileTask;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.parse.ParseException;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActionBarActivity { // extends OrmLiteBaseActivity<DatabaseHelper> {

    //KEY FRAGMENT
    private static final int HOME = 0; //Disabled
    private static final int ZIMESS = 0;
    private static final int USERS = 1;
    //TOTAL FRAGMENTS
    private static final int FRAGMENT_COUNT = 2;
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

    private UpdateDrawerReceiver receiver;

    //Fragments
    private FragmentManager fragmentManager;
    private ScreenSlidePagerAdapter fragmentAdapter;
    private int indexBackOrDefaultFragment;
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

    private DatabaseHelper databaseHelper;

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

        //Database
        databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);


        //Manipulando Fragments
        FragmentManager fm = getFragmentManager();
        //fragments[HOME] = fm.findFragmentById(R.id.fhome);
        fragments[ZIMESS] = fm.findFragmentById(R.id.fzimessNew);
        fragments[USERS] = fm.findFragmentById(R.id.finbox);

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
            initSinchService(); //TODO MENSAJERIA DISABLED
            refreshDatosDrawer();
        }
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

    private void initSinchService() {
        final Intent serviceIntent = new Intent(getApplicationContext(), MessageService.class);
        startService(serviceIntent);
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
        navItems.add(new ItemListDrawer(navTitles[2], navIcons.getResourceId(2, -1)));
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
        lblUsername.setText(userZirkapp.getUsername());
        lblUsermail.setText(userZirkapp.getEmail());
        lblNombreUsuario.setText(userZirkapp.getString("name"));
        avatar.setImageBitmap(GlobalApplication.getAvatar(userZirkapp));
        //Buscar en segundo plano
        new RefreshDataProfileTask(avatar, lblNombreUsuario).execute(userZirkapp); //TODO psoiblemente no necesario
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
                showFragment(ZIMESS, false);
                break;
            case 1:
                showFragment(ZIMESS, false);
                break;
            case 2:
                showFragment(USERS, false);
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
                //Todo comprobar funcionamiento adecuado
                /*if (indexFragment == 1) { //Actualizar listado de Zimess
                    ZimessFragment zimessFragment = (ZimessFragment) fragments[i];
                    zimessFragment.findZimessAround();
                }
                if (indexFragment == 2) { //Actualizar listado de usuarios en el chat
                    UsersOnlineFragment usersOnlineFragment = (UsersOnlineFragment) fragments[i];
                    usersOnlineFragment.buscarUsuariosOnline();
                }*/
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
     * Comprueba si existen datos en Welcomedb
     */
    private void findDataWelcome() {
        List<Welcomedb> listWdb = new ArrayList<Welcomedb>();

        RuntimeExceptionDao<Welcomedb, Integer> dao = databaseHelper.getWelcomedbRuntimeDao();
        listWdb = dao.queryForAll();

        //Si existe un registro de welcolme, no se mostrará la pantalla de bienvenida
        for (Welcomedb w : listWdb) {
            runWelcome = false;
        }
        if (runWelcome) {
            Intent intent = new Intent(this, ManagerWelcome.class);
            intent.putExtra("run", runWelcome);
            startActivityForResult(intent, inputWelcomeRequestCode);
        }
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
        //Verificamos si el welcome fue visto.
        findDataWelcome();
        super.onStart();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Respuesta del Welcome
        if (requestCode == inputWelcomeRequestCode) { //Welcome
            boolean goLogin = data.getBooleanExtra("goLogin", false);
            if (resultCode == RESULT_OK && goLogin) {
                //Creamos un resgitro indicando que ya el welcome fue visto
                Welcomedb wdb = new Welcomedb("SI");
                RuntimeExceptionDao<Welcomedb, Integer> dao = databaseHelper.getWelcomedbRuntimeDao();
                dao.create(wdb);
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
        if (databaseHelper != null) {
            OpenHelperManager.releaseHelper();
            databaseHelper = null;
        }
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
