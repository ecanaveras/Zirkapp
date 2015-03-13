package com.ecp.gsy.dcs.zirkapp.app;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ecp.gsy.dcs.zirkapp.app.util.images.RoundedImageView;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.FindParseObject;
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
public class UserProfileActivity extends Activity {

    private ParseUser currentUser;

    private ActionBar actionBar;
    private RoundedImageView avatar;
    private String objectId;
    private TextView txtCantVisitas, txtWall, txtCantZimess, txtUserNombres;
    private ProgressBar progressBarLoad;
    private GlobalApplication globalApplication;
    private ParseUser zimessUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        currentUser = ParseUser.getCurrentUser();

        globalApplication = (GlobalApplication) getApplicationContext();
        //Tomar el user del Zimess
        zimessUser = globalApplication.getTempZimess().getUser();

        inicializarCompUI();

        loadInfoProfile();

        //Actions in Bar
        actionBar = getActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //Guardar visitar
        saveInfoVisit();

        //Cargar info de visistas y Zimess
        new UserProfileTask().execute(zimessUser);

    }

    private void inicializarCompUI() {
        avatar = (RoundedImageView) findViewById(R.id.imgAvatar);
        txtWall = (TextView) findViewById(R.id.txtWall);
        txtCantVisitas = (TextView) findViewById(R.id.txtCountVisit);
        txtCantZimess = (TextView) findViewById(R.id.txtCountZimess);
        txtUserNombres = (TextView) findViewById(R.id.txtUserNombres);
        progressBarLoad = (ProgressBar) findViewById(R.id.progressLoad);
    }

    private void saveInfoVisit() {
        if (currentUser.equals(zimessUser)) { //Solo usuarios de otro perfil aumentan visitas
            return;
        }
        final ParseObject zimessObject = ParseObject.createWithoutData("ParseZimess", zimessUser.getObjectId());
        //Buscar si existe
        final ParseQuery<ParseObject> query = ParseQuery.getQuery("ParseZVisit");
        query.whereEqualTo("user", zimessObject);
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
                                createOrUpdateVisit(parseObject, zimessObject); //Actualiza
                            }
                        }
                    });
                } else {
                    createOrUpdateVisit(null, zimessObject);
                }
            }
        });

    }

    private void createOrUpdateVisit(ParseObject inParseObject, ParseObject zimessObject) {
        ParseObject parseObject = inParseObject;
        if (parseObject == null) {
            parseObject = new ParseObject("ParseZVisit");
        }
        parseObject.put("user", zimessObject);
        parseObject.increment("count_visit");
        parseObject.saveInBackground();
    }

    private void loadInfoProfile() {
        new RefreshDataProfileTask(avatar, txtWall, txtUserNombres, getString(R.string.msgLoading), this).execute(zimessUser);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed(); //Regresar al intent invocador
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
            progressBarLoad.setBackgroundColor(Color.parseColor("#FFFFFF"));
            progressBarLoad.setVisibility(View.VISIBLE);
        }

        @Override
        protected ParseObject doInBackground(ParseUser... parseUsers) {
            //Buscamos Cant de Zimess en Parse
            cantZimess = FindParseObject.findCountZimess(parseUsers[0]);
            //Buscamos Visitas en Parse
            return FindParseObject.findDataVisit(parseUsers[0]);
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
