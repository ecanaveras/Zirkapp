package com.ecp.gsy.dcs.zirkapp.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ecp.gsy.dcs.zirkapp.app.util.beans.ZimessNew;
import com.ecp.gsy.dcs.zirkapp.app.util.task.GlobalApplication;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

public class DetailZimessActivity extends Activity {

    private ZimessNew zimessDetail;
    private Activity activity;
    private String currentUserId;
    private String userNameParse;
    private EditText txtComment;
    private boolean userFound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_zimess);

        activity = this;

        final GlobalApplication globalApplication = (GlobalApplication) getApplicationContext();
        currentUserId = globalApplication.getCurrentUser().getObjectId();

        //Tomar Zimess enviado.
        zimessDetail = globalApplication.getTempZimess(); // (ZimessNew) getIntent().getSerializableExtra("zimess");
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle("Zimess");

        //Vaciar Zimess variable global
        globalApplication.setTempZimess(null);



       /* ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Cargando");
        progressDialog.setMessage("Espera...");
        progressDialog.show();
        while(!userFound) {
            if(userNameParse != null){
                userFound = true;
                progressDialog.dismiss();
            }
        }*/

        inicializarCompUI();
    }

    @Override
    protected void onStart() {
        //Buscar username del Zimess
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.getInBackground(zimessDetail.getUserId(), new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                if (e == null) {
                    userNameParse = parseUser.getUsername();
                    //Log.d("Parse.user", "Usuario encontrado "+parseUser.getUsername());
                } else {
                    Log.e("Parse.findUser", "Error la buscar el usuario");
                }
            }
        });

        if (userNameParse != null) {
            String msgUsername = new StringBuffer(getResources().getString(R.string.msgReply)).append(" ").append(userNameParse).toString();
            txtComment.setHint(msgUsername);
        } else {
            txtComment.setHint(null);
        }
        super.onStart();
    }

    private void inicializarCompUI() {
        //UI
        TextView lblZimessText = (TextView) findViewById(R.id.lblZimessText);
        lblZimessText.setText(zimessDetail.getZimessText());
        txtComment = (EditText) findViewById(R.id.txtZimessComment);

        //Avatar
        ImageView avatar = (ImageView) findViewById(R.id.imgAvatarDZ);
        avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(AnimationUtils.loadAnimation(view.getContext(), R.anim.anim_image_click));
                Intent intent = new Intent(view.getContext(), UserProfileActivity.class);
                startActivity(intent);
            }
        });

        ImageButton btnSendComment = (ImageButton) findViewById(R.id.btnSendZimessComment);
        btnSendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String comment = txtComment.getText().toString();
                if (!comment.isEmpty()) {
                    sendZimessComment(txtComment.getText().toString());
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Escribe tu comentario!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Publica un comentario
     *
     * @param commentText
     */
    private void sendZimessComment(String commentText) {
        ParseObject commentObject = new ParseObject("ParseZComment");
        commentObject.put("user", ParseUser.getCurrentUser());
        commentObject.put("zimessId", ParseObject.createWithoutData("ParseZimess", zimessDetail.getZimessId()));
        commentObject.put("commentText", commentText);
        commentObject.saveInBackground();
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
