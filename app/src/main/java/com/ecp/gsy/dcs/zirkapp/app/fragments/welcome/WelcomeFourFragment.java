package com.ecp.gsy.dcs.zirkapp.app.fragments.welcome;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.activities.ManagerLogin;

/**
 * Created by Elder on 23/05/2015.
 */
public class WelcomeFourFragment extends Fragment implements View.OnClickListener {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_welcome_four, container, false);
        Button btn = (Button) view.findViewById(R.id.btnWelcome2);
        btn.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnWelcome2) {
            Intent intent = new Intent(getActivity(), ManagerLogin.class);
            startActivity(intent);
        }
    }
}


