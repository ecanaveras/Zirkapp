package com.ecp.gsy.dcs.zirkapp.app.util.adapters;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

import com.ecp.gsy.dcs.zirkapp.app.fragments.HomeFragment;
import com.ecp.gsy.dcs.zirkapp.app.fragments.UsersFragment;
import com.ecp.gsy.dcs.zirkapp.app.fragments.ZimessFragment;

/**
 * Created by Elder on 16/07/2014.
 */
public class ScreenSlidePagerAdapter extends FragmentPagerAdapter {

    private ZimessFragment fzimess;

    public ScreenSlidePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        //Bundle agrs = new Bundle();
        switch (position) {
            case 0:
                return new HomeFragment();
            case 1:
                if(fzimess == null) {
                    fzimess = new ZimessFragment();
                }
                return fzimess;
            case 2:
                return new UsersFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }

}
