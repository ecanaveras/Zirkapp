package com.ecp.gsy.dcs.zirkapp.app.activities;

import android.app.ActionBar;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
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
import com.ecp.gsy.dcs.zirkapp.app.util.task.RefreshDataProfileTask;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.HashMap;

/**
 * Created by Elder on 07/02/2015.
 */
public class UserProfileActivity extends ActionBarActivity {

    private ParseUser currentUser;

    private ActionBar actionBar;
    private ImageView avatar;
    private String objectId;
    private TextView txtCantVisitas, txtWall, txtCantZimess, txtUserNombres;
    private ProgressBar progressBarLoad;
    private GlobalApplication globalApplication;
    private ParseUser parseUser;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        currentUser = ParseUser.getCurrentUser();

        globalApplication = (GlobalApplication) getApplicationContext();
        parseUser = globalApplication.getCustomParseUser();

        inicializarCompUI();

        setTitle("Info. del Usuario");

        loadInfoProfile();

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

        avatar = (ImageView) findViewById(R.id.imgAvatar);
        txtWall = (TextView) findViewById(R.id.txtWall);
        txtCantVisitas = (TextView) findViewById(R.id.txtCountVisit);
        txtCantZimess = (TextView) findViewById(R.id.txtCountZimess);
        txtUserNombres = (TextView) findViewById(R.id.txtUserNombres);
        progressBarLoad = (ProgressBar) findViewById(R.id.progressLoad);
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

        //Buscar si existe
        /*final ParseQuery<ParseZVisit> query = ParseQuery.getQuery(ParseZVisit.class);
        query.whereEqualTo(ParseZVisit.USER, parseUser);
        query.getFirstInBackground(new GetCallback<ParseZVisit>() {
            @Override
            public void done(ParseZVisit parseZVisit, ParseException e) {
                if (e == null && parseZVisit != null) {
                    //Actualizar
                    createOrUpdateVisit(parseZVisit, parseUser);
                } else {
                    createOrUpdateVisit(null, parseUser);
                }
            }
        });*/

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

    private void loadInfoProfile() {
        new RefreshDataProfileTask(avatar, txtWall, txtUserNombres, getResources().getString(R.string.msgLoading), this).execute(parseUser);
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
            //Buscamos la informacion mas reciente del usuario.
            /*ParseQuery query = ParseUser.getQuery();
            query.whereEqualTo("objectId", parseUsers[0].getObjectId());
            try {
                userTemp = (ParseUser) query.getFirst();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            */
            return userTemp;
        }

        @Override
        protected void onPostExecute(ParseUser parseObject) {
            if (parseObject != null) {
                txtCantVisitas.setText(String.valueOf(parseObject.getInt("count_visit")));
                txtCantZimess.setText(String.valueOf(parseObject.getInt("count_zimess")));
            } else {
                txtCantVisitas.setText("0");
                txtCantZimess.setText("0");
            }
            //Cant de Zimess
            //txtCantZimess.setText(Integer.toString(cantZimess));
            progressBarLoad.setVisibility(View.INVISIBLE);

        }
    }
}
