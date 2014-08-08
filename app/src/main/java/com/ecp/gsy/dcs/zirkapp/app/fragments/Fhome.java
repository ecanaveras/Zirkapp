package com.ecp.gsy.dcs.zirkapp.app.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.HomeReceiver;
import com.ecp.gsy.dcs.zirkapp.app.util.dialog.EditDistanceDialog;

/**
 * Created by Elder on 02/06/2014.
 */
public class Fhome extends Fragment implements View.OnClickListener, View.OnLongClickListener, EditDistanceDialog.EditDistanceDialogListener {

    private HomeReceiver homeReceiver;
    private ImageView imgAvatar;
    private int requestCode = 1;
    private TextView txtMsgNoLeidos, txtMsgMensajes, txtMsgCerca, txtUserCerca, txtDistMinima, txtDistMaxima;
    private Integer cantMensajesCerca = 0, cantUsuariosCerca = 0, distMin = 0, distMax = 0, msgNoLeido = 0, msgTotales = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        inicializarCompUI(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        homeReceiver = new HomeReceiver(txtMsgCerca);
        getActivity().registerReceiver(homeReceiver, new IntentFilter("actualizarzmess"));
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(homeReceiver);
    }

    private void inicializarCompUI(View view) {
        imgAvatar = (ImageView) view.findViewById(R.id.imgAvatar);
        imgAvatar.setOnLongClickListener(this);
        txtMsgNoLeidos = (TextView) view.findViewById(R.id.txtMsgNoLeidos);
        txtMsgMensajes = (TextView) view.findViewById(R.id.txtMsgMensajes);
        txtMsgCerca = (TextView) view.findViewById(R.id.txtMsgCerca);
        txtUserCerca = (TextView) view.findViewById(R.id.txtUserCerca);
        txtDistMinima = (TextView) view.findViewById(R.id.txtDistMinima);
        txtDistMaxima = (TextView) view.findViewById(R.id.txtDistMaxima);
        LinearLayout layoutMessages = (LinearLayout) view.findViewById(R.id.LyMensajes);
        LinearLayout layoutDistance = (LinearLayout) view.findViewById(R.id.LyInfoDistancia);
        layoutMessages.setOnClickListener(this);
        layoutDistance.setOnClickListener(this);
        setHasOptionsMenu(true);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.LyMensajes:
                Toast.makeText(getActivity(), "Clicked", Toast.LENGTH_SHORT).show();
                break;
            case R.id.LyInfoDistancia:
                showEditDistance();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onLongClick(View view) {
        if (view.getId() == R.id.imgAvatar) {
            Intent intent = null;
            //Verificar plataforma Android
            if (Build.VERSION.SDK_INT < 19) {
                //Android Jelly Bean 4.3 y Anteriores
                intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
            } else {
                //Android Kitkat 4.4
                intent = new Intent();
                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
            }

            intent.setType("image/*");
            startActivityForResult(intent, requestCode);
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && this.requestCode == requestCode) {
            imgAvatar.setImageURI(data.getData());
            imgAvatar.setTag(data.getData());
            Toast.makeText(getActivity(), "Avatar actualizado!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Show config Distance
     */
    private void showEditDistance() {
        //Set Values distance
        EditDistanceDialog editDistanceDialog = new EditDistanceDialog();
        editDistanceDialog.show(getFragmentManager(), "edit_distance_dialog");
    }

    @Override
    public void onDialogPositiveClick(EditDistanceDialog dialogFragment) {
        txtDistMinima.setText(String.valueOf(dialogFragment.getDistanceMin()));
        txtDistMaxima.setText(String.valueOf(dialogFragment.getDistanceMax()));
    }

    @Override
    public void onDialogNegativeClick(EditDistanceDialog dialogFragment) {

    }


    //<editor-fold desc="METHODS GETTER">
    public Integer getCantMensajesCerca() {
        return cantMensajesCerca;
    }

    public Integer getCantUsuariosCerca() {
        return cantUsuariosCerca;
    }

    public Integer getDistMin() {
        return distMin;
    }

    public Integer getDistMax() {
        return distMax;
    }

    public Integer getMsgNoLeido() {
        return msgNoLeido;
    }

    public Integer getMsgTotales() {
        return msgTotales;
    }
    //</editor-fold>

    //<editor-fold desc="METHODS SETTER">
    public void setCantMensajesCerca(Integer cantMensajesCerca) {
        this.cantMensajesCerca = cantMensajesCerca;
        txtMsgCerca.setText(cantMensajesCerca);
    }

    public void setCantUsuariosCerca(Integer cantUsuariosCerca) {
        this.cantUsuariosCerca = cantUsuariosCerca;
    }

    public void setDistMin(Integer distMin) {
        this.distMin = distMin;
    }

    public void setDistMax(Integer distMax) {
        this.distMax = distMax;
    }

    public void setMsgNoLeido(Integer msgNoLeido) {
        this.msgNoLeido = msgNoLeido;
    }

    public void setMsgTotales(Integer msgTotales) {
        this.msgTotales = msgTotales;
    }
    //</editor-fold>
}
