package com.ecp.gsy.dcs.zirkapp.app.util.adapters;

import android.app.Activity;
import android.content.Context;
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
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.List;

/**
 * Created by Elder on 20/03/2015.
 */
public class ZimessReciclerAdapter extends RecyclerView.Adapter<ZimessReciclerAdapter.ZimessViewHolder> {

    private List<Zimess> zimessList;
    private Context context;
    private GlobalApplication globalApplication;
    private Location currentLocation;

    public ZimessReciclerAdapter(List<Zimess> zimessList, Context context, Location currentLocation) {
        this.context = context;
        this.currentLocation = currentLocation;
        this.zimessList = zimessList;
        globalApplication = (GlobalApplication) context.getApplicationContext();

    }

    @Override
    public ZimessViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View vista = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cardview_item_zimess, viewGroup, false);
        ZimessViewHolder holder = new ZimessViewHolder(vista);
        holder.setContext(context);
        return holder;
    }

    @Override
    public void onBindViewHolder(ZimessViewHolder zimessViewHolder, int i) {
        Zimess zimess = zimessList.get(i);
        zimessViewHolder.setZimess(zimess);

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
        return zimessList != null ? zimessList.size() : 0;
    }

    public static class ZimessViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private Zimess zimess;
        private Context context;


        public TextView lblAliasUsuario,
                lblUsername,
                lblMessage,
                lblDistance,
                lblTimePass,
                lblCantComments;
        public ImageView imgComment;
        public RoundedImageView imgAvatar;
        //imgOptions


        public ZimessViewHolder(View vista) {
            super(vista);
            vista.setOnClickListener(this);
            lblAliasUsuario = (TextView) vista.findViewById(R.id.lblNombreUsuario);
            lblUsername = (TextView) vista.findViewById(R.id.lblUserName);
            lblMessage = (TextView) vista.findViewById(R.id.lblZimess);
            lblDistance = (TextView) vista.findViewById(R.id.lblDistance);
            imgAvatar = (RoundedImageView) vista.findViewById(R.id.imgAvatarItem);
            imgAvatar.setOnClickListener(this);
            lblTimePass = (TextView) vista.findViewById(R.id.txtTiempo);
            lblCantComments = (TextView) vista.findViewById(R.id.lblCantComments);
            imgComment = (ImageView) vista.findViewById(R.id.imgComment);
        }

        public void setZimess(Zimess zimess) {
            this.zimess = zimess;
        }

        public void setContext(Context context) {
            this.context = context;
        }

        /**
         * Vamos al detalle del Zimess
         */
        private void gotoDetail() {
            if (context != null) {
                GlobalApplication globalApplication = (GlobalApplication) context.getApplicationContext();
                globalApplication.setTempZimess(zimess);
                Intent intent = new Intent(context, DetailZimessActivity.class);
                Activity activity = (Activity) context;
                activity.startActivityForResult(intent, 105);//requestCodeUpdateZimess
            }
        }

        @Override
        public void onClick(View v) {
            if (v instanceof ImageView) {
                System.out.println("IMG CLIIIIICK");
            } else {
                gotoDetail();
            }

        }
    }

}
