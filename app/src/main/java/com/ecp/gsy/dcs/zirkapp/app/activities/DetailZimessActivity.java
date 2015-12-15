package com.ecp.gsy.dcs.zirkapp.app.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SoundEffectConstants;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ecp.gsy.dcs.zirkapp.app.GlobalApplication;
import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.fragments.ZimessFragment;
import com.ecp.gsy.dcs.zirkapp.app.util.beans.Zimess;
import com.ecp.gsy.dcs.zirkapp.app.util.locations.Location;
import com.ecp.gsy.dcs.zirkapp.app.util.locations.ManagerDistance;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.DataParseHelper;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.models.ParseZComment;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.models.ParseZimess;
import com.ecp.gsy.dcs.zirkapp.app.util.services.LocationService;
import com.ecp.gsy.dcs.zirkapp.app.util.task.DeleteDataZimessTask;
import com.ecp.gsy.dcs.zirkapp.app.util.task.RefreshDataCommentsTask;
import com.ecp.gsy.dcs.zirkapp.app.util.task.SendPushTask;
import com.parse.DeleteCallback;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DetailZimessActivity extends AppCompatActivity { // implements ObservableScrollViewCallbacks {

    private GlobalApplication globalApplication;
    private ParseZimess zimessDetail;
    private ParseUser currentUser, zimessUser;

    public boolean isZimessUpdated = false;

    //UI
    private EditText txtComment;
    private ListView listComment;
    private LinearLayout layoutBodyZimess;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageView imgAvatar, imgComment;
    private TextView lblTimePass,
            lblDistance,
            lblMessage,
            lblUsername,
            lblAliasUsuario, lblCantComments;
    private ProgressBar progressBar;
    private ImageButton btnSendComment;
    private Toolbar toolbar;
    private String contextClass = null;
    private SharedPreferences preferences;
    private ImageView imgFav;
    private TextView lblCantFavs;
    private boolean isZimessPreloaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_zimess);

        globalApplication = (GlobalApplication) getApplicationContext();
        currentUser = ParseUser.getCurrentUser();

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        isZimessPreloaded = getIntent().getBooleanExtra("zimess_preloaded", true);

        zimessDetail = globalApplication.getTempZimess();

        //Tomar lblNombreUsuario del usuario del Zimess
        zimessUser = zimessDetail.getUser();

        //Toma el lblNombreUsuario de la clase que llama el detail
        contextClass = getIntent().getStringExtra("contextClass");

        inicializarCompUI();
        findZimessComment();
        refreshDataZimess(zimessDetail);
    }

    private void inicializarCompUI() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        toolbar.setTitle("Inf. Zimess");

        //UI
        layoutBodyZimess = (LinearLayout) findViewById(R.id.layoutBodyZimess);
        lblAliasUsuario = (TextView) findViewById(R.id.lblNombreUsuario);
        lblUsername = (TextView) findViewById(R.id.lblUserName);
        lblMessage = (TextView) findViewById(R.id.lblZimess);
        lblDistance = (TextView) findViewById(R.id.lblDistance);
        imgAvatar = (ImageView) findViewById(R.id.imgAvatarItem);
        //ImageView imgOptions = (ImageView) vista.findViewById(R.id.imgOptionsItem);
        lblTimePass = (TextView) findViewById(R.id.txtTiempo);
        lblCantComments = (TextView) findViewById(R.id.lblCantComments);
        imgComment = (ImageView) findViewById(R.id.imgComment);
        //Fav
        imgFav = (ImageView) findViewById(R.id.imgFav);
        lblCantFavs = (TextView) findViewById(R.id.lblCantFavs);

        txtComment = (EditText) findViewById(R.id.txtZimessComment);
        listComment = (ListView) findViewById(R.id.listZComments);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshComment);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                findZimessComment();
            }
        });

        registerForContextMenu(listComment);
        //listComment.setScrollViewCallbacks(this);
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
                globalApplication.setProfileParseUser(zimessDetail.getUser());
                ActivityCompat.startActivity(DetailZimessActivity.this, intent, optionsCompat.toBundle());
            }
        });

        imgFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFavorite(zimessDetail);
            }
        });

        btnSendComment = (ImageButton) findViewById(R.id.btnSendZimessComment);
        btnSendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String comment = txtComment.getText().toString();
                if (!comment.isEmpty()) {
                    btnSendComment.setEnabled(false);
                    sendZimessComment(comment);
                } else {
                    Toast.makeText(DetailZimessActivity.this, getResources().getString(R.string.msgCommentEmpty), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void refreshDataZimess(ParseZimess zimess) {
        //Estableciendo Imagen;
        globalApplication.setAvatarRoundedResize(zimess.getUser().getParseFile("avatar"), imgAvatar, 100, 100);

        lblMessage.setText(zimess.getZimessText());

        String name = zimess.getUser().getString("name") != null ? zimess.getUser().getString("name") : zimess.getUser().getUsername();
        lblAliasUsuario.setText(name);
        lblUsername.setText(zimess.getUser().getUsername());
        lblCantComments.setText(Integer.toString(zimess.getCantComment()));
        lblCantFavs.setText(zimess.getCantFavorite() > 0 ? Integer.toString(zimess.getCantFavorite()) : "");

        //cambiar icono cuando hay comentarios
        if (zimess.getCantComment() > 0) {
            imgComment.setImageResource(R.drawable.ic_icon_response_color);
        } else {
            imgComment.setImageResource(R.drawable.ic_icon_response);
        }

        //cambiar icono cuando es favorito
        if (zimess.isMyFavorite(currentUser.getObjectId())) {
            imgFav.setImageResource(R.drawable.ic_icon_fav_color);
        } else {
            imgFav.setImageResource(R.drawable.ic_icon_fav);
        }
        //Manejando tiempos transcurridos
        String tiempoTranscurrido = globalApplication.getTimepass(zimess.getCreatedAt());
        lblTimePass.setText(tiempoTranscurrido);

        //lblCreatedAt.setText(globalApplication.getDescFechaPublicacion(zimess.getCreateAt()));
        if (!isZimessPreloaded) {
            //Calcular distancia del Zimess remoto
            Location zimessLocation = new Location(zimess.getLocation().getLatitude(), zimess.getLocation().getLongitude());
            ManagerDistance mDistance = new ManagerDistance(getCurrentLocation(), zimessLocation);
            zimess.setDescDistancia(mDistance.getDistanciaToString());
            zimess.setValueDistancia(mDistance.getDistancia());
            lblDistance.setText(zimess.getDescDistancia());
            lblDistance.setBackgroundResource(getResourceRibbon(mDistance.getDistancia()));
        } else {
            lblDistance.setText(zimess.getDescDistancia());
            lblDistance.setBackgroundResource(getResourceRibbon(zimess.getValueDistancia()));
        }
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
        final ParseZComment commentObject = new ParseZComment();
        commentObject.setUser(currentUser);
        commentObject.setZimessId(zimessDetail);
        commentObject.setCommentText(commentText);
        commentObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    updateCantComments();
                    zimessDetail.fetchInBackground(new GetCallback<ParseZimess>() {
                        @Override
                        public void done(ParseZimess parseZimess, ParseException e) {
                            refreshDataZimess(parseZimess);
                        }
                    });
                    try {
                        String receptorId = zimessDetail.fetchIfNeeded().getParseUser("user").getObjectId();
                        final String receptorName = zimessDetail.fetchIfNeeded().getParseUser("user").getUsername();
                        final String name = currentUser.getString("name") != null ? currentUser.getString("name") : currentUser.getUsername();
                        if (receptorId != null && !currentUser.getObjectId().equals(receptorId)) {
                            //Envia la notificacion al creador del Zimess
                            new SendPushTask(zimessDetail.getObjectId(), zimessDetail.getUser(), currentUser.getObjectId(), name, commentText, SendPushTask.PUSH_COMMENT).execute();
                        }
                        //Envia la notificacion a los citados en la respuesta
                        new AsyncTask<String, Void, List<String>>() {

                            @Override
                            protected List doInBackground(String... params) {
                                ArrayList<String> usernames = new ArrayList<>();
                                if (commentText.contains("@")) {
                                    Pattern pattern = Pattern.compile("^@[a-zA-Z0-9_-]{6,20}"); //Tomar parseUser
                                    for (String userName : commentText.split("\\s")) {
                                        Matcher matcher = pattern.matcher(userName);
                                        if (matcher.find()) {
                                            usernames.add(matcher.group(0).replace("@", ""));
                                        }
                                    }
                                }
                                return usernames;
                            }

                            @Override
                            protected void onPostExecute(List<String> usernames) {
                                //Enviar la notificacion a cada uno de los usuarios citados.
                                if (usernames.size() > 0) {
                                    for (String username : usernames) {
                                        if (!currentUser.getUsername().equals(username) && !receptorName.equals(username)) {
                                            ParseUser user = DataParseHelper.findUserName(username);
                                            if (user != null)
                                                new SendPushTask(zimessDetail.getObjectId(), user, currentUser.getObjectId(), name, commentText, SendPushTask.PUSH_QUOTE).execute();
                                        }
                                    }
                                }
                            }
                        }.execute();
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
        findZimessComment();
        //Usando ParseCloud
        ParseCloud.callFunctionInBackground("ParseZComment", new HashMap<String, Object>(), new FunctionCallback<String>() {
            public void done(String result, ParseException e) {
                if (e != null) {
                    Log.e("Parse.Cloud.ZComment", e.getMessage());
                }
            }
        });
    }

    /**
     * Busca los comentarios en Parse
     */
    private void findZimessComment() {
        //Actualizar Lista de Comentarios
        new RefreshDataCommentsTask(this, progressBar, listComment, swipeRefreshLayout, txtComment).execute(zimessDetail);
    }

    /**
     * Elimina el Zimess Actual
     */
    private void deleteZimess() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
        //alert.setTitle(getString(R.string.msgDeleting));
        alert.setMessage(getString(R.string.msgByeZimess));
        alert.setPositiveButton(getString(R.string.lblDelete), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (zimessDetail != null) {
                    //Delete
                    new DeleteDataZimessTask(DetailZimessActivity.this).execute(zimessDetail);
                    if (contextClass != null && contextClass.equals(MyZimessActivity.class.getSimpleName())) {
                        if (MyZimessActivity.isRunning()) {
                            MyZimessActivity.getInstance().findZimessCurrentUser();
                        }
                    } else {
                        getCurrentLocation(); //actualizar Zimess
                    }
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
     * Elimina el comentario solicitado
     *
     * @param parseZComment
     */
    private void deleteComment(final ParseZComment parseZComment) {
        if (parseZComment != null) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
            alert.setMessage(getString(R.string.msgByeComment));
            alert.setPositiveButton(getString(R.string.lblDelete), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    final ProgressDialog dialog = new ProgressDialog(DetailZimessActivity.this);
                    dialog.setMessage(getResources().getString(R.string.msgDeleting));
                    dialog.show();
                    parseZComment.deleteInBackground(new DeleteCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                updateCantComments();
                                if (zimessDetail != null) {
                                    zimessDetail.fetchInBackground(new GetCallback<ParseZimess>() {
                                        @Override
                                        public void done(ParseZimess parseZimess, ParseException e) {
                                            refreshDataZimess(parseZimess);
                                        }
                                    });
                                }
                                Toast.makeText(DetailZimessActivity.this, getResources().getString(R.string.msgCommentDeleteOk), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(DetailZimessActivity.this, getResources().getString(R.string.msgCommentDeleteFailed), Toast.LENGTH_SHORT).show();
                            }
                            dialog.dismiss();
                        }
                    });
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
    }

    /**
     * Permite agregar y quitar el fav
     *
     * @param zimess
     */
    private void addFavorite(ParseZimess zimess) {
        HashMap params = new HashMap<String, Object>();
        params.put("zimessId", zimess.getObjectId());
        //Marcar/desmarcar como favorito
        if (zimess != null && zimess.isMyFavorite(currentUser.getObjectId())) {
            zimess.removeFavorites(Arrays.asList(currentUser.getObjectId()));
            zimess.saveInBackground();
            callParseFunction("DelZimessFavorite", params);

            imgFav.setImageResource(R.drawable.ic_icon_fav);
            if (zimess.getCantFavorite() <= 1) {
                lblCantFavs.setText("");
            } else {
                lblCantFavs.setText(Integer.toString(zimess.getCantFavorite() - 1));
            }
        } else {
            //Actualizar los datos del Zimess
            zimess.addFavorites(currentUser.getObjectId());
            zimess.saveInBackground();

            callParseFunction("AddZimessFavorite", params);

            lblCantFavs.setText(Integer.toString(zimess.getCantFavorite() + 1));
            imgFav.setImageResource(R.drawable.ic_icon_fav_color);

            if (!zimess.getUser().getObjectId().equals(currentUser.getObjectId())) {
                String nameCurrentUser = currentUser.getString("name") != null ? currentUser.getString("name") : currentUser.getUsername();
                new SendPushTask(zimess.getObjectId(), zimess.getUser(), currentUser.getObjectId(), String.format("%s le gusta tu Zimes", nameCurrentUser), String.format("%s...", zimess.getZimessText().length() > 60 ? zimess.getZimessText().substring(0, 60) : zimess.getZimessText()), SendPushTask.PUSH_FAVORITE).execute();
            }
        }

        //Actualizar el Zimess
        zimess.fetchIfNeededInBackground();
        //Toast.makeText(context, "Add favorite", Toast.LENGTH_SHORT).show();
    }


    /**
     * retorna la Ubicacion actual
     *
     * @return
     */
    private Location getCurrentLocation() {
        Location location = null;
        if (LocationService.isRunning()) {
            LocationService locationService = LocationService.getInstance();
            android.location.Location tmpLocation = locationService.getCurrentLocation();
            location = new Location(tmpLocation.getLatitude(), tmpLocation.getLongitude());
        }
        return location;
    }

    private void callParseFunction(final String nameFunction, HashMap<String, Object> params) {
        ParseCloud.callFunctionInBackground(nameFunction, params, new FunctionCallback<String>() {
            public void done(String result, ParseException e) {
                if (e != null) {
                    Log.e("Parse.Cloud." + nameFunction, e.getMessage());
                }
            }
        });
    }

    private int getResourceRibbon(double distancia) {
        Double rango = getRango();
        if (distancia <= rango) { //Verde
            return R.drawable.ic_ribbon_green;
        } else if (distancia > rango && distancia <= (rango * 2)) {
            return R.drawable.ic_ribbon_yellow;
        }
        return R.drawable.ic_ribbon_red;
    }

    private double getRango() {
        int dist_max = Integer.parseInt(preferences.getString("max_dist_list", "10"));
        double rango = (dist_max * 1000) / 3;
        return rango;
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
        if (currentUser.getObjectId().equals(zimessUser.getObjectId())) {
            menu.setGroupVisible(R.id.menuGroupDelete, true);
            menu.setGroupVisible(R.id.menuGroupDenounce, false);
        } else {
            menu.setGroupVisible(R.id.menuGroupDelete, false);
            menu.setGroupVisible(R.id.menuGroupDenounce, true);
        }
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        ParseZComment parseZComment = null;
        if (v.getId() == R.id.listZComments) {
            AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) menuInfo;
            parseZComment = (ParseZComment) listComment.getAdapter().getItem(acmi.position);
        }
        this.getMenuInflater().inflate(R.menu.menu_contextual_comments, menu);
        if (menu == null || parseZComment == null) {
            return;
        }
        if (parseZComment.getUser().getObjectId().equals(currentUser.getObjectId()) || currentUser.getObjectId().equals(zimessUser.getObjectId())) {
            menu.setGroupVisible(R.id.menuGroupDelete, true);
        } else {
            menu.setGroupVisible(R.id.menuGroupDelete, false);
        }
        super.onCreateContextMenu(menu, v, menuInfo);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {

        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        ParseZComment parseZComment = (ParseZComment) listComment.getAdapter().getItem(acmi.position);
        switch (item.getItemId()) {
            case R.id.ctx_view_profile:
                if (parseZComment != null) {
                    globalApplication.setProfileParseUser(parseZComment.getUser());
                    Intent intent = new Intent(this, UserProfileActivity.class);
                    startActivity(intent);
                }
                return true;
            case R.id.ctx_delete_commentario:
                deleteComment(parseZComment);
                return true;
            case R.id.ctx_report_comment:
                Toast.makeText(this, "Proximamente...", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    /*@Override
    public void onScrollChanged(int i, boolean b, boolean b1) {
    }

    @Override
    public void onDownMotionEvent() {
    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
        if (scrollState == ScrollState.UP) {
            if (layoutBodyZimess.isShown()) {
                //layoutBodyZimess.setVisibility(View.GONE);
                layoutBodyZimess.animate().translationY(-200);
            }
        }

        if (scrollState == ScrollState.DOWN) {
            if (!layoutBodyZimess.isShown()) {
                //layoutBodyZimess.setVisibility(View.VISIBLE);
                layoutBodyZimess.animate().translationY(0);
            }
        }
    }*/
}
