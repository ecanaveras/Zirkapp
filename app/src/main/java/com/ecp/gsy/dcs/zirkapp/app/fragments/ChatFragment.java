package com.ecp.gsy.dcs.zirkapp.app.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v13.app.FragmentTabHost;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.listener.FragmentIterationListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Elder on 11/04/2015.
 */
public class ChatFragment extends Fragment {

    private AppBarLayout appBar;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    private static ChatFragment instance = null;
    public static final String TAG = "ChatFragment";
    private FragmentIterationListener mCallback = null;

    public static ChatFragment newInstance(Bundle arguments) {
        ChatFragment chatFragment = new ChatFragment();
        if (arguments != null) {
            chatFragment.setArguments(arguments);
        }
        return chatFragment;
    }

    public ChatFragment() {
    }

    public static ChatFragment getInstance() {
        return instance;
    }

    public static boolean isRunning() {
        return instance != null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        initComponentsUI(container, view);

        instance = this;

        return view;
    }

    private void initComponentsUI(ViewGroup container, View view_fragment) {
        View main = (View) container.getParent();
        appBar = (AppBarLayout) main.findViewById(R.id.appbar);
        tabLayout = new TabLayout(getActivity());
        tabLayout.setTabTextColors(Color.parseColor("#FFFFFF"), Color.parseColor("#FFFFFF"));
        tabLayout.setSelectedTabIndicatorColor(Color.parseColor("#FFFFFF"));
        appBar.addView(tabLayout);

        viewPager = (ViewPager) view_fragment.findViewById(R.id.pager);
        //Setear View Pager
        AdaptadorSecciones adapter = new AdaptadorSecciones(getFragmentManager());
        adapter.addFragment(new UsersFragment(), getString(R.string.title_tab_user_online));
        adapter.addFragment(new ChatHistoryFragment(), getString(R.string.title_tab_messages));
        viewPager.setAdapter(adapter);
        //Setear ViewPager en TabLayout
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        try {
            mCallback = (FragmentIterationListener) activity;
        } catch (ClassCastException ex) {
            Log.e(TAG, "El activity debe implementar la interfaz FragmentIterationListener");
        }
    }

    @Override
    public void onDetach() {
        mCallback = null;
        super.onDetach();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        appBar.removeView(tabLayout);
    }

    /**
     * Gestiona los fragmentos y titulos de los tabs
     */
    public class AdaptadorSecciones extends FragmentStatePagerAdapter {

        private final List<Fragment> fragments = new ArrayList<>();
        private final List<String> titles = new ArrayList<>();

        public AdaptadorSecciones(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
            fragments.add(fragment);
            titles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }
}
