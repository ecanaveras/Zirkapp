package com.ecp.gsy.dcs.zirkapp.app.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.activities.MainActivity;
import com.ecp.gsy.dcs.zirkapp.app.util.broadcast.HomeReceiver;

/**
 * Created by Elder on 02/06/2014.
 */
public class HomeFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener {

    private final static int FRAGMENT_ZIMESS = 1;
    private final static int FRAGMENT_INBOX = 2;

    private HomeReceiver homeReceiver;
    private ImageView imgAvatar;
    private int requestCode = 1;
    private TextView lblMsgNoLeidos, lblMsgMensajes, lblMsgCerca, lblUserCerca, lblDistMinima, lblDistMaxima, lblRango;
    private Integer cantMensajesCerca = 0, cantUsuariosCerca = 0, msgNoLeido = 0, msgTotales = 0;
    private NumberPicker distMinPicker, distMaxPicker;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        inicializarCompUI(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        homeReceiver = new HomeReceiver(lblMsgCerca);
        getActivity().registerReceiver(homeReceiver, new IntentFilter("actualizarzmess"));
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(homeReceiver);
    }

    private void inicializarCompUI(View view) {
        imgAvatar = (ImageView) view.findViewById(R.id.imgAvatarH);
        imgAvatar.setOnLongClickListener(this);
        lblMsgNoLeidos = (TextView) view.findViewById(R.id.txtMsgNoLeidos);
        lblMsgMensajes = (TextView) view.findViewById(R.id.txtMsgMensajes);
        lblMsgCerca = (TextView) view.findViewById(R.id.txtMsgCerca);
        lblUserCerca = (TextView) view.findViewById(R.id.txtUserCerca);
        lblDistMinima = (TextView) view.findViewById(R.id.txtDistMinima);
        lblDistMaxima = (TextView) view.findViewById(R.id.txtDistMaxima);
        //layouts de acceso rapido
        LinearLayout layoutInbox = (LinearLayout) view.findViewById(R.id.LyInfoInbox);
        LinearLayout layoutDistance = (LinearLayout) view.findViewById(R.id.LyInfoDistancia);
        LinearLayout layoutZimess = (LinearLayout) view.findViewById(R.id.LyInfoZimess);
        layoutInbox.setOnClickListener(this);
        layoutDistance.setOnClickListener(this);
        layoutZimess.setOnClickListener(this);
        setHasOptionsMenu(false);
    }

    @Override
    public void onClick(View view) {
        //Navegar y Conf desde los layout de home
        if (view instanceof LinearLayout) {
            MainActivity m = (MainActivity) getActivity();
            switch (view.getId()) {
                case R.id.LyInfoInbox:
                    m.selectItemDrawer(FRAGMENT_INBOX);
                    break;
                case R.id.LyInfoZimess:
                    m.selectItemDrawer(FRAGMENT_ZIMESS);
                    break;
                case R.id.LyInfoDistancia:
                    showEditDistance();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public boolean onLongClick(View view) {
        if (view.getId() == R.id.imgAvatarH) {
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

        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        View vConfig = (View) layoutInflater.inflate(R.layout.layout_edit_distance, null);
        //Conf picker
        distMinPicker = (NumberPicker) vConfig.findViewById(R.id.pickerDistMin);
        distMaxPicker = (NumberPicker) vConfig.findViewById(R.id.pickerDistMax);
        lblRango = (TextView) vConfig.findViewById(R.id.lblRango);
        //Min
        distMinPicker.setMinValue(0);
        distMinPicker.setMaxValue(3);
        distMinPicker.setDisplayedValues(new String[]{"10 m", "100 m", "300 m", "500 m"});
        distMinPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        distMinPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i2) {
                setTitleRangeChangeDistance(numberPicker.getValue(), distMaxPicker.getValue());
            }
        });
        //Max
        distMaxPicker.setMinValue(0);
        distMaxPicker.setMaxValue(3);
        distMaxPicker.setDisplayedValues(new String[]{"1 Km", "2 Km", "3 Km", "5 Km"});
        distMaxPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        distMaxPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i2) {
                setTitleRangeChangeDistance(distMinPicker.getValue(), numberPicker.getValue());
            }
        });

        AlertDialog.Builder alBuilder = new AlertDialog.Builder(getActivity());
        alBuilder.setView(vConfig);
        alBuilder.setTitle(R.string.lblConfigDistance);
        alBuilder
                .setCancelable(false)
                .setPositiveButton(R.string.lblOk, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (dialogInterface != null) {
                            Dialog dialog = Dialog.class.cast(dialogInterface);
                            distMinPicker = (NumberPicker) dialog.findViewById(R.id.pickerDistMin);
                            distMaxPicker = (NumberPicker) dialog.findViewById(R.id.pickerDistMax);
                            lblDistMinima.setText(String.valueOf(distMinPicker.getValue()));
                            lblDistMaxima.setText(String.valueOf(distMaxPicker.getValue()));
                            dialog.dismiss();
                        }
                    }
                })
                .setNegativeButton(R.string.lblCancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });

        AlertDialog dialog = alBuilder.create();
        dialog.show();
    }

    private void setTitleRangeChangeDistance(int valuemin, int valuemax) {
        String msg = "";
        lblRango.setTextColor(Color.BLACK);
        if (valuemin == 3 && valuemax == 0) {
            msg = "Rango mínimo, 'Pocos Zimess'";
            lblRango.setTextColor(Color.RED);
        }
        if (valuemin == 0 && valuemax == 3) {
            msg = "Rango maximo, 'Muchos Zimess'";
            lblRango.setTextColor(Color.GREEN);
        }
        lblRango.setText(msg);
    }

    //<editor-fold desc="METHODS GETTER">
    public Integer getCantMensajesCerca() {
        return cantMensajesCerca;
    }

    public Integer getCantUsuariosCerca() {
        return cantUsuariosCerca;
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
        lblMsgCerca.setText(cantMensajesCerca);
    }

    public void setCantUsuariosCerca(Integer cantUsuariosCerca) {
        this.cantUsuariosCerca = cantUsuariosCerca;
    }

    public void setMsgNoLeido(Integer msgNoLeido) {
        this.msgNoLeido = msgNoLeido;
    }

    public void setMsgTotales(Integer msgTotales) {
        this.msgTotales = msgTotales;
    }
    //</editor-fold>
}
