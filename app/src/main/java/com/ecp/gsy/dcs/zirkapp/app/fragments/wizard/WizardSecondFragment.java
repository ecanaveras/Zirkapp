package com.ecp.gsy.dcs.zirkapp.app.fragments.wizard;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ecp.gsy.dcs.zirkapp.app.R;

/**
 * Created by ecanaveras on 22/09/2015.
 */
public class WizardSecondFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wizard_second, container, false);
        return view;
    }
}
