package com.ecp.gsy.dcs.zirkapp.app.util.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ecp.gsy.dcs.zirkapp.app.R;
import com.parse.ParseUser;

/**
 * Created by Elder on 02/03/2015.
 */
public class RefreshDataProfileTask extends AsyncTask<ParseUser, Void, ParseUser> {

    private ImageView avatar;
    private byte[] byteImage;
    private ProgressDialog progressDialog;
    private Context context;
    private String messageDialog;
    private TextView txtWall;
    private TextView txtNombres;

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
        //Buscar perfil
        return parseUsers[0];
    }

    @Override
    protected void onPostExecute(ParseUser user) {
        if (user != null) {
            //Podemos obtener todos los datos del profile
            if (txtWall != null) {
                txtWall.setText(user.getString("wall"));
            }
            if (txtNombres != null) {
                txtNombres.setText(user.getString("name"));
                txtNombres.setVisibility(View.VISIBLE);
            }

            avatar.setImageDrawable(GlobalApplication.getAvatar(user));
        }
        if (context != null)
            progressDialog.dismiss();

    }

}
