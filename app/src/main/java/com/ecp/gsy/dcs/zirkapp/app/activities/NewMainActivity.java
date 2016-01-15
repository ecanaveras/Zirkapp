package com.ecp.gsy.dcs.zirkapp.app.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.fragments.ChatHistoryFragment;
import com.ecp.gsy.dcs.zirkapp.app.fragments.UsersFragment;
import com.ecp.gsy.dcs.zirkapp.app.fragments.ZimessFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Elder on 05/01/2016.
 */
public class NewMainActivity extends AppCompatActivity {

    private AppBarLayout appBar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ZimessFragment zimessFragment;
    private UsersFragment usersFragment;
    private ChatHistoryFragment chatHistoryFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_main);
        if (savedInstanceState == null) {
            zimessFragment = ZimessFragment.newInstance(null);
            usersFragment = UsersFragment.newInstance(null);
            chatHistoryFragment = ChatHistoryFragment.newInstance(null);
            crearTabs();

            //Setear adaptador al viewPager
            viewPager = (ViewPager) findViewById(R.id.pager);
            setupViewPager(viewPager);
            tabLayout.setupWithViewPager(viewPager);
        }

    }

    private void crearTabs() {
        appBar = (AppBarLayout) findViewById(R.id.appbar);
        tabLayout = new TabLayout(this);
        tabLayout.setTabTextColors(Color.parseColor("#FFFFFF"), Color.parseColor("#FFFFFF"));
        tabLayout.setSelectedTabIndicatorColor(Color.parseColor("#FFFFFF"));
        appBar.addView(tabLayout);
    }

    private void setupViewPager(ViewPager viewPager) {
        AdaptadorSecciones adapter = new AdaptadorSecciones(getFragmentManager());
        adapter.addFragment(zimessFragment, getString(R.string.title_fragment_zimess));
        adapter.addFragment(usersFragment, getString(R.string.title_tab_user_online));
        adapter.addFragment(chatHistoryFragment, getString(R.string.title_tab_messages));
        viewPager.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
