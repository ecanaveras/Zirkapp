package com.ecp.gsy.dcs.zirkapp.app.util.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.beans.ZimessComment;
import com.ecp.gsy.dcs.zirkapp.app.util.beans.ZimessNew;

import java.util.ArrayList;

/**
 * Created by Elder on 24/02/2015.
 */
public class CommentsAdapter extends BaseAdapter {

    private ArrayList<ZimessComment> zimessCommentArrayL;
    private Activity context;

    public CommentsAdapter(Activity context, ArrayList<ZimessComment> zimessCommentArrayList) {
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
        TextView lblCommentUser = (TextView) vista.findViewById(R.id.lblCommentUser);
        TextView lblCommentText = (TextView) vista.findViewById(R.id.lblCommentText);
        //3. Asignar valores
        lblCommentUser.setText(comment.getUserComment().getUsername().toString());
        lblCommentText.setText(comment.getCommentText().toString());

        return vista;
    }
}
