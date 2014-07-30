package com.ecp.gsy.dcs.zirkapp.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.beans.Zimess;

public class DetailZimessActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_zimess);
        String msgString = getIntent().getStringExtra("mensaje");
        String msgUsername = getIntent().getStringExtra("username");
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle("Zimess");

        //UI
        TextView msg = (TextView) findViewById(R.id.textView);
        msg.setText(msgString);
        TextView txtRespuesta = (TextView) findViewById(R.id.txtMsgRespuesta);
        if (msgUsername != null) {
            msgUsername = new StringBuffer(getResources().getString(R.string.msgReply)).append(" ").append(msgUsername).toString();
            txtRespuesta.setHint(msgUsername);
        } else {
            txtRespuesta.setHint(null);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail_zimess, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
