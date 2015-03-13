package com.ecp.gsy.dcs.zirkapp.app.util.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.beans.ZimessComment;
import com.parse.ParseException;

import java.util.ArrayList;

/**
 * Created by Elder on 24/02/2015.
 */
public class CommentsAdapter extends BaseAdapter {

    private ArrayList<ZimessComment> zimessCommentArrayL;
    private Context context;

    public CommentsAdapter(Context context, ArrayList<ZimessComment> zimessCommentArrayList) {
        this.context = context;
        this.zimessCommentArrayL = zimessCommentArrayList;
    }

    @Override
    public int getCount() {
        return zimessCommentArrayL.size();
    }

    @Override
    public Object getItem(int i) {
        return zimessCommentArrayL.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View vista = view;
        if(vista == null){
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            vista = layoutInflater.inflate(R.layout.listview_item_comments, viewGroup, false);
        }
        //1. Crear ZimessComment
        ZimessComment comment = zimessCommentArrayL.get(i);
        //2. Iniciar UI de la lista
        ImageView imgAvatar = (ImageView) vista.findViewById(R.id.imgCommentAvatarItem);
        TextView lblCommentUser = (TextView) vista.findViewById(R.id.lblCommentUserName);
        TextView lblCommentText = (TextView) vista.findViewById(R.id.lblCommentText);
        TextView lblCommentName = (TextView) vista.findViewById(R.id.lblCommentNombreUsuario);

        //3. Asignar valores
        lblCommentUser.setText(comment.getUserComment().getUsername().toString());
        lblCommentText.setText(comment.getCommentText().toString());
        if(comment.getProfile() != null) {
            lblCommentName.setText(comment.getProfile().getString("name"));
            //Estableciendo Imagen;
            byte[] byteImage = new byte[0];
            try {
                byteImage = comment.getProfile().getParseFile("avatar").getData();
                Bitmap bmp = BitmapFactory.decodeByteArray(byteImage, 0, byteImage.length);
                imgAvatar.setImageBitmap(bmp);
            } catch (ParseException e1) {
                e1.printStackTrace();
            }
        }

        return vista;
    }
}
