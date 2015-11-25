package com.ecp.gsy.dcs.zirkapp.app.fragments.profile;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ecp.gsy.dcs.zirkapp.app.GlobalApplication;
import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.beans.Zimess;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.models.ParseZimess;
import com.parse.ParseUser;

/**
 * Created by ecanaveras on 13/11/2015.
 */
public class InfoUserFragment extends Fragment {

    private GlobalApplication globalApplication;
    private ParseUser parseUser;
    private TextView txtCantVisitas;
    private TextView txtCantZimess;
    private TextView txtAboutMe;
    private TextView txtLikeMe;
    private TextView txtUserCity;

    public InfoUserFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info_user, container, false);

        globalApplication = (GlobalApplication) getActivity().getApplicationContext();
        parseUser = globalApplication.getCustomParseUser();

        inicializarCompUI(view);

        return view;
    }

    private void inicializarCompUI(View view) {
        txtAboutMe = (TextView) view.findViewById(R.id.txtAboutMe);
        txtLikeMe = (TextView) view.findViewById(R.id.txtLikeMe);
        txtUserCity = (TextView) view.findViewById(R.id.txtUserCity);

        txtCantVisitas = (TextView) view.findViewById(R.id.txtCountVisit);
        txtCantZimess = (TextView) view.findViewById(R.id.txtCountZimess);
        //progressBarLoad = (ProgressBar) view.findViewById(R.id.progressLoad);


    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (parseUser != null) {
            //Sobre mi...
            txtAboutMe.setText(parseUser.getString("about_me") != null ? parseUser.getString("about_me") : getString(R.string.InfoNotAvailable));
            txtLikeMe.setText(parseUser.getString("like_me") != null ? parseUser.getString("like_me") : getString(R.string.InfoNotAvailable));
            txtUserCity.setText(parseUser.getString("city") != null ? parseUser.getString("city") : getString(R.string.InfoNotAvailable));
            //Popularidad
            txtCantVisitas.setText(String.valueOf(parseUser.getInt("count_visit")));
            txtCantZimess.setText(String.valueOf(parseUser.getInt("count_zimess")));
        }
    }
}
