package com.ecp.gsy.dcs.zirkapp.app.util.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ecp.gsy.dcs.zirkapp.app.GlobalApplication;
import com.parse.ParseUser;

/**
 * Created by Elder on 02/03/2015.
 */
public class RefreshDataProfileTask extends AsyncTask<ParseUser, Void, ParseUser> {

    private ImageView avatar;
    private ProgressDialog progressDialog;
    private Context context;
    private String messageDialog;
    private TextView txtWall;
    private TextView txtNombres;
    private TextView txtCantZimess;
    private TextView txtCantVisits;
    private RoundedBitmapDrawable imgDrawable;

    public RefreshDataProfileTask(ImageView avatar) {
        this.avatar = avatar;
    }

    public RefreshDataProfileTask(ImageView avatar, TextView txtNombres) {
        this.avatar = avatar;
        this.txtNombres = txtNombres;
    }

    public RefreshDataProfileTask(ImageView avatar, TextView txtWall, TextView txtNombres, String messageDialog, Context context) {
        this.avatar = avatar;
        this.context = context;
        this.messageDialog = messageDialog;
        this.txtWall = txtWall;
        this.txtNombres = txtNombres;
    }


    public RefreshDataProfileTask(ImageView avatar, TextView txtWall, TextView txtNombres, TextView txtCantZimess, TextView txtCantVisits, String messageDialog, Context context) {
        this.avatar = avatar;
        this.txtWall = txtWall;
        this.txtNombres = txtNombres;
        this.txtCantZimess = txtCantZimess;
        this.txtCantVisits = txtCantVisits;
        this.messageDialog = messageDialog;
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        if (context != null) {
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage(messageDialog);
            progressDialog.show();
        }
    }

    @Override
    protected ParseUser doInBackground(ParseUser... parseUsers) {
        //Obtener Imagen
        imgDrawable = (GlobalApplication.getAvatar(parseUsers[0]));
        return parseUsers[0];
    }

    @Override
    protected void onPostExecute(ParseUser user) {
        //Set Imagen
        avatar.setImageDrawable(imgDrawable);

        if (user != null) {
            //Podemos obtener todos los datos del profile
            if (txtWall != null) {
                String wall = user.getString("wall");
                txtWall.setText(wall != null && !wall.isEmpty() ? wall : "I'm using Zirkapp!");

            }
            if (txtNombres != null) {
                String name = user.getString("name");
                txtNombres.setText(name != null ? name : user.getUsername());
                txtNombres.setVisibility(View.VISIBLE);
            }
            if (txtCantVisits != null) {
                txtCantVisits.setText(String.valueOf(user.getInt("count_visit")));
            }
            if (txtCantZimess != null) {
                txtCantZimess.setText(String.valueOf(user.getInt("count_zimess")));
            }
        }

        if (context != null)
            progressDialog.dismiss();

    }

}
