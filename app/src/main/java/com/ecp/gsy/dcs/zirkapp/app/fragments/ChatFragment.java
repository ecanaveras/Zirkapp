package com.ecp.gsy.dcs.zirkapp.app.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v13.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ecp.gsy.dcs.zirkapp.app.R;

/**
 * Created by Elder on 11/04/2015.
 */
public class ChatFragment extends Fragment {

    private FragmentTabHost mTabHost;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_tabs, container, false);

        mTabHost = (FragmentTabHost) view.findViewById(android.R.id.tabhost);
        //mTabHost = new FragmentTabHost(getActivity());
        mTabHost.setup(getActivity(), getFragmentManager(), android.R.id.tabcontent);

        mTabHost.addTab(mTabHost.newTabSpec("tab1").setIndicator(getResources().getString(R.string.lblUsersOnline), null), UsersFragment.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("tab2").setIndicator(getResources().getString(R.string.lblHistory), null), ChatHistoryFragment.class, null);
        return view;
    }


    @Override
    public void onDestroy() {
        mTabHost = null;
        super.onDestroy();
    }

    /*@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        setHasOptionsMenu(true);

        final ArrayList<Fragment> fragmentArrayList = new ArrayList<>();
        final String[] titles = {"Usuarios", "Historial"};

        fragmentArrayList.add(new UsersFragment());
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
    }*/
}
