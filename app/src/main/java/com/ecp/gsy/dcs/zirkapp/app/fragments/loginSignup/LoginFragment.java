package com.ecp.gsy.dcs.zirkapp.app.fragments.loginSignup;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.activities.ManagerLogin;
import com.ecp.gsy.dcs.zirkapp.app.util.beans.HandlerLogindb;
import com.ecp.gsy.dcs.zirkapp.app.util.beans.Welcomedb;
import com.ecp.gsy.dcs.zirkapp.app.util.database.DatabaseHelper;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

//import com.facebook.AccessToken;
//import com.facebook.GraphRequest;
//import com.facebook.GraphResponse;
//import com.parse.ParseFacebookUtils;
//import com.parse.ParseTwitterUtils;
//import com.parse.twitter.Twitter;

/**
 * Created by Elder on 18/02/2015.
 */
public class LoginFragment extends Fragment {

    private String username = null;
    private String password = null;
    private Activity activity;
    private DatabaseHelper databaseHelper;
    private EditText txtUser;
    private EditText txtPassword;
    private ProgressDialog progressDialog;
    private ParseUser userLogin;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        //Facebook
        //callbackManager = CallbackManager.Factory.create();

        inicializarCompUI(view);
        activity = getActivity();

        //Database
        databaseHelper = OpenHelperManager.getHelper(getActivity(), DatabaseHelper.class);

        return view;
    }

    private void inicializarCompUI(View view) {

        Button btnLogin = (Button) view.findViewById(R.id.btnLogin);
        //Button btnLoginFacebook = (Button) view.findViewById(R.id.btnLoginFacebook);
        //Button btnLoginTwitter = (Button) view.findViewById(R.id.btnLoginTwitter);
        txtUser = (EditText) view.findViewById(R.id.txtUserLogin);
        txtPassword = (EditText) view.findViewById(R.id.txtPassLogin);
        TextView createAccount = (TextView) view.findViewById(R.id.lblCreateAccount);

        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Cambia el fragment
                ManagerLogin managerLogin = (ManagerLogin) activity;
                managerLogin.mViewPager.setCurrentItem(managerLogin.mViewPager.getCurrentItem() + 1);
            }
        });


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginWithParse();
            }
        });
        /*
        btnLoginFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginWithFacebook();
            }
        });

        btnLoginTwitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginWithTwitter();
            }
        });
        */
    }

    /**
     * Realiza el logueo con PARSE
     */
    private void loginWithParse() {
        username = txtUser.getText().toString();
        password = txtPassword.getText().toString();

        if (username.isEmpty()) {
            txtUser.setError(getResources().getString(R.string.msgSignUpUserEmpty));
            Toast.makeText(getActivity(),
                    R.string.msgLoginEmpty,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.isEmpty()) {
            txtPassword.setError(getResources().getString(R.string.msgSignUpPassEmpty));
            Toast.makeText(getActivity(),
                    R.string.msgLoginEmpty,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.trim().length() <= 5) {
            Toast.makeText(getActivity(),
                    R.string.msgLoginPassInvalid,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getResources().getString(R.string.mgsLoging));
        progressDialog.show();

        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (user != null) {
                    //Conectar al chat
                    user.put("online", true);
                    user.saveInBackground();
                    saveInfoWelcome();
                    //Guardar en db una session activa
                    saveSessionActive(true);
                    //finish
                    activity.finish();
                } else {
                    //TODO manejar excepciones de login
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
                progressDialog.dismiss();
            }
        });
    }

    /**
     * Realiza el logueo con FACEBOOK
     * /
    private void loginWithFacebook() {
        //(https://developers.facebook.com/docs/facebook-login/permissions/)
        List<String> permisos = Arrays.asList("public_profile", "email");

        ParseFacebookUtils.logInWithReadPermissionsInBackground(getActivity(), permisos, new LogInCallback() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                if (parseUser == null) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.msgNoLoginFacebook), Toast.LENGTH_SHORT).show();
                } else {
                    getDataFacebook(true);
                }
            }
        });
    }
     */

    /**
     * Realiza el logueo con TWITTER
     * /
    private void loginWithTwitter() {
        userLogin = null;
        ParseTwitterUtils.logIn(getActivity(), new LogInCallback() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                if (parseUser == null) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.msgNoLoginTwitter), Toast.LENGTH_SHORT).show();
                } else {
                    userLogin = parseUser;
                    Twitter twitterUser = ParseTwitterUtils.getTwitter();
                    if (twitterUser != null) {
                        getDataTwitter(twitterUser);
                    }
                }
            }
        });
    }
     */

    /*
    private void getDataFacebook(final boolean isNew) {
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getResources().getString(R.string.mgsLoging));
        progressDialog.show();
        //Obtener info de facebook
        GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {

                    @Override
                    public void onCompleted(JSONObject fbUser, GraphResponse response) {
                        userLogin = ParseUser.getCurrentUser();
                        if (fbUser != null && userLogin != null) {
                            //if (isNew)
                            userLogin.setUsername(fbUser.optString("first_name"));
                            userLogin.put("name", fbUser.optString("name"));
                            userLogin.setEmail(fbUser.optString("email"));
                            //userLogin.put("emailVerified", fbUser.optBoolean("verified")); Only Read
                            userLogin.put("online", true);
                            userLogin.saveInBackground();
                            //Buscar y guardar Avatar
                            getAvatarFacebook(fbUser.optString("id"));
                            //Guardar informacion del welcome
                            saveInfoWelcome();
                            //Guardar en db una session activa
                            saveSessionActive(true);
                        }
                    }
                }).executeAsync();
    }
    */

    /*
    private void getDataTwitter(Twitter twitter) {
        if (userLogin != null && twitter.getScreenName().length() > 0) {
            if (userLogin.isNew()) {
                userLogin.setUsername(twitter.getScreenName());
                userLogin.put("online", true);
                userLogin.saveInBackground();
            }
            //Buscar y guardar Avatar
            getAvatarTwitter(twitter);
            //Guardar informacion del welcome
            saveInfoWelcome();
            //Guardar en db una session activa
            saveSessionActive(true);
        }
    }
    */

    /**
     * GET Avatar de la API de TWITTER
     *
     * @param twitter
     * /
    private void getAvatarTwitter(Twitter twitter) {

        //Obtener info de Twitter
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getResources().getString(R.string.mgsLoging));
        progressDialog.show();

        AsyncTask<Twitter, Void, Bitmap> task = new AsyncTask<Twitter, Void, Bitmap>() {

            @Override
            protected Bitmap doInBackground(Twitter... twitters) {
                String urlTwitterFormat = "https://api.twitter.com/1.1/users/show.json?screen_name=%s";
                //HttpClient client = new DefaultHttpClient();
                AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
                HttpGet verifyGet = new HttpGet(URI.create(String.format(urlTwitterFormat, twitters[0].getScreenName())));
                twitters[0].signRequest(verifyGet);
                Bitmap bitmap = null;
                try {
                    HttpResponse response = client.execute(verifyGet);
                    HttpEntity entity = response.getEntity();
                    InputStream inputStream = entity.getContent();
                    JSONObject jsonObject = new JSONObject(convertStreamToString(inputStream));
                    if (userLogin != null) {
                        userLogin.put("name", jsonObject.getString("name"));
                        userLogin.put("city", jsonObject.getString("location"));
                    }
                    String urlImage = jsonObject.getString("profile_image_url");
                    urlImage = urlImage.replace("_normal", "");
                    //Log.i("profile_image_url", urlImage);
                    URL url = new URL(urlImage);
                    URLConnection urlConnection = url.openConnection();
                    urlConnection.setUseCaches(true);
                    urlConnection.connect();
                    InputStream is = urlConnection.getInputStream();
                    bitmap = BitmapFactory.decodeStream(is);
                    is.close();
                } catch (Exception e) {
                    Log.e("get.avatar.twitter", e.getMessage());
                } finally {
                    if (verifyGet != null) {
                        verifyGet.abort();
                    }
                    if (client != null) {
                        client.close();
                    }
                }
                return bitmap;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if (bitmap != null && userLogin != null) {
                    ParseFile parseFile = new ParseFile("ParseZAvatar", getByteAvatar(bitmap));
                    parseFile.saveInBackground();
                    userLogin.put("avatar", parseFile);
                }
                if (userLogin != null) {
                    //Guardar informacion de usuario
                    userLogin.saveInBackground();
                }
                if (progressDialog != null)
                    progressDialog.dismiss();

                activity.finish();
            }

        }.execute(twitter);
    }
    */


    /**
     * GET Avatar de la API de FACEBOOK
     *
     * @param facebookId
     */
    private void getAvatarFacebook(final String facebookId) {
        AsyncTask<Void, Void, Bitmap> task = new AsyncTask<Void, Void, Bitmap>() {

            @Override
            protected Bitmap doInBackground(Void... params) {
                Bitmap bitmap = null;
                if (facebookId != null) {
                    Log.i("run.facebook.id", facebookId);
                    String urlFacebookFormat = "https://graph.facebook.com/%s/picture?type=large";
                    try {
                        URL url = new URL(String.format(urlFacebookFormat, facebookId));
                        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                        httpURLConnection.setDoInput(true);
                        httpURLConnection.setInstanceFollowRedirects(true);
                        httpURLConnection.connect();
                        InputStream is = new BufferedInputStream(httpURLConnection.getInputStream());
                        bitmap = BitmapFactory.decodeStream(is);
                        is.close();
                    } catch (Exception e) {
                        Log.e("get.avatar.facebook", e.getMessage());
                    }
                }
                return bitmap;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if (bitmap != null && userLogin != null) {
                    ParseFile parseFile = new ParseFile("ParseZAvatar", getByteAvatar(bitmap));
                    parseFile.saveInBackground();
                    userLogin.put("avatar", parseFile);
                    //Guardar informacion de usuario
                    userLogin.saveInBackground();
                }

                if (progressDialog != null)
                    progressDialog.dismiss();

                activity.finish();
            }
        };
        task.execute();
    }


    private byte[] getByteAvatar(Bitmap photo) {
        if (photo == null)
            return null;

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        return stream.toByteArray();
    }


    private void saveInfoWelcome() {
        List<Welcomedb> listWdb = new ArrayList<Welcomedb>();

        Dao dao = null;
        try {
            dao = databaseHelper.getWelcomedbDao();
            listWdb = dao.queryForAll();
        } catch (SQLException e) {
            Log.e("Ormlite", "Error buscando welcome");
        }

        boolean guardar = true;

        //Si existe un registro de welcolme, no guardar otro mas
        for (Welcomedb w : listWdb) {
            guardar = false;
            break;
        }

        if (guardar && dao != null) {
            try {
                Welcomedb wdb = new Welcomedb("SI");
                dao.create(wdb);
            } catch (SQLException e) {
                Log.e("Ormlite", "Error creando welcome");
            }
        }
    }

    private void saveSessionActive(boolean sessionActive) {
        List<HandlerLogindb> listHldb = new ArrayList<>();

        Dao dao = null;
        try {
            dao = databaseHelper.getHandlerLogindbDao();
            listHldb = dao.queryForAll();
        } catch (SQLException e) {
            Log.e("Ormlite", "Error buscando handlerLogin");
        }


        boolean guardar = true;

        //Si existe un registro se actualiza y no se crea uno nuevo
        for (HandlerLogindb row : listHldb) {
            if (dao != null) {
                try {
                    row.setSessionActive(sessionActive);
                    dao.update(row);
                } catch (SQLException e) {
                    Log.e("Ormlite", "Error actualizando handlerLogin");
                }
            }
            guardar = false;
            break;
        }

        //Guardar si no existen registro
        if (guardar && dao != null) {
            try {
                HandlerLogindb ldb = new HandlerLogindb(sessionActive);
                dao.create(ldb);
            } catch (SQLException e) {
                Log.e("Ormlite", "Error creando handlerLogin");
            }
        }
    }

    public static String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    @Override
    public void onDestroyView() {
        if (databaseHelper != null) {
            OpenHelperManager.releaseHelper();
            databaseHelper = null;
        }
        super.onDestroyView();
    }
}
