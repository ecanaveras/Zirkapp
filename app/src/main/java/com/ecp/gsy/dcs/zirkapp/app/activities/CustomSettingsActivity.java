package com.ecp.gsy.dcs.zirkapp.app.activities;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;

import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.fragments.SettingsFragment;

/**
 * Created by Elder on 15/04/2015.
 */
public class CustomSettingsActivity extends AppCompatActivity {

    /*@Override
    public void onBuildHeaders(List<Header> target) {
        super.onBuildHeaders(target);
        loadHeadersFromResource(R.xml.pref_headers, target);
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setClickable(true);
        toolbar.setTitle(R.string.title_activity_settings);
        toolbar.setTitleTextColor(Color.parseColor("#FFFFFF"));
        toolbar.setNavigationIcon(getResIdFromAttribute(this, R.attr.homeAsUpIndicator));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        getFragmentManager().beginTransaction().replace(R.id.content_frame, new SettingsFragment()).commit();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

    }

    private static int getResIdFromAttribute(final Activity activity, final int attr) {
        if (attr == 0) {
            return 0;
        }
        final TypedValue typedvalueattr = new TypedValue();
        activity.getTheme().resolveAttribute(attr, typedvalueattr, true);
        return typedvalueattr.resourceId;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
