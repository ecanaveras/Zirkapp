package com.ecp.gsy.dcs.zirkapp.app;

import android.app.ActionBar;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ecp.gsy.dcs.zirkapp.app.util.parse.DataParseHelper;
import com.ecp.gsy.dcs.zirkapp.app.util.task.GlobalApplication;
import com.ecp.gsy.dcs.zirkapp.app.util.task.RefreshDataProfileTask;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

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

        //Buscar si existe
        final ParseQuery<ParseObject> query = ParseQuery.getQuery("ParseZVisit");
        query.whereEqualTo("user", parseUser);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (parseObjects.size() > 0) {
                    objectId = parseObjects.get(0).getObjectId();
                }
                if (objectId != null) {//actualizar
                    query.getInBackground(objectId, new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject parseObject, ParseException e) {
                            if (e == null) {
                                createOrUpdateVisit(parseObject, parseUser); //Actualiza
                            }
                        }
                    });
                } else {
                    createOrUpdateVisit(null, parseUser);
                }
            }
        });

    }

    private void createOrUpdateVisit(ParseObject inParseObject, ParseUser parseUser) {
        ParseObject parseObject = inParseObject;
        if (parseObject == null) {
            parseObject = new ParseObject("ParseZVisit");
        }
        parseObject.put("user", parseUser);
        parseObject.increment("count_visit");
        parseObject.saveInBackground();
    }

    private void loadInfoProfile() {
        new RefreshDataProfileTask(avatar, txtWall, txtUserNombres, getString(R.string.msgLoading), this).execute(parseUser);
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


    private class UserProfileTask extends AsyncTask<ParseUser, Void, ParseObject> {

        private Integer cantZimess;


        @Override
        protected void onPreExecute() {
            progressBarLoad.setVisibility(View.VISIBLE);
        }

        @Override
        protected ParseObject doInBackground(ParseUser... parseUsers) {
            //Buscamos Cant de Zimess en Parse
            cantZimess = DataParseHelper.findCountZimess(parseUsers[0]);
            //Buscamos Visitas en Parse
            return DataParseHelper.findDataVisit(parseUsers[0]);
        }

        @Override
        protected void onPostExecute(ParseObject parseObject) {
            if (parseObject != null) {
                txtCantVisitas.setText(parseObject.get("count_visit").toString());
            } else {
                txtCantVisitas.setText("0");
            }
            //Cant de Zimess
            txtCantZimess.setText(Integer.toString(cantZimess));

            progressBarLoad.setVisibility(View.INVISIBLE);

        }
    }
}
