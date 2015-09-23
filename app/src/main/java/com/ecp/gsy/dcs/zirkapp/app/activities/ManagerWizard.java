package com.ecp.gsy.dcs.zirkapp.app.activities;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.fragments.wizard.WizardFirstFragment;
import com.ecp.gsy.dcs.zirkapp.app.fragments.wizard.WizardSecondFragment;
import com.ecp.gsy.dcs.zirkapp.app.fragments.wizard.WizardThirdFragment;
import com.parse.ParseUser;

/**
 * Created by ecanaveras on 22/09/2015.
 */
public class ManagerWizard extends Activity {

    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    public ViewPager mViewPager;

    private ParseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wizard);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
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
                    return "Otra Info".toUpperCase();
                case 2:
                    return "Perfil social".toUpperCase();
            }
            return null;
        }
    }
}
