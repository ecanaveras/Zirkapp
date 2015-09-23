package com.ecp.gsy.dcs.zirkapp.app.activities;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.fragments.loginSignup.LoginFragment;
import com.ecp.gsy.dcs.zirkapp.app.fragments.loginSignup.SignupFragment;
//import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;


public class ManagerLogin extends Activity {

    public SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    public ViewPager mViewPager;
    private boolean isLogout;
    private Fragment loginFragment;
    private Fragment signupFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        isLogout = getIntent().getBooleanExtra("logout", false);
        if (isLogout) {
            //finishMainActivity();
        }

        loginFragment = new LoginFragment();
        signupFragment = new SignupFragment();

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

    }


    @Override
    public void onBackPressed() {
        invalidateOptionsMenu();
        if (isLogout || ParseUser.getCurrentUser() == null) {
            finishMainActivity();
            finish();
            moveTaskToBack(true);
            System.exit(0);
        }
        super.onBackPressed();
    }

    private void finishMainActivity() {
        if (MainActivity.instance != null) {
            MainActivity activity = MainActivity.instance;
            activity.finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.runFinalization();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (loginFragment != null)
            loginFragment.onActivityResult(requestCode, resultCode, data);

        if (signupFragment != null)
            signupFragment.onActivityResult(requestCode, resultCode, data);

        //ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
        /*for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            Fragment f = mSectionsPagerAdapter.getItem(i);
            if (f != null)
                f.onActivityResult(requestCode, resultCode, data);
        }*/
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return loginFragment;
                case 1:
                    return signupFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
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
