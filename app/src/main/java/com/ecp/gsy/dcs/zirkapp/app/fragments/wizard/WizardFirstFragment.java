package com.ecp.gsy.dcs.zirkapp.app.fragments.wizard;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.activities.ManagerLogin;
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

    private EditText lblUser;
    private EditText lblUserNombre;
    private EditText lblEmail;
    private static EditText lblFecha;
    private ParseUser currentUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wizard_firts, container, false);

        inicializarCompUI(view);

        currentUser = ParseUser.getCurrentUser();

        loadDataUser();

        return view;
    }

    private void inicializarCompUI(View view) {
        lblUser = (EditText) view.findViewById(R.id.txtUsername);
        lblUserNombre = (EditText) view.findViewById(R.id.txtUserNombres);
        lblEmail = (EditText) view.findViewById(R.id.txtUserEmail);
        lblFecha = (EditText) view.findViewById(R.id.txtUserEdad);

        lblFecha.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                DatePickerFragment newFragment = null;
                if (lblFecha.getText() != null && !lblFecha.getText().toString().isEmpty()) {
                    try {
                        newFragment = new DatePickerFragment(new SimpleDateFormat("dd/MM/yyyy").parse(lblFecha.getText().toString().trim()));
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
                //Cambia el fragment
                ManagerWizard managerWizard = (ManagerWizard) getActivity();
                managerWizard.mViewPager.setCurrentItem(managerWizard.mViewPager.getCurrentItem() + 1);
            }
        });
    }


    private void loadDataUser() {
        if (currentUser != null) {
            lblUser.setText(currentUser.getUsername());
            lblEmail.setText(currentUser.getString("email"));
            lblUserNombre.setText(currentUser.getString("name"));
            if (currentUser.getDate("birthday") != null) {
                lblFecha.setText(new SimpleDateFormat("dd/MM/yyyy").format(currentUser.getDate("birthday")));
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
            lblFecha.setText(String.format(patter, day, month + 1, year));
        }

    }
}
