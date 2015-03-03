package com.ecp.gsy.dcs.zirkapp.app.util.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.TextView;

import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.images.RoundedImageView;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

/**
 * Created by Elder on 02/03/2015.
 */
public class RefreshDataProfileTask extends AsyncTask<ParseUser, Void, String> {

    private RoundedImageView avatar;
    private List<ParseObject> parseObjectList;
    private byte[] byteImage;
    private ProgressDialog progressDialog;
    private Context context;
    private String messageDialog;
    private TextView txtWall;
    private TextView txtNombres;

    public RefreshDataProfileTask(RoundedImageView avatar) {
        this.avatar = avatar;
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
    protected String doInBackground(ParseUser... parseUsers) {
        //Buscamos datos en Parse
        ParseQuery<ParseObject> query = ParseQuery.getQuery("ParseZProfile");
        query.whereEqualTo("user", parseUsers[0]);
        parseObjectList = null;
        try {
            parseObjectList = query.find();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "finish";
    }

    @Override
    protected void onPostExecute(String s) {
        if (parseObjectList.size() > 0) {
            //Podemos obtener todos los datos del profile
            if(txtWall != null){
                txtWall.setText(parseObjectList.get(0).get("wall").toString());
            }
            if(txtNombres != null){
                txtNombres.setText(parseObjectList.get(0).get("name").toString());
            }
            //Setter Imagen
            byteImage = new byte[0];
            try {
                byteImage = parseObjectList.get(0).getParseFile("avatar").getData();
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
