package com.ecp.gsy.dcs.zirkapp.app.util.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;

import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.images.RoundedImageView;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.FindParseObject;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * Created by Elder on 02/03/2015.
 */
public class RefreshDataProfileTask extends AsyncTask<ParseUser, Void, ParseObject> {

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
    protected ParseObject doInBackground(ParseUser... parseUsers) {
        //Buscar perfil
        return FindParseObject.findProfile(parseUsers[0]);
    }

    @Override
    protected void onPostExecute(ParseObject objectProfile) {
        if (objectProfile != null) {
            //Podemos obtener todos los datos del profile
            if (txtWall != null) {
                txtWall.setText(objectProfile.get("wall").toString());
            }
            if (txtNombres != null) {
                txtNombres.setText(objectProfile.get("name").toString());
                txtNombres.setVisibility(View.VISIBLE);
            }
            //Setter Imagen
            byteImage = new byte[0];
            try {
                byteImage = objectProfile.getParseFile("avatar").getData();
                Bitmap bmp = BitmapFactory.decodeByteArray(byteImage, 0, byteImage.length);
                avatar.setImageBitmap(bmp);
            } catch (ParseException e1) {
                e1.printStackTrace();
            }
        } else {
            if (byteImage == null) {
                avatar.setImageResource(R.drawable.ic_user_male);
            }
        }

        if (context != null) {
            progressDialog.dismiss();
        }
    }

}
