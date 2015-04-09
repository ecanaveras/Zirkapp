package com.ecp.gsy.dcs.zirkapp.app.fragments.loginSignup;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ecp.gsy.dcs.zirkapp.app.MainActivity;
import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.beans.Welcomedb;
import com.ecp.gsy.dcs.zirkapp.app.util.database.DatabaseHelper;
import com.gc.materialdesign.views.ButtonRectangle;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Elder on 18/02/2015.
 */
public class SignupFragment extends Fragment {

    private Activity activity;
    private DatabaseHelper databaseHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_singup, container, false);
        inicializarCompUI(view);
        activity = getActivity();

        //Database
        databaseHelper = OpenHelperManager.getHelper(getActivity(), DatabaseHelper.class);

        return view;
    }

    private void inicializarCompUI(View view) {

        ButtonRectangle btnSingUp = (ButtonRectangle) view.findViewById(R.id.btnSingUp);
        final EditText txtCorreo = (EditText) view.findViewById(R.id.txtUserEmail);
        final EditText txtUsername = (EditText) view.findViewById(R.id.txtUserSignUp);
        final EditText txtPassword1 = (EditText) view.findViewById(R.id.txtPassSignUp1);
        final EditText txtPassword2 = (EditText) view.findViewById(R.id.txtPassSignUp2);

        //Sugerir email
        txtCorreo.setText(getEmailAccount());

        btnSingUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String correo = txtCorreo.getText().toString();
                String username = txtUsername.getText().toString().trim();
                String password1 = txtPassword1.getText().toString().trim();
                String password2 = txtPassword2.getText().toString().trim();

                //Validación Basica -Deben estar los datos diligenciados-
                int error = 0;

                if (correo.isEmpty()) {
                    txtCorreo.setError(getResources().getString(R.string.msgSignUpEmailEmpty));
                    error++;
                } else if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                    txtCorreo.setError(getResources().getString(R.string.msgEmailInvalid));
                    error++;
                }
                if (username.isEmpty()) {
                    txtUsername.setError(getResources().getString(R.string.msgSignUpUserEmpty));
                    error++;
                }
                if (password1.isEmpty() || password2.isEmpty()) {
                    txtPassword1.setError(getResources().getString(R.string.msgSignUpPassEmpty));
                    txtPassword2.setError(getResources().getString(R.string.msgSignUpPassEmpty));
                    error++;
                }

                if (error != 0) {
                    return;
                }


                //Validacion de Password
                //Minimo 6 caracteres
                if (password1.trim().length() < 6) {
                    txtPassword1.setError(getResources().getString(R.string.msgLoginPassInvalid));
                    return;
                }

                //Contraseñas no coinciden
                if (!password1.equals(password2)) {
                    txtPassword2.setError(getResources().getString(R.string.msgSignUpPassDif));
                    return;
                }

                final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                progressDialog.setMessage("Ingresando...");
                progressDialog.show();

                //Crear usuario en Parse.

                ParseUser user = new ParseUser();
                user.setUsername(username);
                user.setPassword(password1);
                user.setEmail(correo);
                user.put("online", false);

                user.signUpInBackground(new SignUpCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
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

    /**
     * Obtiene la cuenta de gmail de Android para sugerirla en el signup
     *
     * @return
     */
    private String getEmailAccount() {
        AccountManager manager = AccountManager.get(getActivity());
        Account[] accounts = manager.getAccountsByType("com.google");
        List<String> possibleEmails = new LinkedList<String>();

        for (Account account : accounts) {
            // TODO: Check possibleEmail against an email regex or treat
            // account.name as an email address only for certain account.type
            // values.
            possibleEmails.add(account.name);
        }

        if (!possibleEmails.isEmpty() && possibleEmails.get(0) != null) {
            return possibleEmails.get(0);
        } else {
            return null;
        }
    }

}
