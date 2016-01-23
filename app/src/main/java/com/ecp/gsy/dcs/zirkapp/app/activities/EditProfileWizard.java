package com.ecp.gsy.dcs.zirkapp.app.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.fragments.wizard.WizardFirstFragment;
import com.ecp.gsy.dcs.zirkapp.app.fragments.wizard.WizardSecondFragment;
import com.ecp.gsy.dcs.zirkapp.app.fragments.wizard.WizardThirdFragment;
import com.parse.ParseUser;

/**
 * Created by ecanaveras on 22/09/2015.
 */
public class EditProfileWizard extends AppCompatActivity {

    SectionsPagerAdapter mSectionsPagerAdapter;

    private boolean w1 = false;
    private boolean w2 = false;
    private boolean w3 = false;


    /**
     * The {@link ViewPager} that will host the section contents.
     */
    public ViewPager mViewPager;

    private ParseUser currentUser;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile_wizard);

        initComponentsUI();
    }

    private void initComponentsUI() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.title_activity_my_account));
        toolbar.setLogo(R.drawable.ic_account_circle_white_24dp);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
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
    public void onBackPressed() {
        /*if (w1 || w2 || w3) {
            Toast.makeText(this, "Hey, fijate si finalizaste esta parte!", Toast.LENGTH_LONG).show();
            return;
        }*/

        super.onBackPressed();
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new WizardFirstFragment();
                case 1:
                    return new WizardSecondFragment();
                case 2:
                    return new WizardThirdFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Datos de Perfil".toUpperCase();
                case 1:
                    return "Foto y Estado".toUpperCase();
                case 2:
                    return "Perfil social".toUpperCase();
            }
            return null;
        }
    }

    public boolean isW1() {
        return w1;
    }

    public void setW1(boolean w1) {
        this.w1 = w1;
    }

    public boolean isW2() {
        return w2;
    }

    public void setW2(boolean w2) {
        this.w2 = w2;
    }
}
