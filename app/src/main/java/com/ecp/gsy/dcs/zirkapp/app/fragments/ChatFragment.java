package com.ecp.gsy.dcs.zirkapp.app.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
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
import android.widget.TextView;

import com.ecp.gsy.dcs.zirkapp.app.GlobalApplication;
import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.activities.MainActivity;
import com.ecp.gsy.dcs.zirkapp.app.util.listener.FragmentIterationListener;
import com.parse.ParseCloud;
import com.parse.ParseException;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Elder on 11/04/2015.
 */
public class ChatFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager viewPager;

    private static ChatFragment instance = null;
    public static final String TAG = ChatFragment.class.getSimpleName();
    private AdaptadorSecciones adapter;
    private int tabSelected = 0;
    private boolean counterMessages = false;

    public static ChatFragment newInstance(Bundle arguments) {
        ChatFragment chatFragment = new ChatFragment();
        if (arguments != null) {
            chatFragment.setArguments(arguments);
        }
        return chatFragment;
    }

    public static ChatFragment getInstance() {
        return instance;
    }

    public static boolean isRunning() {
        return instance != null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle arguments) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        //Obtener el Tab a mostrar por default
        if (this.getArguments() != null) {
            tabSelected = this.getArguments().getInt("tabSelected", 0);
        }

        adapter = new AdaptadorSecciones(getFragmentManager());
        adapter.addFragment(UsersFragment.newInstance(null), getString(R.string.title_tab_user_online));
        adapter.addFragment(ChatHistoryFragment.newInstance(null), getString(R.string.title_tab_messages));

        initComponentsUI(view);

        instance = this;

        return view;
    }

    private void initComponentsUI(View view_fragment) {
        tabLayout = (TabLayout) view_fragment.findViewById(R.id.tabs);
        tabLayout.setSelectedTabIndicatorColor(Color.parseColor("#FFFFFF"));

        viewPager = (ViewPager) view_fragment.findViewById(R.id.pager);
        //Setear View Pager
        viewPager.setAdapter(adapter);
        //Setear ViewPager en TabLayout
        tabLayout.setupWithViewPager(viewPager);

        //Select Tab
        TabLayout.Tab tab = tabLayout.getTabAt(tabSelected);
        tab.select();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Set customview in tablayout
        if (tabLayout.getTabCount() == 2) {
            setupCountTabMessages();
            //Log.i(TAG, "setupCustomTab: true");
        }
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
            protected Integer doInBackground(Void... params) {
                return getCantMessages();
            }

            @Override
            protected void onPostExecute(Integer result) {
                //Set customview in tablayout
                if (result != null) {
                    //set info Tab
                    title.setText(tab.getText());
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

    /**
     * Invoca una funcion en Parse que devuelve la cantidad de mensajes no leidos del usuario actual
     *
     * @return
     */
    private Integer getCantMessages() {
        if (!counterMessages) {
            HashMap params = new HashMap<String, Object>();
            try {
                return (Integer) ParseCloud.callFunction("getTotalMessagesNoRead", params);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void onBackPressed() {
        if (tabSelected == 1) {
            MainActivity activity = (MainActivity) getActivity();
            activity.selectItemDrawer(activity.getNavItem(1));
        }
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
