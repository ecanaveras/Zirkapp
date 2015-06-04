package com.ecp.gsy.dcs.zirkapp.app.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alertdialogpro.AlertDialogPro;
import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.fragments.ZimessFragment;
import com.ecp.gsy.dcs.zirkapp.app.util.beans.Zimess;
import com.ecp.gsy.dcs.zirkapp.app.util.locations.Location;
import com.ecp.gsy.dcs.zirkapp.app.util.locations.ManagerDistance;
import com.ecp.gsy.dcs.zirkapp.app.util.services.LocationService;
import com.ecp.gsy.dcs.zirkapp.app.util.task.DeleteDataZimessTask;
import com.ecp.gsy.dcs.zirkapp.app.GlobalApplication;
import com.ecp.gsy.dcs.zirkapp.app.util.task.RefreshDataCommentsTask;
import com.ecp.gsy.dcs.zirkapp.app.util.task.RefreshDataZimessTask;
import com.ecp.gsy.dcs.zirkapp.app.util.task.SendPushTask;
import com.gc.materialdesign.views.ButtonRectangle;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.HashMap;

public class DetailZimessActivity extends ActionBarActivity {

    private GlobalApplication globalApplication;
    private Zimess zimessDetail;
    private ParseUser currentUser, zimessUser;

    public boolean isZimessUpdated = false;

    //UI
    private EditText txtComment;
    private ListView listComment;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageView imgAvatar, imgComment;
    private TextView lblTimePass,
            lblDistance,
            lblMessage,
            lblUsername,
            lblAliasUsuario, lblCantComments;
    private ProgressBar progressBar;
    private ButtonRectangle btnSendComment;
    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_zimess);

        globalApplication = (GlobalApplication) getApplicationContext();
        currentUser = ParseUser.getCurrentUser();

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
            String name = zimessUser.getString("name") != null ? zimessUser.getString("name") : zimessUser.getUsername();
            String msgUsername = new StringBuffer(getResources().getString(R.string.msgReply)).append(" ").append(name).toString();
            txtComment.setHint(msgUsername);
        } else {
            txtComment.setHint(null);
        }

        //Avatar
        imgAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String transitionName = getResources().getString(R.string.imgNameTransition);
                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(DetailZimessActivity.this, view, transitionName);
                Intent intent = new Intent(view.getContext(), UserProfileActivity.class);
                globalApplication.setCustomParseUser(zimessDetail.getUser());
                ActivityCompat.startActivity(DetailZimessActivity.this, intent, optionsCompat.toBundle());
            }
        });

        btnSendComment = (ButtonRectangle) findViewById(R.id.btnSendZimessComment);
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
        imgAvatar.setImageDrawable(zimess.getAvatar());

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

        //Todo verificar utilidad
        //Calcular distancia del Zimess remoto
        Location currentLocation = getCurrentLocation();
        if (currentLocation != null) {
            Location zimessLocation = new Location(zimess.getLocation().getLatitude(), zimess.getLocation().getLongitude());
            ManagerDistance mDistance = new ManagerDistance(currentLocation, zimessLocation);
            lblDistance.setText(mDistance.getDistanciaToString());
        }
        lblMessage.setText(zimess.getZimessText());
    }

    /**
     * Publica un comentario
     *
     * @param commentText
     */
    private void sendZimessComment(final String commentText) {
        if (zimessDetail == null || currentUser == null) {
            Toast.makeText(this, getResources().getString(R.string.msgZimessNull), Toast.LENGTH_SHORT).show();
            return;
        }
        final ParseObject zimessObject = ParseObject.createWithoutData("ParseZimess", zimessDetail.getZimessId());
        ParseObject commentObject = new ParseObject("ParseZComment");
        commentObject.put("user", currentUser);
        commentObject.put("zimessId", zimessObject);
        commentObject.put("commentText", commentText);
        commentObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    updateCantComments();
                    try {
                        String receptorId = zimessObject.fetchIfNeeded().getParseUser("user").getObjectId();
                        if (receptorId != null && !currentUser.getObjectId().equals(receptorId)) {
                            String name = currentUser.getString("name") != null ? currentUser.getString("name") : currentUser.getUsername();
                            new SendPushTask(zimessDetail.getZimessId(), receptorId, currentUser.getObjectId(), name, commentText, SendPushTask.PUSH_COMMENT).execute();
                        }
                    } catch (ParseException e1) {
                        Log.e("find.parse.user", e.getMessage());
                    }
                    isZimessUpdated = true;
                    txtComment.setText(null);
                } else {
                    Toast.makeText(DetailZimessActivity.this, getResources().getString(R.string.msgCommentError), Toast.LENGTH_SHORT).show();
                    Log.e("Parse.Zimess.Comment", e.getMessage());
                }
                btnSendComment.setEnabled(true);
            }
        });
    }

    private void updateCantComments() {
        //Usando ParseCloud
        ParseCloud.callFunctionInBackground("ParseZComment", new HashMap<String, Object>(), new FunctionCallback<String>() {
            public void done(String result, ParseException e) {
                if (e == null) {
                    //System.out.println(result);
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

    /**
     * Elimina el Zimess Actual
     */
    private void deleteZimess() {
        AlertDialogPro.Builder alert = new AlertDialogPro.Builder(this);
        //alert.setTitle(getString(R.string.msgDeleting));
        alert.setMessage(getString(R.string.msgByeZimess));
        alert.setPositiveButton(getString(R.string.lblDelete), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (zimessDetail != null) {
                    //Delete
                    new DeleteDataZimessTask(DetailZimessActivity.this).execute(zimessDetail.getZimessId());
                    getCurrentLocation(false); //actualizar Zimess
                }
            }
        });

        alert.setNegativeButton(getString(R.string.lblCancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        alert.show();
    }


    /**
     * retorna la Ubicacion actual
     *
     * @return
     */
    private Location getCurrentLocation() {
        return getCurrentLocation(true);
    }

    /**
     * retorna la Ubicacion actual
     *
     * @return
     */
    private Location getCurrentLocation(boolean isManual) {
        Location location = null;
        if (LocationService.isRunning()) {
            LocationService locationService = LocationService.getInstance();
            android.location.Location tmpLocation = locationService.getCurrentLocation(isManual);
            location = new Location(tmpLocation.getLatitude(), tmpLocation.getLongitude());
        }
        return location;
    }

    @Override
    public void onBackPressed() {
        if (isZimessUpdated) {
            if (ZimessFragment.isRunning()) {
                ZimessFragment zf = ZimessFragment.getInstance();
                zf.findZimessAround(getCurrentLocation(), globalApplication.getSortZimess());
            }
        }
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_bar_delete_zimess:
                deleteZimess();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_detail_zimess, menu);
        if (menu == null) {
            return true;
        }
        if (currentUser.equals(zimessUser)) {
            menu.setGroupVisible(R.id.menuGroupDelete, true);
            menu.setGroupVisible(R.id.menuGroupDenounce, false);
        } else {
            menu.setGroupVisible(R.id.menuGroupDelete, false);
            menu.setGroupVisible(R.id.menuGroupDenounce, true);
        }
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
