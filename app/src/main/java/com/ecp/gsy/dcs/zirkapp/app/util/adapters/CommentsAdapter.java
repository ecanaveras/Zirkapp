package com.ecp.gsy.dcs.zirkapp.app.util.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.ecp.gsy.dcs.zirkapp.app.GlobalApplication;
import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.models.ParseZComment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Elder on 24/02/2015.
 */
public class CommentsAdapter extends BaseAdapter {

    private List<ParseZComment> zimessCommentArrayL;
    private Context context;
    private GlobalApplication globalApplication;
    private EditText txtComentario;
    private List<String> arryUserNames = new ArrayList<>();

    public CommentsAdapter(Context context, List<ParseZComment> zimessCommentArrayL, EditText txtComentario) {
        this.context = context;
        this.zimessCommentArrayL = zimessCommentArrayL;
        this.txtComentario = txtComentario;
        globalApplication = (GlobalApplication) context.getApplicationContext();
    }

    public CommentsAdapter(Context context, List<ParseZComment> zimessCommentList) {
        this.context = context;
        this.zimessCommentArrayL = zimessCommentList;
        globalApplication = (GlobalApplication) context.getApplicationContext();
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
        if (vista == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            vista = layoutInflater.inflate(R.layout.itemlist_comments, viewGroup, false);
        }
        //1. Crear ZimessComment
        ParseZComment comment = zimessCommentArrayL.get(i);
        //2. Iniciar UI de la lista
        ItemViewHolder viewHolder = new ItemViewHolder(vista);
        //3. Asignar valores
        viewHolder.username = comment.getUser().getUsername();
        viewHolder.lblCommentUser.setText(comment.getUser().getUsername());
        viewHolder.lblCommentText.setText(comment.getCommentText());
        viewHolder.lblNumComment.setText(Integer.toString(i + 1));

        String name = comment.getUser().getString("name");
        viewHolder.lblCommentName.setText(name != null ? name : comment.getUser().getUsername());
        //Estableciendo Imagen;
        viewHolder.imgAvatar.setImageDrawable(comment.getAvatar());

        //Manejando tiempos transcurridos
        String tiempoTranscurrido = globalApplication.getTimepass(comment.getCreatedAt());
        viewHolder.lblTimePass.setText(tiempoTranscurrido);

        return vista;
    }

    private class ItemViewHolder implements View.OnClickListener {

        public TextView lblCommentUser, lblCommentText, lblCommentName, lblTimePass, lblNumComment;
        public ImageView imgAvatar, imgQuoteUser;
        public String username;

        public ItemViewHolder(View vista) {
            imgAvatar = (ImageView) vista.findViewById(R.id.imgCommentAvatarItem);
            lblCommentUser = (TextView) vista.findViewById(R.id.lblCommentUserName);
            lblCommentText = (TextView) vista.findViewById(R.id.lblCommentText);
            lblCommentName = (TextView) vista.findViewById(R.id.lblCommentNombreUsuario);
            lblTimePass = (TextView) vista.findViewById(R.id.txtCommentTiempo);
            lblNumComment = (TextView) vista.findViewById(R.id.lblNumComment);
            imgQuoteUser = (ImageView) vista.findViewById(R.id.imgQuoteUser);
            //vista.setOnClickListener(this);
            imgQuoteUser.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v instanceof ImageView) {
                v.startAnimation(AnimationUtils.loadAnimation(context, R.anim.anim_image_click));
                boolean userExist = false;
                for (String usern : arryUserNames) {
                    if (username.equals(usern)) {
                        userExist = true;
                        break;
                    }
                }
                //Validar si ha sido borrado
                if (userExist && txtComentario.getText() != null && txtComentario.getText().toString().contains(username)) {
                    userExist = true;
                } else {
                    userExist = false;
                }

                if (!userExist) {
                    arryUserNames.add(username);
                    String textTmp = txtComentario.getText().toString();
                    txtComentario.setText(new StringBuffer(textTmp).append(textTmp.isEmpty() ? "@" : " @").append(username));
                    txtComentario.setSelection(txtComentario.getText().length());
                } else {
                    //Toast.makeText(context, context.getResources().getString(R.string.msgUserQuoted), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}

