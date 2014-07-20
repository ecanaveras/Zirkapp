package com.ecp.gsy.dcs.zirkapp.app.util;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.ecp.gsy.dcs.zirkapp.app.fragments.Fhome;
import com.ecp.gsy.dcs.zirkapp.app.fragments.Fmessages;

/**
 * Created by Elder on 16/07/2014.
 */
public class ScreenSlidePagerAdapter extends FragmentPagerAdapter {

    SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();
    private String[] titles = {"Home","Activiy"};

    public ScreenSlidePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        //Bundle agrs = new Bundle();
        switch (position) {
            case 0:
                return new Fhome();
            case 1:
                return new Fmessages();
            case 2:
                //fragment = new Amygos();
                break;
        }
        return null;
    }

    @Override
    public int getCount() {
        return titles.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position].toUpperCase();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment1 = (Fragment) super.instantiateItem(container, position);
        registeredFragments.put(position, fragment1);
        return fragment1;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        registeredFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    public Fragment getRegisteredFragment(int position){
        return  registeredFragments.get(position);
    }
}
