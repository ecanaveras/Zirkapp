package com.ecp.gsy.dcs.zirkapp.app.fragments.loginSignup;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ecp.gsy.dcs.zirkapp.app.R;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

/**
 * Created by Elder on 18/02/2015.
 */
public class Flogin extends Fragment {

    private Button btnLogin;
    private EditText txtUser;
    private EditText txtPassword;

    private String username = null;
    private String password = null;
    private Activity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        inicializarCompUI(view);
        activity = getActivity();
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

                ParseUser.logInInBackground(username, password, new LogInCallback() {
                    @Override
                    public void done(ParseUser parseUser, ParseException e) {
                        if (parseUser != null) {
                            Intent intent = new Intent();
                            intent.putExtra("loginOk", true);
                            activity.setResult(Activity.RESULT_OK, intent);
                            activity.finish();
                        } else {
                            Toast.makeText(getActivity(),
                                    "There was an error logging in.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }
}
