package com.ecp.gsy.dcs.zirkapp.app.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ecp.gsy.dcs.zirkapp.app.R;
import com.viewpagerindicator.TitlePageIndicator;

import java.util.ArrayList;

/**
 * Created by Elder on 11/04/2015.
 */
public class ChatFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        setHasOptionsMenu(true);

        final ArrayList<Fragment> fragmentArrayList = new ArrayList<>();
        final String[] titles = {"Online", "Historial"};

        fragmentArrayList.add(new UsersOnlineFragment());
        fragmentArrayList.add(new ChatHistoryFragment());

        ViewPager viewPager = (ViewPager) view.findViewById(R.id.pager);
        viewPager.setAdapter(new FragmentPagerAdapter(getFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return fragmentArrayList.get(position);
            }

            @Override
            public int getCount() {
                return fragmentArrayList.size();
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return titles[position];
            }
        });

        TitlePageIndicator titlePageIndicator = (TitlePageIndicator) view.findViewById(R.id.titles);
        titlePageIndicator.setViewPager(viewPager);

        return view;
    }
}
