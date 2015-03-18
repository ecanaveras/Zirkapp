package com.ecp.gsy.dcs.zirkapp.app.util.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.images.RoundedImageView;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.FindParseObject;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * Created by Elder on 02/03/2015.
 */
public class RefreshDataProfileTask extends AsyncTask<ParseUser, Void, ParseUser> {

    private RoundedImageView avatar;
    private byte[] byteImage;
    private ProgressDialog progressDialog;
    private Context context;
    private String messageDialog;
    private TextView txtWall;
    private TextView txtNombres;

    public RefreshDataProfileTask(RoundedImageView avatar) {
        this.avatar = avatar;
    }

    public RefreshDataProfileTask(RoundedImageView avatar, TextView txtNombres) {
        this.avatar = avatar;
        this.txtNombres = txtNombres;
    }

    public RefreshDataProfileTask(RoundedImageView avatar, TextView txtWall, TextView txtNombres, String messageDialog, Context context) {
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
                txtWall.setText(user.get("wall").toString());
            }
            if (txtNombres != null) {
                txtNombres.setText(user.get("name").toString());
                txtNombres.setVisibility(View.VISIBLE);
            }
            if (getAvatar(user) != null) {
                avatar.setImageBitmap(getAvatar(user));
            } else {
                avatar.setImageResource(R.drawable.ic_user_male);
            }

        }
        if (context != null)
            progressDialog.dismiss();

    }

    /**
     * Retorna la imagen del usuario
     *
     * @return
     */
    public Bitmap getAvatar(ParseUser currentUser) {
        if (currentUser != null && currentUser.getParseFile("avatar") != null) {
            byte[] byteImage;
            try {
                byteImage = currentUser.getParseFile("avatar").getData();
                if (byteImage != null) {
                    return BitmapFactory.decodeByteArray(byteImage, 0, byteImage.length);
                }
            } catch (ParseException e) {
                Log.e("Parse.avatar.exception", e.getMessage());
            } catch (OutOfMemoryError e) {
                Log.e("Parse.avatar.outmemory", e.toString());
            }
        }
        return null;
    }

}
