package com.ecp.gsy.dcs.zirkapp.app.fragments.loginSignup;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.beans.HandlerLogindb;
import com.ecp.gsy.dcs.zirkapp.app.util.beans.Welcomedb;
import com.ecp.gsy.dcs.zirkapp.app.util.database.DatabaseHelper;
import com.ecp.gsy.dcs.zirkapp.app.util.locations.Location;
import com.ecp.gsy.dcs.zirkapp.app.util.services.LocationService;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Elder on 18/02/2015.
 */
public class SignupFragment extends Fragment {

    private DatabaseHelper databaseHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_singup, container, false);
        inicializarCompUI(view);

        //Database
        databaseHelper = OpenHelperManager.getHelper(getActivity(), DatabaseHelper.class);

        return view;
    }

    private void inicializarCompUI(View view) {

        Button btnSingUp = (Button) view.findViewById(R.id.btnSingUp);
        final EditText txtCorreo = (EditText) view.findViewById(R.id.txtUserEmail);
        final EditText txtUsername = (EditText) view.findViewById(R.id.txtUserSignUp);
        final EditText txtPassword1 = (EditText) view.findViewById(R.id.txtPassSignUp1);
        final EditText txtPassword2 = (EditText) view.findViewById(R.id.txtPassSignUp2);

        //Sugerir email
        //txtCorreo.setText(getEmailAccount());

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

                //validacion parseUser
                if (username.trim().contains(" ")) {
                    txtUsername.setError(getResources().getString(R.string.msgNoSpaces));
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
                progressDialog.setMessage(getResources().getString(R.string.mgsLoging));
                progressDialog.show();

                //Crear usuario en Parse.

                ParseUser user = new ParseUser();
                user.setUsername(username.trim());
                user.setPassword(password1);
                user.setEmail(correo);
                //Conectar al chat
                Location currentLocation = getCurrentLocation();
                if (currentLocation != null) {
                    user.put("location", new ParseGeoPoint(currentLocation.getLatitud(), currentLocation.getLongitud()));
                    user.put("online", true);
                }

                user.signUpInBackground(new SignUpCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            saveSessionActive(true);
                            saveInfoWelcome();
                        } else {
                            Log.e(SignupFragment.class.getSimpleName(), e.getMessage());
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                        progressDialog.dismiss();
                    }
                });


            }
        });
    }


    /**
     * retorna la Ubicacion actual
     *
     * @return
     */
    private Location getCurrentLocation() {
        Location location = null;
        if (LocationService.isRunning()) {
            LocationService locationService = LocationService.getInstance();
            android.location.Location tmpLocation = locationService.getCurrentLocation();
            if (tmpLocation != null) {
                location = new Location(tmpLocation.getLatitude(), tmpLocation.getLongitude());
            }
        }
        return location;
    }

    private void saveInfoWelcome() {
//        Intent intent = new Intent();
//        intent.putExtra("loginOk", true);
//        getActivity().setResult(Activity.RESULT_OK, intent);

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
        getActivity().finish();
    }

    private void saveSessionActive(boolean sessionActive) {
        List<HandlerLogindb> listHldb = new ArrayList<>();

        Dao dao = null;
        try {
            dao = databaseHelper.getHandlerLogindbDao();
            listHldb = dao.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
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
        if (guardar) {
            if (dao != null) {
                try {
                    HandlerLogindb ldb = new HandlerLogindb(sessionActive);
                    dao.create(ldb);
                } catch (SQLException e) {
                    Log.e("Ormlite", "Error creando handlerLogin");
                }
            }
        }
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
