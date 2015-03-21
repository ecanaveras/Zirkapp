package com.ecp.gsy.dcs.zirkapp.app;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
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
import com.ecp.gsy.dcs.zirkapp.app.util.services.ManagerGPS;
import com.ecp.gsy.dcs.zirkapp.app.util.task.GlobalApplication;
import com.ecp.gsy.dcs.zirkapp.app.util.task.RefreshDataCommentsTask;
import com.ecp.gsy.dcs.zirkapp.app.util.task.RefreshDataZimessTask;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.HashMap;

public class DetailZimessActivity extends ActionBarActivity {

    private Zimess zimessDetail;
    private Activity activity;
    private ParseUser currentUser, zimessUser;
    private EditText txtComment;
    private ListView listComment;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ManagerGPS managerGPS;
    private ImageView imgAvatar, imgComment;
    private GlobalApplication globalApplication;
    private TextView lblTimePass,
            lblDistance,
            lblMessage,
            lblUsername,
            lblAliasUsuario, lblCantComments;
    private ProgressBar progressBar;
    private String zimessId;
    private ImageButton btnSendComment;
    private boolean isAddedComment = false;
    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_zimess);

        activity = this;
        globalApplication = (GlobalApplication) getApplicationContext();
        currentUser = globalApplication.getCurrentUser();

        //Tomar Zimess enviado.
        zimessDetail = globalApplication.getTempZimess();
        //Tomar nombre del usuario del Zimess
        zimessUser = zimessDetail.getUser();

        inicializarCompUI();
        findZimessComment();
    }

    private void inicializarCompUI() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        toolbar.setTitle("Inf. Zimess");

        //UI
        lblAliasUsuario = (TextView) findViewById(R.id.lblNombreUsuario);
        lblUsername = (TextView) findViewById(R.id.lblUserName);
        lblMessage = (TextView) findViewById(R.id.lblZimess);
        lblDistance = (TextView) findViewById(R.id.lblDistance);
        imgAvatar = (ImageView) findViewById(R.id.imgAvatarItem);
        //ImageView imgOptions = (ImageView) vista.findViewById(R.id.imgOptionsItem);
        lblTimePass = (TextView) findViewById(R.id.txtTiempo);
        lblCantComments = (TextView) findViewById(R.id.lblCantComments);
        imgComment = (ImageView) findViewById(R.id.imgComment);

        txtComment = (EditText) findViewById(R.id.txtZimessComment);
        listComment = (ListView) findViewById(R.id.listZComments);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshComment);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                findZimessComment();
            }
        });

        listComment.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition = (listComment == null || listComment.getChildCount() == 0) ? 0 : listComment.getChildAt(0).getTop();
                swipeRefreshLayout.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
            }
        });

        progressBar = (ProgressBar) findViewById(R.id.progressLoad);

        refreshDataZimess(zimessDetail);

        //Set custom hint
        if (zimessUser != null) {
            String name = zimessUser.getString("name");
            String msgUsername = new StringBuffer(getResources().getString(R.string.msgReply)).append(" ").append(!name.isEmpty() ? name : zimessUser.getUsername()).toString();
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

        btnSendComment = (ImageButton) findViewById(R.id.btnSendZimessComment);
        btnSendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String comment = txtComment.getText().toString();
                if (!comment.isEmpty()) {
                    btnSendComment.setEnabled(false);
                    sendZimessComment(txtComment.getText().toString());
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Escribe tu comentario!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void refreshDataZimess(Zimess zimess) {
        //Estableciendo Imagen;
        imgAvatar.setImageBitmap(zimess.getAvatar());

        lblAliasUsuario.setText(zimess.getUser().getString("name"));
        lblUsername.setText(zimess.getUser().getUsername());
        lblCantComments.setText(Integer.toString(zimess.getCantComment()));

        //cambiar icono cuando hay comentarios
        if (zimess.getCantComment() > 0)
            imgComment.setImageResource(R.drawable.ic_icon_response_color);
        else
            imgComment.setImageResource(R.drawable.ic_icon_response);
        //Manejando tiempos transcurridos
        String tiempoTranscurrido = globalApplication.getTimepass(zimess.getCreateAt());
        lblTimePass.setText(tiempoTranscurrido);

        //lblCreatedAt.setText(globalApplication.getDescFechaPublicacion(zimess.getCreateAt()));

        //Calcular distancia del Zimess remoto
        managerGPS = new ManagerGPS(this);
        if (managerGPS.isEnableGetLocation()) {
            Location currentLocation = new Location(managerGPS.getLatitud(), managerGPS.getLongitud());

            Location zimessLocation = new Location(zimess.getLocation().getLatitude(), zimess.getLocation().getLongitude());
            ManagerDistance mDistance = new ManagerDistance(currentLocation, zimessLocation);
            lblDistance.setText(mDistance.getDistanciaToString());
            lblMessage.setText(zimess.getZimessText());
        } else {
            managerGPS.gpsShowSettingsAlert();
        }

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
                    isAddedComment = true;
                    txtComment.setText(null);
                    updateCantComments();
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Error al enviar tu comentario, reintentalo!",
                            Toast.LENGTH_SHORT).show();
                    Log.e("Parse.sendZimessComment", e.getMessage());
                }
                btnSendComment.setEnabled(true);
            }
        });
        //findZimessComment();
    }

    private void updateCantComments() {
        //Usando ParseCloud
        ParseCloud.callFunctionInBackground("ParseZComment", new HashMap<String, Object>(), new FunctionCallback<String>() {
            public void done(String result, ParseException e) {
                if (e == null) {
                    System.out.println(result);
                } else {
                    Log.e("Parze.Cloud.ZComment", e.getMessage());
                }
            }
        });

        findZimessComment();
        findZimessUpdated();
    }


    private void findZimessComment() {
        //Actualizar Lista de Comentarios
        new RefreshDataCommentsTask(this, progressBar, listComment, swipeRefreshLayout).execute(zimessDetail.getZimessId());
    }

    private void findZimessUpdated() {
        //Actualizar el Zimess recien comentado
        new RefreshDataZimessTask(this, zimessDetail).execute();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("updateZimessOk", isAddedComment);
        setResult(Activity.RESULT_OK, intent);
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail_zimess, menu);
        return true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {

        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {

        }
    }
}
