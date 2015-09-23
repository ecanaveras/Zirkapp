package com.ecp.gsy.dcs.zirkapp.app.util.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ecp.gsy.dcs.zirkapp.app.GlobalApplication;
import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.activities.DetailZimessActivity;
import com.ecp.gsy.dcs.zirkapp.app.activities.MyZimessActivity;
import com.ecp.gsy.dcs.zirkapp.app.activities.UserProfileActivity;
import com.ecp.gsy.dcs.zirkapp.app.util.locations.Location;
import com.ecp.gsy.dcs.zirkapp.app.util.locations.ManagerDistance;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.DataParseHelper;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.models.ParseZFavorite;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.models.ParseZimess;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Elder on 20/03/2015.
 */
public class ZimessRecyclerAdapter extends RecyclerView.Adapter<ZimessRecyclerAdapter.ZimessViewHolder> {

    private List<ParseZimess> zimessList;
    private Location currentLocation;
    private Context context;
    private GlobalApplication globalApplication;
    private Double rango;
    private String currentUserId;

    public ZimessRecyclerAdapter(Context context, List<ParseZimess> zimessList, Location currentLocation) {
        this.context = context;
        this.zimessList = zimessList;
        this.currentLocation = currentLocation;
        globalApplication = (GlobalApplication) context.getApplicationContext();
        if (ParseUser.getCurrentUser() != null)
            currentUserId = ParseUser.getCurrentUser().getObjectId();
        rango = getRango();
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
        ParseZimess zimess = zimessList.get(i);

        zimessViewHolder.setZimess(zimess);
        zimessViewHolder.setIndexZimess(i);

        String name = zimess.getUser().getString("name");
        zimessViewHolder.lblAliasUsuario.setText(name != null ? name : zimess.getUser().getUsername());

        //Estableciendo Imagen;
        globalApplication.setAvatarRoundedResize(zimess.getUser().getParseFile("avatar"), zimessViewHolder.imgAvatar, 100, 100);

        zimessViewHolder.lblUsername.setText(null);// zimess.getUser().getParseUser();
        zimessViewHolder.lblCantComments.setText(zimess.getCantComment() > 0 ? Integer.toString(zimess.getCantComment()) : "");
        zimessViewHolder.lblCantFavs.setText(zimess.getCantFavorite() > 0 ? Integer.toString(zimess.getCantFavorite()) : "");


        //cambiar icono cuando es favorito
        if (zimess.isMyFavorite(currentUserId)) {
            zimessViewHolder.imgFav.setImageResource(R.drawable.ic_icon_fav_color);
        } else {
            zimessViewHolder.imgFav.setImageResource(R.drawable.ic_icon_fav);
        }

        //cambiar icono cuando hay comentarios
        if (zimess.getCantComment() > 0) {
            zimessViewHolder.imgComment.setImageResource(R.drawable.ic_icon_response_color);
        } else {
            zimessViewHolder.imgComment.setImageResource(R.drawable.ic_icon_response);
        }

        zimessViewHolder.lblMessage.setText(zimess.getZimessText());

        //Manejando tiempos transcurridos
        String tiempoTranscurrido = GlobalApplication.getTimepass(zimess.getCreatedAt());
        zimessViewHolder.lblTimePass.setText(tiempoTranscurrido);

        //Calcular distancia del Zimess remoto
        Location zimessLocation = new Location(zimess.getLocation().getLatitude(), zimess.getLocation().getLongitude());
        ManagerDistance mDistance = new ManagerDistance(currentLocation, zimessLocation);
        zimess.setDescDistancia(mDistance.getDistanciaToString());
        zimessViewHolder.lblDistance.setText(zimess.getDescDistancia());
        zimessViewHolder.lblDistance.setBackgroundResource(getResourceRibbon(mDistance.getDistancia()));
    }

    @Override
    public int getItemCount() {
        return zimessList != null ? zimessList.size() : 0;
    }

    private int getResourceRibbon(double distancia) {
        if (distancia <= rango) { //Verde
            return R.drawable.ic_ribbon_green;
        } else if (distancia > rango && distancia <= (rango * 2)) {
            return R.drawable.ic_ribbon_yellow;
        }
        return R.drawable.ic_ribbon_red;
    }

    private double getRango() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        int dist_max = Integer.parseInt(preferences.getString("max_dist_list", "10"));
        double rango = (dist_max * 1000) / 3;
        return rango;
    }

    public class ZimessViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ParseZimess zimess;
        private Context context;
        private int indexZimess;

        public TextView lblAliasUsuario,
                lblUsername,
                lblMessage,
                lblDistance,
                lblTimePass,
                lblCantComments, lblCantFavs;
        public ImageView imgComment, imgFav, imgAvatar;
        public LinearLayout lyFavorito;

        public ZimessViewHolder(View vista) {
            super(vista);
            lblAliasUsuario = (TextView) vista.findViewById(R.id.lblNombreUsuario);
            lblUsername = (TextView) vista.findViewById(R.id.lblUserName);
            lblMessage = (TextView) vista.findViewById(R.id.lblZimess);
            lblDistance = (TextView) vista.findViewById(R.id.lblDistance);
            imgAvatar = (ImageView) vista.findViewById(R.id.imgAvatarItem);
            lblTimePass = (TextView) vista.findViewById(R.id.txtTiempo);
            lblCantComments = (TextView) vista.findViewById(R.id.lblCantComments);
            lblCantFavs = (TextView) vista.findViewById(R.id.lblCantFavs);
            imgComment = (ImageView) vista.findViewById(R.id.imgComment);
            imgFav = (ImageView) vista.findViewById(R.id.imgFav);
            lyFavorito = (LinearLayout) vista.findViewById(R.id.lyFavorito);

            vista.setOnClickListener(this);
            imgAvatar.setOnClickListener(this);
            lyFavorito.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (view instanceof ImageView) { //Ir al perfil de usuario
                globalApplication.setCustomParseUser(zimess.getUser());
                Intent intent = new Intent(context, UserProfileActivity.class);
                context.startActivity(intent);
            } else if (view instanceof LinearLayout && view.getId() == lyFavorito.getId()) {
                HashMap params = new HashMap<String, Object>();
                params.put("zimessId", zimess.getObjectId());

                if (zimess != null && zimess.isMyFavorite(currentUserId)) {
                    zimess.removeFavorites(Arrays.asList(currentUserId));
                    zimess.saveInBackground();
                    callParseFunction("DelZimessFavorite", params);
                    imgFav.setImageResource(R.drawable.ic_icon_fav);
                    if (zimess.getCantFavorite() <= 1) {
                        lblCantFavs.setText("");
                    } else {
                        lblCantFavs.setText(Integer.toString(zimess.getCantFavorite() - 1));
                    }
                } else {
                    //Actualizar los datos del Zimess
                    zimess.addFavorites(currentUserId);
                    zimess.saveInBackground();
                    callParseFunction("AddZimessFavorite", params);

                    lblCantFavs.setText(Integer.toString(zimess.getCantFavorite() + 1));
                    imgFav.setImageResource(R.drawable.ic_icon_fav_color);
                }
                try {
                    //Actualizar el Zimess
                    zimessList.set(indexZimess, (ParseZimess) zimessList.get(indexZimess).fetch());
                    //Toast.makeText(context, "Add favorite", Toast.LENGTH_SHORT).show();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                view.playSoundEffect(SoundEffectConstants.CLICK);
            } else {
                //Ir al Zimess
                globalApplication.setTempZimess(zimess);
                Intent intent = new Intent((Activity) context, DetailZimessActivity.class);
                if (context instanceof MyZimessActivity)
                    intent.putExtra("contextClass", MyZimessActivity.class.getSimpleName());
                context.startActivity(intent);
            }
        }

        private void callParseFunction(final String nameFunction, HashMap<String, Object> params) {
            ParseCloud.callFunctionInBackground(nameFunction, params, new FunctionCallback<String>() {
                public void done(String result, ParseException e) {
                    if (e != null) {
                        Log.e("Parse.Cloud." + nameFunction, e.getMessage());
                    }
                }
            });
        }

        public void setZimess(ParseZimess zimess) {
            this.zimess = zimess;
        }


        public void setContext(Context context) {
            this.context = context;
        }

        public void setIndexZimess(int indexZimess) {
            this.indexZimess = indexZimess;
        }
    }

}
