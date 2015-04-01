package com.ecp.gsy.dcs.zirkapp.app.fragments.loginSignup;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.beans.Welcomedb;
import com.ecp.gsy.dcs.zirkapp.app.util.database.DatabaseHelper;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.io.IOException;

/**
 * Created by Elder on 18/02/2015.
 */
public class LoginFragment extends Fragment {

    private Button btnLogin;
    private EditText txtUser;
    private EditText txtPassword;

    private String username = null;
    private String password = null;
    private Activity activity;
    private DatabaseHelper databaseHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_login, container, false);
        inicializarCompUI(view);
        activity = getActivity();

        //Database
        databaseHelper = OpenHelperManager.getHelper(getActivity(), DatabaseHelper.class);

        return view;
    }

    private void inicializarCompUI(View view) {

        btnLogin = (Button) view.findViewById(R.id.btnLogin);
        txtUser = (EditText) view.findViewById(R.id.txtUserLogin);
        txtPassword = (EditText) view.findViewById(R.id.txtPassLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                username = txtUser.getText().toString();
                password = txtPassword.getText().toString();

                if (username.isEmpty() || password.isEmpty()) {
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
                    public void done(ParseUser parseUser, ParseException e) {
                        if (parseUser != null) {
                            Welcomedb wdb = new Welcomedb("SI");
                            RuntimeExceptionDao<Welcomedb, Integer> dao = databaseHelper.getWelcomedbRuntimeDao();
                            dao.create(wdb);
                            activity.finish();
                        } else {
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                        progressDialog.dismiss();
                    }
                });
            }
        });
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
