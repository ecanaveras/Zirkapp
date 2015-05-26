package com.ecp.gsy.dcs.zirkapp.app.util.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ecp.gsy.dcs.zirkapp.app.GlobalApplication;
import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.beans.Zimess;
import com.ecp.gsy.dcs.zirkapp.app.util.locations.Location;
import com.ecp.gsy.dcs.zirkapp.app.util.locations.ManagerDistance;

import java.util.List;

/**
 * Created by Elder on 20/03/2015.
 */
public class ZimessReciclerAdapter extends RecyclerView.Adapter<ZimessReciclerAdapter.ZimessViewHolder> {

    private List<Zimess> zimessList;
    private Location currentLocation;

    public ZimessReciclerAdapter(List<Zimess> zimessList, Location currentLocation) {
        this.zimessList = zimessList;
        this.currentLocation = currentLocation;
    }

    @Override
    public ZimessViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View vista = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cardview_item_zimess, viewGroup, false);
        ZimessViewHolder holder = new ZimessViewHolder(vista);
        return holder;
    }

    @Override
    public void onBindViewHolder(ZimessViewHolder zimessViewHolder, int i) {
        Zimess zimess = zimessList.get(i);

        zimessViewHolder.lblAliasUsuario.setText(zimess.getUser().getString("name"));

        //Estableciendo Imagen;
        zimessViewHolder.imgAvatar.setImageDrawable(zimess.getAvatar());

        zimessViewHolder.lblUsername.setText(zimess.getUser().getUsername());
        zimessViewHolder.lblCantComments.setText(Integer.toString(zimess.getCantComment()));

        //cambiar icono cuando hay comentarios
        if (zimess.getCantComment() != 0)
            zimessViewHolder.imgComment.setImageResource(R.drawable.ic_icon_response_color);
        else
            zimessViewHolder.imgComment.setImageResource(R.drawable.ic_icon_response);

        zimessViewHolder.lblMessage.setText(zimess.getZimessText());

        //Manejando tiempos transcurridos
        String tiempoTranscurrido = GlobalApplication.getTimepass(zimess.getCreateAt());
        zimessViewHolder.lblTimePass.setText(tiempoTranscurrido);

        //Calcular distancia del Zimess remoto
        Location zimessLocation = new Location(zimess.getLocation().getLatitude(), zimess.getLocation().getLongitude());
        ManagerDistance mDistance = new ManagerDistance(currentLocation, zimessLocation);
        zimessViewHolder.lblDistance.setText(mDistance.getDistanciaToString());
    }

    @Override
    public int getItemCount() {
        return zimessList != null ? zimessList.size() : 0;
    }

    public class ZimessViewHolder extends RecyclerView.ViewHolder {

        public TextView lblAliasUsuario,
                lblUsername,
                lblMessage,
                lblDistance,
                lblTimePass,
                lblCantComments;
        public ImageView imgComment, imgAvatar;

        public ZimessViewHolder(View vista) {
            super(vista);
            lblAliasUsuario = (TextView) vista.findViewById(R.id.lblNombreUsuario);
            lblUsername = (TextView) vista.findViewById(R.id.lblUserName);
            lblMessage = (TextView) vista.findViewById(R.id.lblZimess);
            lblDistance = (TextView) vista.findViewById(R.id.lblDistance);
            imgAvatar = (ImageView) vista.findViewById(R.id.imgAvatarItem);
            lblTimePass = (TextView) vista.findViewById(R.id.txtTiempo);
            lblCantComments = (TextView) vista.findViewById(R.id.lblCantComments);
            imgComment = (ImageView) vista.findViewById(R.id.imgComment);
        }
    }

}
