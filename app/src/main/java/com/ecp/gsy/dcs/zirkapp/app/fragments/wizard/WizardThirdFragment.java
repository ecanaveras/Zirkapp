package com.ecp.gsy.dcs.zirkapp.app.fragments.wizard;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.activities.MainActivity;
import com.ecp.gsy.dcs.zirkapp.app.activities.ManagerWizard;
import com.parse.ParseUser;

/**
 * Created by ecanaveras on 22/09/2015.
 */
public class WizardThirdFragment extends Fragment {

    private EditText txtCiudad, txtLike, txtAboutMe, txtFace, txtTwi, txtGoo, txtInst, txtLink, txtSkyp;
    private ParseUser currentUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wizard_third, container, false);

        currentUser = ParseUser.getCurrentUser();

        inicializarCompUI(view);

        loadDataUser();

        return view;
    }

    private void inicializarCompUI(View view) {
        Button btnNext = (Button) view.findViewById(R.id.btnFinish);
        Button btnBack = (Button) view.findViewById(R.id.btnBack);

        txtCiudad = (EditText) view.findViewById(R.id.txtUserCity);
        txtLike = (EditText) view.findViewById(R.id.txtLikeMe);
        txtAboutMe = (EditText) view.findViewById(R.id.txtAboutMe);
        txtFace = (EditText) view.findViewById(R.id.txtAccountFace);
        txtTwi = (EditText) view.findViewById(R.id.txtAccountTwit);
        txtGoo = (EditText) view.findViewById(R.id.txtAccountGoo);
        txtInst = (EditText) view.findViewById(R.id.txtAccountInst);
        txtLink = (EditText) view.findViewById(R.id.txtAccountLink);
        txtSkyp = (EditText) view.findViewById(R.id.txtAccountSkyp);

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Cambia el fragment
                saveDataUser();
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                getActivity().finish();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Cambia el fragment
                ManagerWizard managerWizard = (ManagerWizard) getActivity();
                managerWizard.mViewPager.setCurrentItem(managerWizard.mViewPager.getCurrentItem() - 1);
            }
        });
    }


    private void saveDataUser() {
        if (currentUser != null) {
            String ciudad = txtCiudad.getText().toString();
            String likes = txtLike.getText().toString();
            String about = txtAboutMe.getText().toString();
            //Redes sociales
            String face = txtFace.getText().toString();
            String twi = txtTwi.getText().toString();
            String goo = txtGoo.getText().toString();
            String inst = txtInst.getText().toString();
            String sky = txtSkyp.getText().toString();
            String link = txtLink.getText().toString();

            currentUser.put("city", ciudad);
            currentUser.put("like_me", likes);
            currentUser.put("about_me", about);
            //Redes
            currentUser.put("facebook_account", face);
            currentUser.put("twitter_account", twi);
            currentUser.put("googleplus_account", goo);
            currentUser.put("instagram_account", inst);
            currentUser.put("skype_account", sky);
            currentUser.put("linkedin_account", link);

            currentUser.saveInBackground();
        }

    }

    private void loadDataUser() {
        if (currentUser != null) {
            txtCiudad.setText(currentUser.getString("city"));
            txtLike.setText(currentUser.getString("like_me"));
            txtAboutMe.setText(currentUser.getString("about_me"));
            //Redes
            txtFace.setText(currentUser.getString("facebook_account"));
            txtTwi.setText(currentUser.getString("twitter_account"));
            txtGoo.setText(currentUser.getString("googleplus_account"));
            txtInst.setText(currentUser.getString("instagram_account"));
            txtSkyp.setText(currentUser.getString("skype_account"));
            txtLink.setText(currentUser.getString("linkedin_account"));
        }
    }
}

