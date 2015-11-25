package com.ecp.gsy.dcs.zirkapp.app.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ecp.gsy.dcs.zirkapp.app.GlobalApplication;
import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.fragments.ChatHistoryFragment;
import com.ecp.gsy.dcs.zirkapp.app.fragments.UsersFragment;
import com.ecp.gsy.dcs.zirkapp.app.fragments.loginSignup.LoginFragment;
import com.ecp.gsy.dcs.zirkapp.app.fragments.loginSignup.SignupFragment;
import com.ecp.gsy.dcs.zirkapp.app.fragments.profile.InfoUserFragment;
import com.ecp.gsy.dcs.zirkapp.app.fragments.profile.SocialUserFragment;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.models.ParseZVisit;
import com.ecp.gsy.dcs.zirkapp.app.util.task.SendPushTask;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Elder on 07/02/2015.
 */
public class UserProfileActivity extends AppCompatActivity {

    private static final String EXTRA_POSITION = "POSITION";

    private ParseUser currentUser;

    private ImageView avatar;
    private TextView txtWall;// txtCantZimess, txtCantVisitas,txtUserNombres;
    private GlobalApplication globalApplication;
    private ParseUser parseUser;
    private Toolbar toolbar;
    private TextView txtEdad;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        currentUser = ParseUser.getCurrentUser();

        globalApplication = (GlobalApplication) getApplicationContext();
        parseUser = globalApplication.getCustomParseUser();

        inicializarCompUI();

        globalApplication.setAvatarParse(parseUser.getParseFile("avatar"), avatar, false);

        //Guardar visitar
        saveInfoVisit();
        //Cargar info de visistas y Zimess
        new UserProfileTask().execute(parseUser);

    }

    private void inicializarCompUI() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


        CollapsingToolbarLayout collapser =
                (CollapsingToolbarLayout) findViewById(R.id.collapser);
        String name = parseUser.getString("name") != null ? parseUser.getString("name") : parseUser.getUsername();
        collapser.setTitle(name); // Cambiar titulo

        //Tabs
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setTabTextColors(Color.parseColor("#FFFFFF"), Color.parseColor("#FFFFFF"));
        tabLayout.setSelectedTabIndicatorColor(Color.parseColor("#FFFFFF"));

        viewPager = (ViewPager) findViewById(R.id.pager);
        //Setear View Pager
        AdaptadorSecciones adapter = new AdaptadorSecciones(getFragmentManager());
        adapter.addFragment(new InfoUserFragment(), getString(R.string.title_tab_info_user));
        adapter.addFragment(new SocialUserFragment(), getString(R.string.title_tab_social_user));
        viewPager.setAdapter(adapter);
        //Setear ViewPager en TabLayout
        tabLayout.setupWithViewPager(viewPager);
        //Fin tabs


        avatar = (ImageView) findViewById(R.id.imgAvatar);
        txtWall = (TextView) findViewById(R.id.txtWall);
        txtEdad = (TextView) findViewById(R.id.txtEdad);

        //Botones
        ImageButton btnOpenChat = (ImageButton) findViewById(R.id.btnOpenChat);
        ImageButton btnSendZiss = (ImageButton) findViewById(R.id.btnSendZiss);

        //Accion Botones
        btnOpenChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Ir la perfil del usuario
                globalApplication.setCustomParseUser(parseUser);
                Intent intent = new Intent(UserProfileActivity.this, MessagingActivity.class);
                startActivity(intent);
            }
        });

        btnSendZiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nameCurrentUser = currentUser.getString("name") != null ? currentUser.getString("name") : currentUser.getUsername();
                String nameReceptorUser = parseUser.getString("name") != null ? parseUser.getString("name") : parseUser.getUsername();
                new SendPushTask(parseUser, currentUser.getObjectId(), "Ziiiss", String.format("%s ha dado un toque en tu perfil...", nameCurrentUser), SendPushTask.PUSH_ZISS).execute();
                Toast.makeText(UserProfileActivity.this, String.format("Has dado un Ziss a %s", nameReceptorUser), Toast.LENGTH_SHORT).show();
            }
        });

        txtWall.setText(parseUser.getString("wall") != null && !parseUser.getString("wall").isEmpty() ? parseUser.getString("wall") : getString(R.string.usingZirkapp));


        int edad = calcEdad(parseUser.getDate("birthday"));
        if (edad > 0)
            txtEdad.setText(edad + " " + getString(R.string.lblYears));
        else {
            txtEdad.setText(null);
            txtEdad.setVisibility(View.GONE);
        }
    }

    private void saveInfoVisit() {
        if (currentUser.equals(parseUser)) { //Solo usuarios de otro perfil aumentan visitas
            return;
        }

        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("userId", parseUser.getObjectId());
        ParseCloud.callFunctionInBackground("AddUserVisit", params, new FunctionCallback<String>() {
            public void done(String result, ParseException e) {
                if (e == null) {
                    //Log.e("P.Cloud.ZVisit.Result", result);
                } else {
                    Log.e("P.Cloud.ZVisit", e.getMessage());
                }
            }
        });
    }

    private int calcEdad(Date birthday) {
        Double age = 0.0;
        if (birthday != null) {
            Calendar dob = Calendar.getInstance();
            dob.setTime(birthday);
            Calendar today = Calendar.getInstance();
            age = Double.valueOf(today.get(Calendar.YEAR) - dob.get(Calendar.YEAR));
            if (today.get(Calendar.MONTH) < dob.get(Calendar.MONTH)) {
                age--;
            } else if (today.get(Calendar.MONTH) == dob.get(Calendar.MONTH)
                    && today.get(Calendar.DAY_OF_MONTH) < dob.get(Calendar.DAY_OF_MONTH)) {
                age--;
            }
        }
        return age.intValue();
    }

    /**
     * Crea o actualiza las visitas al usuario
     *
     * @param parseZVisit
     * @param parseUser
     */
    private void createOrUpdateVisit(ParseZVisit parseZVisit, ParseUser parseUser) {
        if (parseZVisit == null) {
            parseZVisit = new ParseZVisit();
        }
        parseZVisit.put(ParseZVisit.USER, parseUser);
        parseZVisit.increment(ParseZVisit.COUNT_VISIT);
        parseZVisit.saveInBackground();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static void launch(Activity context, int position, View sharedView) {
        Intent intent = new Intent(context, UserProfileActivity.class);
        intent.putExtra(EXTRA_POSITION, position);

        // Los elementos 4, 5 y 6 usan elementos compartidos,
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            if (position >= 3) {
                ActivityOptions options0 = ActivityOptions
                        .makeSceneTransitionAnimation(context, sharedView, sharedView.getTransitionName());
                context.startActivity(intent, options0.toBundle());
            } else {
                ActivityOptions options0 = ActivityOptions.makeSceneTransitionAnimation(context);
                context.startActivity(intent, options0.toBundle());
            }
        }
    }

    private class UserProfileTask extends AsyncTask<ParseUser, Void, ParseUser> {

        private Integer cantZimess;

        @Override
        protected void onPreExecute() {
            //progressBarLoad.setVisibility(View.VISIBLE);
        }

        @Override
        protected ParseUser doInBackground(ParseUser... parseUsers) {
            ParseUser userTemp = null;
            try {
                userTemp = parseUsers[0].fetch();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return userTemp;
        }

        @Override
        protected void onPostExecute(ParseUser parseUser) {
            if (parseUser != null) {
                globalApplication.setAvatarParse(parseUser.getParseFile("avatar"), avatar, false);
                //txtUserNombres.setText(parseUser.getString("name") != null ? parseUser.getString("name") : parseUser.getUsername());
                txtWall.setText(parseUser.getString("wall") != null && !parseUser.getString("wall").isEmpty() ? parseUser.getString("wall") : getString(R.string.usingZirkapp));
                //txtCantVisitas.setText(String.valueOf(parseUser.getInt("count_visit")));
                //txtCantZimess.setText(String.valueOf(parseUser.getInt("count_zimess")));
            } else {
                //txtCantVisitas.setText("0");
                //txtCantZimess.setText("0");
            }
            //progressBarLoad.setVisibility(View.INVISIBLE);

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
