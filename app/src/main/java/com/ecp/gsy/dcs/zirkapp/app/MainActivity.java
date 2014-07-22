package com.ecp.gsy.dcs.zirkapp.app;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;

import com.ecp.gsy.dcs.zirkapp.app.util.ScreenSlidePagerAdapter;


public class MainActivity extends Activity implements ActionBar.TabListener, ViewPager.OnPageChangeListener {

    //Contiene los frames
    private ViewPager mPager;
    private ScreenSlidePagerAdapter adapter;
    private ManagerWelcome managerWelcome;
    //private ActionBar actionBar;


    //Respuesta del welcome
    int inputRequestCode;
    boolean runWelcome = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try{
            runWelcome = savedInstanceState.getBoolean("runWelcome");
        }catch (Exception e){
            e.printStackTrace();
        }

        //Establece los Fragments del adapter al mPager
        mPager = (ViewPager) findViewById(R.id.pager);
        //actionBar = getActionBar();
        adapter = new ScreenSlidePagerAdapter(getFragmentManager());

        mPager.setAdapter(adapter);

        //Tabs
        //actionBar.setHomeButtonEnabled(false);
        //actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        //Agregando pestañas
        /*for(String nombre: titles){
            ActionBar.Tab tab = actionBar.newTab().setText(nombre);
            tab.setTabListener(this);
            actionBar.addTab(tab);
        }*/

        mPager.setOnPageChangeListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(runWelcome){
            initWelcome(true);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("runWelcome", runWelcome);
    }

    private void initWelcome(boolean run) {
        Intent intent = new Intent(this, ManagerWelcome.class);
        intent.putExtra("run", run);
        inputRequestCode = 10;
        startActivityForResult(intent, inputRequestCode);
    }

        @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == inputRequestCode) {
            if (resultCode == RESULT_OK) {
                runWelcome = false;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }



    //<editor-fold desc="METHOS VIEW CHANGE LISTENER">
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        //actionBar.setSelectedNavigationItem(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
    //</editor-fold>

    //<editor-fold desc="METHODS CHANGE TAB LISTENER">
    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
            mPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }
    //</editor-fold>


}
