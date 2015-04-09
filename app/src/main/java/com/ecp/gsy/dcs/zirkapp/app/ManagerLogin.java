package com.ecp.gsy.dcs.zirkapp.app;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.Toast;


import com.ecp.gsy.dcs.zirkapp.app.fragments.loginSignup.LoginFragment;
import com.ecp.gsy.dcs.zirkapp.app.fragments.loginSignup.SignupFragment;
import com.ecp.gsy.dcs.zirkapp.app.util.beans.Welcomedb;
import com.ecp.gsy.dcs.zirkapp.app.util.database.DatabaseHelper;
import com.ecp.gsy.dcs.zirkapp.app.util.task.RegisterGcmTask;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.query.In;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;


public class ManagerLogin extends Activity {

    public SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    public ViewPager mViewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position + 1) {
                case 1:
                    return new LoginFragment();
                case 2:
                    return new SignupFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Ingresa".toUpperCase();
                case 1:
                    return "Registrate".toUpperCase();
            }
            return null;
        }
    }

}
