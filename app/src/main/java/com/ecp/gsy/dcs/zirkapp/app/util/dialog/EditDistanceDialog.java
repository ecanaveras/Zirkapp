package com.ecp.gsy.dcs.zirkapp.app.util.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.ecp.gsy.dcs.zirkapp.app.R;

/**
 * Created by elcapi05 on 08/08/2014.
 */
public class EditDistanceDialog extends DialogFragment {

    private NumberPicker distMinPicker;
    private NumberPicker distMaxPicker;
    private int distanceMin;
    private int distanceMax;

    public EditDistanceDialog() {
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        View vConfig = (View) layoutInflater.inflate(R.layout.layout_edit_distance, null);
        //Conf picker
        distMinPicker = (NumberPicker) vConfig.findViewById(R.id.pickerDistMin);
        distMaxPicker = (NumberPicker) vConfig.findViewById(R.id.pickerDistMax);
        distMinPicker.setMinValue(10);
        distMinPicker.setMaxValue(500);
        distMaxPicker.setMinValue(1000);
        distMaxPicker.setMaxValue(5000);
        distMaxPicker.setWrapSelectorWheel(true);
        distMaxPicker.setWeightSum(1000);
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
                            distanceMin = distMinPicker.getValue();
                            distanceMax = distMaxPicker.getValue();
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

        return alBuilder.create();
    }

    public int getDistanceMin() {
        return distanceMin;
    }

    public int getDistanceMax() {
        return distanceMax;
    }
}
