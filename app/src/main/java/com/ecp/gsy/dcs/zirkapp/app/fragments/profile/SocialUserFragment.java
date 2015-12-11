package com.ecp.gsy.dcs.zirkapp.app.fragments.profile;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ecp.gsy.dcs.zirkapp.app.GlobalApplication;
import com.ecp.gsy.dcs.zirkapp.app.R;
import com.parse.ParseUser;

/**
 * Created by ecanaveras on 13/11/2015.
 */
public class SocialUserFragment extends Fragment {

    private GlobalApplication globalApplication;
    private ParseUser parseUser;
    private TextView txtAccountFace;
    private TextView txtAccountTwit;
    private TextView txtAccountGoo;
    private TextView txtAccountInst;
    private TextView txtAccountSkyp;
    private TextView txtAccountLink;

    public SocialUserFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_social_user, container, false);

        globalApplication = (GlobalApplication) getActivity().getApplicationContext();
        parseUser = globalApplication.getProfileParseUser();

        inicializarCompUI(view);

        return view;
    }

    private void inicializarCompUI(View view) {
        txtAccountFace = (TextView) view.findViewById(R.id.txtAccountFace);
        txtAccountTwit = (TextView) view.findViewById(R.id.txtAccountTwit);
        txtAccountGoo = (TextView) view.findViewById(R.id.txtAccountGoo);
        txtAccountInst = (TextView) view.findViewById(R.id.txtAccountInst);
        txtAccountSkyp = (TextView) view.findViewById(R.id.txtAccountSkyp);
        txtAccountLink = (TextView) view.findViewById(R.id.txtAccountLink);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (parseUser != null) {
            txtAccountFace.setText(parseUser.getString("facebook_account") != null ? parseUser.getString("facebook_account") : getString(R.string.InfoNotAvailable));
            txtAccountTwit.setText(parseUser.getString("twitter_account") != null ? parseUser.getString("twitter_account") : getString(R.string.InfoNotAvailable));
            txtAccountGoo.setText(parseUser.getString("googleplus_account") != null ? parseUser.getString("googleplus_account") : getString(R.string.InfoNotAvailable));
            txtAccountInst.setText(parseUser.getString("instagram_account") != null ? parseUser.getString("instagram_account") : getString(R.string.InfoNotAvailable));
            txtAccountSkyp.setText(parseUser.getString("skype_account") != null ? parseUser.getString("skype_account") : getString(R.string.InfoNotAvailable));
            txtAccountLink.setText(parseUser.getString("linkedin_account") != null ? parseUser.getString("linkedin_account") : getString(R.string.InfoNotAvailable));
        }
    }
}
