package com.ecp.gsy.dcs.zirkapp.app.fragments.wizard;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.activities.ManagerWizard;
import com.parse.ParseUser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by ecanaveras on 22/09/2015.
 */
public class WizardFirstFragment extends Fragment {

    private EditText txtUsername;
    private EditText txtNombres;
    private EditText txtEmail;
    private RadioGroup radioGroup;
    private static EditText txtFecha;
    private ParseUser currentUser;
    private RadioButton rbtnM, rbtnF;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wizard_firts, container, false);

        inicializarCompUI(view);

        currentUser = ParseUser.getCurrentUser();

        loadDataUser();

        return view;
    }

    private void inicializarCompUI(View view) {
        txtUsername = (EditText) view.findViewById(R.id.txtUsername);
        txtNombres = (EditText) view.findViewById(R.id.txtUserNombres);
        txtEmail = (EditText) view.findViewById(R.id.txtUserEmail);
        txtFecha = (EditText) view.findViewById(R.id.txtUserBirthday);
        radioGroup = (RadioGroup) view.findViewById(R.id.radioG);
        rbtnF = (RadioButton) view.findViewById(R.id.rbtnFemenino);
        rbtnM = (RadioButton) view.findViewById(R.id.rbtnMasculino);


        txtFecha.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                DatePickerFragment newFragment = null;
                if (txtFecha.getText() != null && !txtFecha.getText().toString().isEmpty()) {
                    try {
                        newFragment = new DatePickerFragment(new SimpleDateFormat("dd/MM/yyyy").parse(txtFecha.getText().toString().trim()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } else {
                    newFragment = new DatePickerFragment(currentUser.getDate("birthday"));
                }
                if (newFragment != null) {
                    DialogFragment dialogFragment = newFragment;
                    dialogFragment.show(getActivity().getFragmentManager(), "datePicker");
                }
            }
        });

        Button btnNext = (Button) view.findViewById(R.id.btnNext);

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (saveDataUser()) {
                    //Cambia el fragment
                    ManagerWizard managerWizard = (ManagerWizard) getActivity();
                    managerWizard.mViewPager.setCurrentItem(managerWizard.mViewPager.getCurrentItem() + 1);
                }
            }
        });
    }

    private boolean saveDataUser() {
        if (currentUser != null) {
            String correo = txtEmail.getText().toString();
            String username = txtUsername.getText().toString().trim();
            String nombres = txtNombres.getText().toString().trim();
            String fecNac = txtFecha.getText().toString().trim();


            //Validacion Basica -Deben estar los datos diligenciados-
            int error = 0;

            if (username.isEmpty()) {
                txtUsername.setError(getResources().getString(R.string.msgSignUpUserEmpty));
                error++;
            }

            if (correo.isEmpty()) {
                txtEmail.setError(getResources().getString(R.string.msgSignUpEmailEmpty));
                error++;
            } else if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                txtEmail.setError(getResources().getString(R.string.msgEmailInvalid));
                error++;
            }
            if (nombres.isEmpty()) {
                txtNombres.setError(getResources().getString(R.string.msgProfiNameEmty));
                error++;
            }
            if (fecNac.isEmpty()) {
                txtFecha.setError(getResources().getString(R.string.msgProfiBirthdayEmty));
                error++;
            }

            if (error != 0) {
                return false;
            }

            Calendar c = Calendar.getInstance();
            try {
                c.setTime(new SimpleDateFormat("dd/MM/yyyy").parse(txtFecha.getText().toString().trim()));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            currentUser.setEmail(correo);
            currentUser.put("name", nombres);
            currentUser.put("birthday", c.getTime());
            currentUser.put("gender", (radioGroup.getCheckedRadioButtonId() == R.id.rbtnMasculino ? "M" : "F"));

            currentUser.saveInBackground();
        }

        return true;

    }

    private void loadDataUser() {
        if (currentUser != null) {
            txtUsername.setText(currentUser.getUsername());
            txtEmail.setText(currentUser.getString("email"));
            txtNombres.setText(currentUser.getString("name"));
            if (currentUser.getDate("birthday") != null) {
                txtFecha.setText(new SimpleDateFormat("dd/MM/yyyy").format(currentUser.getDate("birthday")));
            }
            radioGroup.clearCheck();
            String gender = currentUser.getString("gender");
            System.out.println("GENDER " + gender);
            if (gender != null && gender.equals("F")) {
                rbtnF.setChecked(true);
            } else {
                rbtnM.setChecked(true);
            }
        }
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        private Date fecha;

        public DatePickerFragment(Date fecha) {
            this.fecha = fecha;
        }

        public DatePickerFragment() {
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int year, month, day;
            final Calendar c = Calendar.getInstance();
            if (fecha == null) {
                // Use the current date as the default date in the picker
                year = c.get(Calendar.YEAR) - 10;
            } else {
                c.setTime(fecha);
                year = c.get(Calendar.YEAR);
            }
            month = c.get(Calendar.MONTH);
            day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
            String patter = "%02d/%02d/%d";
            txtFecha.setText(String.format(patter, day, month + 1, year));
        }

    }
}
