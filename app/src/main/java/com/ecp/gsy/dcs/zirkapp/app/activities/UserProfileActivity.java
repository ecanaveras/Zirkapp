package com.ecp.gsy.dcs.zirkapp.app.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ecp.gsy.dcs.zirkapp.app.GlobalApplication;
import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.models.ParseZVisit;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by Elder on 07/02/2015.
 */
public class UserProfileActivity extends AppCompatActivity {

    private static final String EXTRA_POSITION = "POSITION";

    private ParseUser currentUser;

    private ActionBar actionBar;
    private ImageView avatar;
    private String objectId;
    private TextView txtCantVisitas, txtWall, txtCantZimess, txtUserNombres;
    private ProgressBar progressBarLoad;
    private GlobalApplication globalApplication;
    private ParseUser parseUser;
    private Toolbar toolbar;
    private TextView txtEdad;

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

        avatar = (ImageView) findViewById(R.id.imgAvatar);
        txtWall = (TextView) findViewById(R.id.txtWall);
        txtEdad = (TextView) findViewById(R.id.txtEdad);
        txtCantVisitas = (TextView) findViewById(R.id.txtCountVisit);
        txtCantZimess = (TextView) findViewById(R.id.txtCountZimess);
        txtUserNombres = (TextView) findViewById(R.id.txtUserNombres);
        progressBarLoad = (ProgressBar) findViewById(R.id.progressLoad);

        //Setup Data
        //txtUserNombres.setText(name);
        txtWall.setText(parseUser.getString("wall") != null && !parseUser.getString("wall").isEmpty() ? parseUser.getString("wall") : getString(R.string.usingZirkapp));
        txtCantVisitas.setText(String.valueOf(parseUser.getInt("count_visit")));
        txtCantZimess.setText(String.valueOf(parseUser.getInt("count_zimess")));
        int edad = calcEdad(parseUser.getDate("birthday"));
        if (edad > 0)
            txtEdad.setText(edad + getString(R.string.lblYears));
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
        int age = 0;
        if (birthday != null) {
            Calendar dob = Calendar.getInstance();
            dob.setTime(birthday);
            Calendar today = Calendar.getInstance();
            age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
            if (today.get(Calendar.MONTH) < dob.get(Calendar.MONTH)) {
                age--;
            } else if (today.get(Calendar.MONTH) == dob.get(Calendar.MONTH)
                    && today.get(Calendar.DAY_OF_MONTH) < dob.get(Calendar.DAY_OF_MONTH)) {
                age--;
            }
        }
        return age;
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
            progressBarLoad.setVisibility(View.VISIBLE);
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
                txtCantVisitas.setText(String.valueOf(parseUser.getInt("count_visit")));
                txtCantZimess.setText(String.valueOf(parseUser.getInt("count_zimess")));
            } else {
                txtCantVisitas.setText("0");
                txtCantZimess.setText("0");
            }
            progressBarLoad.setVisibility(View.INVISIBLE);

        }
    }
}
