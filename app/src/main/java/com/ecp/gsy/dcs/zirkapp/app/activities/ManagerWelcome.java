package com.ecp.gsy.dcs.zirkapp.app.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.Toast;

import com.alertdialogpro.AlertDialogPro;
import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.fragments.welcome.WelcomeFirstFragment;
import com.ecp.gsy.dcs.zirkapp.app.fragments.welcome.WelcomeFourFragment;
import com.ecp.gsy.dcs.zirkapp.app.fragments.welcome.WelcomeSecondFragment;
import com.ecp.gsy.dcs.zirkapp.app.fragments.welcome.WelcomeThirdFragment;
import com.ecp.gsy.dcs.zirkapp.app.util.beans.HandlerLogindb;
import com.ecp.gsy.dcs.zirkapp.app.util.beans.Welcomedb;
import com.ecp.gsy.dcs.zirkapp.app.util.database.DatabaseHelper;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.parse.ParseUser;
import com.viewpagerindicator.CirclePageIndicator;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Elder on 30/05/2014.
 */
public class ManagerWelcome extends Activity {


    //Usuario de Parse
    private ParseUser currentUser;

    //Respuesta del Login
    private int inputLoginRequestCode = 100;

    private boolean runWelcome = true;

    //Database Local
    private DatabaseHelper databaseHelper;


    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        //Database
        databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        CirclePageIndicator circlePageIndicator = (CirclePageIndicator) findViewById(R.id.indicator);
        circlePageIndicator.setViewPager(mViewPager);

    }


    @Override
    public void onBackPressed() {
        showMessageExitApp();
    }

    private void showMessageExitApp() {
        AlertDialogPro.Builder alertDialogBuilder = new AlertDialogPro.Builder(this);
        alertDialogBuilder.setTitle(getResources().getString(R.string.msgExitApp));
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(R.string.msgYes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        moveTaskToBack(true);
                        android.os.Process.killProcess(android.os.Process.myPid());
                        System.exit(1);
                    }
                })
                .setNegativeButton(getResources().getString(R.string.msgNo), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    /**
     * Comprueba si existen datos en Welcomedb
     */
    private void findDataLocalDb() {
        List<Welcomedb> listWdb = new ArrayList<Welcomedb>();
        List<HandlerLogindb> listHldb = new ArrayList<>();

        try {
            Dao dao = null;
            dao = databaseHelper.getWelcomedbDao();
            listWdb = dao.queryForAll();
        } catch (SQLException e) {
            Log.e("Ormlite", "Error buscando welcome");
        }

        try {
            Dao daoL = null;
            daoL = databaseHelper.getHandlerLogindbDao();
            listHldb = daoL.queryForAll();
        } catch (SQLException e) {
            Log.e("Ormlite", "Error buscando handkerLogin");
        }

        //Si existe un registro de welcolme, no se mostrará la pantalla de bienvenida
        for (Welcomedb w : listWdb) {
            runWelcome = false;
            break;
        }

        //Se verifica si la session está activa
        boolean isSessionActive = false;
        for (HandlerLogindb row : listHldb) {
            isSessionActive = row.isSessionActive();
            Log.i("session.state.db", String.valueOf(row.isSessionActive()));
            break;
        }
        if (!runWelcome) {
            currentUser = ParseUser.getCurrentUser();
            if (currentUser != null && isSessionActive) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else if (!isSessionActive) {
                Intent login = new Intent(this, ManagerLogin.class);
                login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                login.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(login, inputLoginRequestCode);
            }
        }
    }

    @Override
    protected void onStart() {
        findDataLocalDb();
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        if (databaseHelper != null) {
            OpenHelperManager.releaseHelper();
            databaseHelper = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Respuesta del Login
        if (requestCode == inputLoginRequestCode && data != null) {
            boolean loginOk = data.getBooleanExtra("loginOk", false);
            if (resultCode == RESULT_OK && loginOk) {
                currentUser = ParseUser.getCurrentUser();
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(getApplicationContext(),
                        "No ha sido posible loguearse",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new WelcomeFirstFragment();
                case 1:
                    return new WelcomeSecondFragment();
                case 2:
                    return new WelcomeThirdFragment();
                case 3:
                    return new WelcomeFourFragment();

            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 4 total pages.
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                default:
                    return "Zirkapp W" + String.valueOf(position);
            }
        }
    }
}
