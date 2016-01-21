package com.ecp.gsy.dcs.zirkapp.app.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ecp.gsy.dcs.zirkapp.app.R;
import com.parse.ParseCloud;
import com.parse.ParseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Elder on 05/01/2016.
 */
public class MainFragment extends Fragment {

    private static MainFragment instance = null;
    public static final String TAG = MainFragment.class.getSimpleName();
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private boolean counterMessages;
    private int tabSelected = 1;

    public static MainFragment newInstance(Bundle arguments) {
        MainFragment mainFragment = new MainFragment();
        if (arguments != null) {
            mainFragment.setArguments(arguments);
        }
        return mainFragment;
    }

    public static MainFragment getInstance() {
        return instance;
    }

    public static boolean isRunning() {
        return instance != null;
    }


    public MainFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        //Obtener el Tab a mostrar por default
        if (this.getArguments() != null) {
            tabSelected = this.getArguments().getInt("tabSelected", 1);
        }

        if (savedInstanceState == null) {
            crearTabs(container);

            //Setear adaptador al viewPager
            viewPager = (ViewPager) view.findViewById(R.id.pager);
            setupViewPager(viewPager);
            tabLayout.setupWithViewPager(viewPager);

            //Select Tab
            TabLayout.Tab tab = tabLayout.getTabAt(tabSelected);
            tab.select();
        }

        instance = this;

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupCountTabMessages();
    }

    private void crearTabs(ViewGroup container) {
        View parent = (View) container.getParent();
        tabLayout = (TabLayout) parent.findViewById(R.id.tabs);//new TabLayout(getActivity());
        tabLayout.setVisibility(View.VISIBLE);
        tabLayout.setTabTextColors(Color.parseColor("#FFFFFF"), Color.parseColor("#FFFFFF"));
        tabLayout.setSelectedTabIndicatorColor(Color.parseColor("#FFFFFF"));
    }

    private void setupViewPager(ViewPager viewPager) {
        AdaptadorSecciones adapter = new AdaptadorSecciones(getFragmentManager());
        adapter.addFragment(new ZimessFragment(), getString(R.string.title_fragment_zimess));
        adapter.addFragment(new ChatHistoryFragment(), getString(R.string.title_tab_messages));
        adapter.addFragment(new UsersFragment(), getString(R.string.title_tab_user_online));
        viewPager.setAdapter(adapter);
    }

    public void setupCountTabMessages() {
        if (counterMessages) {
            return;
        }
        new AsyncTask<Void, Void, Integer>() {

            private View customTabView;
            private TextView title;
            private TextView count;
            private TabLayout.Tab tab;

            @Override
            protected void onPreExecute() {
                counterMessages = true;
                //get Tab
                tab = tabLayout.getTabAt(1); //Tab Mensajes
                customTabView = tab.getCustomView();
                if (customTabView == null) {
                    customTabView = LayoutInflater.from(getActivity()).inflate(R.layout.tablayout_indicator, null);
                }
                title = (TextView) customTabView.findViewById(R.id.titleTab);
                count = (TextView) customTabView.findViewById(R.id.countTab);
            }

            @Override
            protected Integer doInBackground(Void... p) {
                Integer result = null;
                HashMap params = new HashMap<String, Object>();
                try {
                    result = (Integer) ParseCloud.callFunction("getTotalMessagesNoRead", params);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return result;
            }

            @Override
            protected void onPostExecute(Integer result) {
                //Set customview in tablayout
                if (result != null) {
                    //set info Tab
                    title.setText(tab.getText().toString().toUpperCase());
                    if (result > 0) {
                        count.setText(String.valueOf(result));
                    } else {
                        count.setVisibility(View.GONE);
                    }
                    //Set View in Tab
                    tab.setCustomView(null);
                    tab.setCustomView(customTabView);
                }
                counterMessages = false;
            }

        }.execute();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
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
