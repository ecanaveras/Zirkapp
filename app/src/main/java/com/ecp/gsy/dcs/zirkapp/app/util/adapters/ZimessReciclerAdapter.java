package com.ecp.gsy.dcs.zirkapp.app.util.adapters;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ecp.gsy.dcs.zirkapp.app.DetailZimessActivity;
import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.beans.Zimess;
import com.ecp.gsy.dcs.zirkapp.app.util.locations.Location;
import com.ecp.gsy.dcs.zirkapp.app.util.locations.ManagerDistance;
import com.ecp.gsy.dcs.zirkapp.app.util.task.GlobalApplication;
import com.parse.ParseUser;

import java.util.List;

/**
 * Created by Elder on 20/03/2015.
 */
public class ZimessReciclerAdapter extends RecyclerView.Adapter<ZimessReciclerAdapter.ZimessViewHolder> {

    private List<Zimess> zimessList;
    private Fragment activity;
    private GlobalApplication globalApplication;
    private Location currentLocation;

    public ZimessReciclerAdapter(List<Zimess> zimessList, Fragment activity, Location currentLocation) {
        this.activity = activity;
        this.currentLocation = currentLocation;
        //globalApplication = (GlobalApplication) this.activity.getActivity().getApplicationContext();
    }

    @Override
    public ZimessViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View vista = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.listview_item_zimess_new, viewGroup, false);
        ZimessViewHolder holder = new ZimessViewHolder(vista);
        //holder.setActivity(activity);
        return holder;
    }

    @Override
    public void onBindViewHolder(ZimessViewHolder zimessViewHolder, int i) {
        Zimess zimess = zimessList.get(i);
        //zimessViewHolder.setZimess(zimess);

        zimessViewHolder.lblAliasUsuario.setText(zimess.getUser().getString("name"));
        //Estableciendo Imagen;
        zimessViewHolder.imgAvatar.setImageBitmap(zimess.getAvatar());

        zimessViewHolder.lblUsername.setText(zimess.getUser().getUsername());
        zimessViewHolder.lblCantComments.setText(Integer.toString(zimess.getCantComment()));

        //cambiar icono cuando hay comentarios
        if (zimess.getCantComment() != 0)
            zimessViewHolder.imgComment.setImageResource(R.drawable.ic_icon_response_color);
        else
            zimessViewHolder.imgComment.setImageResource(R.drawable.ic_icon_response);


        //Manejando tiempos transcurridos
        String tiempoTranscurrido = globalApplication.getTimepass(zimess.getCreateAt());
        zimessViewHolder.lblTimePass.setText(tiempoTranscurrido);

        //lblCreatedAt.setText(globalApplication.getDescFechaPublicacion(zimess.getCreateAt()));

        //Calcular distancia del Zimess remoto
        Location zimessLocation = new Location(zimess.getLocation().getLatitude(), zimess.getLocation().getLongitude());
        ManagerDistance mDistance = new ManagerDistance(currentLocation, zimessLocation);
        zimessViewHolder.lblDistance.setText(mDistance.getDistanciaToString());
        zimessViewHolder.lblMessage.setText(zimess.getZimessText());

    }

    @Override
    public int getItemCount() {
        return zimessList.size();
    }

    public static class ZimessViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private Zimess zimess;
        private Fragment activity;


        public TextView lblAliasUsuario,
                lblUsername,
                lblMessage,
                lblDistance,
                lblTimePass,
                lblCantComments;
        public ImageView imgComment,
                imgAvatar;
        //imgOptions


        public ZimessViewHolder(View vista) {
            super(vista);
            //vista.setOnClickListener(this);
            lblAliasUsuario = (TextView) vista.findViewById(R.id.lblNombreUsuario);
            lblUsername = (TextView) vista.findViewById(R.id.lblUserName);
            lblMessage = (TextView) vista.findViewById(R.id.lblZimess);
            lblDistance = (TextView) vista.findViewById(R.id.lblDistance);
            imgAvatar = (ImageView) vista.findViewById(R.id.imgAvatarItem);
            lblTimePass = (TextView) vista.findViewById(R.id.txtTiempo);
            lblCantComments = (TextView) vista.findViewById(R.id.lblCantComments);
            imgComment = (ImageView) vista.findViewById(R.id.imgComment);
        }

        public void setZimess(Zimess zimess) {
            this.zimess = zimess;
        }

        public void setActivity(Fragment activity) {
            this.activity = activity;
        }

        /**
         * Vamos al detalle del Zimess
         */
        private void gotoDetail() {
            GlobalApplication globalApplication = (GlobalApplication) activity.getActivity().getApplicationContext();
            globalApplication.setTempZimess(zimess);
            Intent intent = new Intent(activity.getActivity(), DetailZimessActivity.class);
            activity.startActivityForResult(intent, 105);//requestCodeUpdateZimess
        }

        @Override
        public void onClick(View v) {
            //gotoDetail();
        }
    }

}
