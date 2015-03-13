package com.ecp.gsy.dcs.zirkapp.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ecp.gsy.dcs.zirkapp.app.util.beans.Zimess;
import com.ecp.gsy.dcs.zirkapp.app.util.locations.Location;
import com.ecp.gsy.dcs.zirkapp.app.util.locations.ManagerDistance;
import com.ecp.gsy.dcs.zirkapp.app.util.locations.ManagerGPS;
import com.ecp.gsy.dcs.zirkapp.app.util.task.GlobalApplication;
import com.ecp.gsy.dcs.zirkapp.app.util.task.RefreshDataCommentsTask;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

public class DetailZimessActivity extends Activity {

    private Zimess zimessDetail;
    private Activity activity;
    private ParseUser currentUser;
    private String userNameProfile;
    private EditText txtComment;
    private ListView listComment;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ManagerGPS managerGPS;
    private ImageView imgAvatar;
    private GlobalApplication globalApplication;
    private TextView lblTimePass,
            lblDistance,
            lblMessage,
            lblUsername,
            lblAliasUsuario;
    private ProgressBar progressBar;
    private String zimessId;
    private TextView lblCantComments;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_zimess);

        activity = this;
        managerGPS = new ManagerGPS(getApplicationContext());
        managerGPS.obtenertUbicacion();

        globalApplication = (GlobalApplication) getApplicationContext();
        currentUser = globalApplication.getCurrentUser();

        //Tomar Zimess enviado.
        zimessDetail = globalApplication.getTempZimess(); // (ZimessNew) getIntent().getSerializableExtra("zimess");
        if (zimessDetail != null && zimessDetail.getProfile() != null)
            userNameProfile = zimessDetail.getProfile().getString("name");

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle("Zimess");

        //Vaciar Zimess variable global
        globalApplication.setTempZimess(null);



       /* ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Cargando");
        progressDialog.setMessage("Espera...");
        progressDialog.show();
        while(!userFound) {
            if(userNameProfile != null){
                userFound = true;
                progressDialog.dismiss();
            }
        }*/

        inicializarCompUI();
    }

    @Override
    protected void onStart() {
        //Buscar username del Zimess
        /*ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.getInBackground(zimessDetail.getUserId(), new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                if (e == null) {
                    userNameProfile = parseUser.getUsername();
                    //Log.d("Parse.user", "Usuario encontrado "+parseUser.getUsername());
                } else {
                    Log.e("Parse.findUser", "Error la buscar el usuario");
                }
            }
        });*/

        findZimessComment();

        super.onStart();
    }

    private void inicializarCompUI() {
        //UI
        lblAliasUsuario = (TextView) findViewById(R.id.lblNombreUsuario);
        lblUsername = (TextView) findViewById(R.id.lblUserName);
        lblMessage = (TextView) findViewById(R.id.lblZimess);
        lblDistance = (TextView) findViewById(R.id.lblDistance);
        imgAvatar = (ImageView) findViewById(R.id.imgAvatarItem);
        //ImageView imgOptions = (ImageView) vista.findViewById(R.id.imgOptionsItem);
        lblTimePass = (TextView) findViewById(R.id.txtTiempo);
        lblCantComments = (TextView) findViewById(R.id.lblCantComments);

        txtComment = (EditText) findViewById(R.id.txtZimessComment);
        listComment = (ListView) findViewById(R.id.listZComments);

        progressBar = (ProgressBar) findViewById(R.id.progressLoad);

        refreshDataZimess(zimessDetail);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshComment);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                findZimessComment();
            }
        });

        //Set custom hint
        if (userNameProfile != null) {
            String msgUsername = new StringBuffer(getResources().getString(R.string.msgReply)).append(" ").append(userNameProfile).toString();
            txtComment.setHint(msgUsername);
        } else {
            txtComment.setHint(null);
        }

        //Avatar
        imgAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(AnimationUtils.loadAnimation(view.getContext(), R.anim.anim_image_click));
                Intent intent = new Intent(view.getContext(), UserProfileActivity.class);
                globalApplication.setTempZimess(zimessDetail);
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

    private void refreshDataZimess(Zimess zimess) {
        if (zimess.getProfile() != null) {
            lblAliasUsuario.setText(zimess.getProfile().getString("name"));
            //Estableciendo Imagen;
            byte[] byteImage = new byte[0];
            try {
                byteImage = zimess.getProfile().getParseFile("avatar").getData();
                Bitmap bmp = BitmapFactory.decodeByteArray(byteImage, 0, byteImage.length);
                imgAvatar.setImageBitmap(bmp);
            } catch (ParseException e1) {
                e1.printStackTrace();
            }
        }
        lblUsername.setText(zimess.getUser().getUsername());
        lblCantComments.setText(zimess.getUser().getUsername());


        //Manejando tiempos transcurridos
        String tiempoTranscurrido = globalApplication.getTimepass(zimess.getCreateAt());
        lblTimePass.setText(tiempoTranscurrido);

        //lblCreatedAt.setText(globalApplication.getDescFechaPublicacion(zimess.getCreateAt()));

        //Calcular distancia del Zimess remoto
        Location currentLocation = new Location(managerGPS.getLatitud(), managerGPS.getLongitud());

        Location zimessLocation = new Location(zimess.getLocation().getLatitude(), zimess.getLocation().getLongitude());
        ManagerDistance mDistance = new ManagerDistance(currentLocation, zimessLocation);
        lblDistance.setText(mDistance.getDistanciaToString());
        lblMessage.setText(zimess.getZimessText());

    }

    /**
     * Publica un comentario
     *
     * @param commentText
     */
    private void sendZimessComment(String commentText) {
        final ParseObject zimessObject = ParseObject.createWithoutData("ParseZimess", zimessDetail.getZimessId());
        ParseObject commentObject = new ParseObject("ParseZComment");
        commentObject.put("user", currentUser);
        commentObject.put("zimessId", zimessObject);
        commentObject.put("commentText", commentText);
        commentObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    txtComment.setText(null);
                    updateCantComments(zimessObject);
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Error al enviar tu comentario, reintentalo!",
                            Toast.LENGTH_SHORT).show();
                    Log.e("Parse.sendZimessComment", e.getMessage());
                }
            }
        });
        findZimessComment();
    }

    private void updateCantComments(ParseObject zimessObject) {
        if (zimessObject != null) {
            zimessObject.increment("cant_comment");
            zimessObject.saveInBackground();
        }

    }


    private void findZimessComment() {
        //Actualizar Lista de Comentarios
        new RefreshDataCommentsTask(this, progressBar, listComment, swipeRefreshLayout).execute(zimessDetail.getZimessId());
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
