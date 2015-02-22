package com.ecp.gsy.dcs.zirkapp.app.util;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentActivity;

import com.ecp.gsy.dcs.zirkapp.app.fragments.Fhome;
import com.ecp.gsy.dcs.zirkapp.app.fragments.Finbox;
import com.ecp.gsy.dcs.zirkapp.app.fragments.Fzimess;

/**
 * Created by Elder on 16/07/2014.
 */
public class ScreenSlidePagerAdapter extends FragmentPagerAdapter {

    private Fzimess fzimess;

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
                if(fzimess == null) {
                    fzimess = new Fzimess();
                }
                return fzimess;
            case 2:
                return new Finbox();
        }
        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }

}
