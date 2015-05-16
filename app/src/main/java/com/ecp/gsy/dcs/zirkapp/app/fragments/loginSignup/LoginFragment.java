package com.ecp.gsy.dcs.zirkapp.app.fragments.loginSignup;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.activities.ManagerLogin;
import com.ecp.gsy.dcs.zirkapp.app.util.beans.Welcomedb;
import com.ecp.gsy.dcs.zirkapp.app.util.database.DatabaseHelper;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.gc.materialdesign.views.ButtonRectangle;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseFile;
import com.parse.ParseTwitterUtils;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.twitter.Twitter;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

        ButtonRectangle btnLogin = (ButtonRectangle) view.findViewById(R.id.btnLogin);
        Button btnLoginFacebook = (Button) view.findViewById(R.id.btnLoginFacebook);
        Button btnLoginTwitter = (Button) view.findViewById(R.id.btnLoginTwitter);
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

        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Ingresando...");
        progressDialog.show();

        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (user != null) {
                    //Conectar al chat
                    initChatAndSave(user);
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
     */
    private void loginWithFacebook() {
        //(https://developers.facebook.com/docs/facebook-login/permissions/)
        List<String> permisos = Arrays.asList("public_profile", "email");

        ParseFacebookUtils.logInWithReadPermissionsInBackground(getActivity(), permisos, new LogInCallback() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                if (parseUser == null) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.msgNoLoginFacebook), Toast.LENGTH_LONG).show();
                } else if (parseUser.isNew()) {
                    GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(),
                            new GraphRequest.GraphJSONObjectCallback() {

                                @Override
                                public void onCompleted(JSONObject fbUser, GraphResponse response) {
                                    ParseUser user = ParseUser.getCurrentUser();
                                    if (fbUser != null && user != null && fbUser.optString("name").length() > 0) {
                                        user.setUsername(fbUser.optString("first_name"));
                                        user.put("name", fbUser.optString("name"));
                                        user.setEmail(fbUser.optString("email"));
                                        getAvatarFacebook(user, fbUser.optString("id"));
                                    }
                                }
                            }).executeAsync();
                } else {
                    initChatAndSave(parseUser);
                }
            }
        });
    }

    /**
     * Realiza el logueo con TWITTER
     */
    private void loginWithTwitter() {
        ParseTwitterUtils.logIn(getActivity(), new LogInCallback() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                if (parseUser == null) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.msgNoLoginTwitter), Toast.LENGTH_LONG).show();
                } else if (parseUser.isNew()) {
                    Twitter twitterUser = ParseTwitterUtils.getTwitter();
                    if (twitterUser != null && twitterUser.getScreenName().length() > 0) {
                        parseUser.put("name", twitterUser.getScreenName());
                        parseUser.setUsername(twitterUser.getScreenName());
                        initChatAndSave(parseUser);
                    }
                } else {
                    initChatAndSave(parseUser);
                }
            }
        });
    }

    private void getAvatarFacebook(final ParseUser user, final String id) {
        AsyncTask<Void, Void, Bitmap> task = new AsyncTask<Void, Void, Bitmap>() {

            ProgressDialog progressDialog;

            @Override
            protected void onPreExecute() {
                progressDialog = new ProgressDialog(getActivity());
                progressDialog.setMessage("Ingresando...");
                progressDialog.show();
            }

            @Override
            protected Bitmap doInBackground(Void... params) {
                Bitmap bitmap = null;
                try {
                    URL url = new URL("http://graph.facebook.com/" + id + "/picture?type=large");
                    URLConnection urlConnection = url.openConnection();
                    urlConnection.setUseCaches(true);
                    urlConnection.connect();
                    InputStream inputStream = urlConnection.getInputStream();
                    BufferedInputStream buffer = new BufferedInputStream(inputStream);
                    bitmap = BitmapFactory.decodeStream(buffer);
                    buffer.close();
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return bitmap;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if (bitmap != null && user != null) {
                    ParseFile parseFile = new ParseFile("ParseZAvatar", getByteAvatar(bitmap));
                    parseFile.saveInBackground();
                    user.put("avatar", parseFile);
                    initChatAndSave(user);
                } else if (user != null) {
                    initChatAndSave(user);
                }

                progressDialog.dismiss();
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

    /**
     * Conecta al usuario al chat
     *
     * @param user
     */
    private void initChatAndSave(ParseUser user) {

        if (user == null) {
            return;
        }
        user.put("online", true);
        user.saveInBackground();
        saveInfoWelcome();
    }


    private void saveInfoWelcome() {
        List<Welcomedb> listWdb = new ArrayList<Welcomedb>();

        RuntimeExceptionDao<Welcomedb, Integer> dao = databaseHelper.getWelcomedbRuntimeDao();
        listWdb = dao.queryForAll();

        boolean guardar = true;

        //Si existe un registro de welcolme, no guardar otro mas
        for (Welcomedb w : listWdb) {
            guardar = false;
            break;
        }

        if (guardar) {
            Welcomedb wdb = new Welcomedb("SI");
            dao.create(wdb);
        }
        activity.finish();
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
