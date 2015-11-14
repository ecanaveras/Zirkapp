package com.ecp.gsy.dcs.zirkapp.app.fragments.profile;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ecp.gsy.dcs.zirkapp.app.R;

/**
 * Created by ecanaveras on 13/11/2015.
 */
public class InfoUserFragment extends Fragment {

    public InfoUserFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info_user, container, false);
        return view;
    }
}
