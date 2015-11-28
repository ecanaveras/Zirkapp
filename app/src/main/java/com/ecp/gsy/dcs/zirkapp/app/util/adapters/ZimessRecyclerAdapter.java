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

import com.ecp.gsy.dcs.zirkapp.app.GlobalApplication;
import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.activities.DetailZimessActivity;
import com.ecp.gsy.dcs.zirkapp.app.activities.MyZimessActivity;
import com.ecp.gsy.dcs.zirkapp.app.activities.UserProfileActivity;
import com.ecp.gsy.dcs.zirkapp.app.util.listener.ItemClickListener;
import com.ecp.gsy.dcs.zirkapp.app.util.locations.Location;
import com.ecp.gsy.dcs.zirkapp.app.util.locations.ManagerDistance;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.models.ParseZimess;
import com.ecp.gsy.dcs.zirkapp.app.util.task.SendPushTask;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Elder on 20/03/2015.
 */
public class ZimessRecyclerAdapter extends RecyclerView.Adapter<ZimessRecyclerAdapter.ZimessViewHolder> implements ItemClickListener {

    private List<ParseZimess> zimessList;
    private Location currentLocation;
    private Context context;
    private GlobalApplication globalApplication;
    private Double rango;
    private ParseUser currentUser;

    public ZimessRecyclerAdapter(Context context, List<ParseZimess> zimessList, Location currentLocation) {
        this.context = context;
        this.zimessList = zimessList;
        this.currentLocation = currentLocation;
        globalApplication = (GlobalApplication) context.getApplicationContext();
        this.currentUser = ParseUser.getCurrentUser();
        rango = getRango();
    }

    @Override
    public ZimessViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View vista = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cardview_item_zimess, viewGroup, false);
        return new ZimessViewHolder(vista, this);
    }

    @Override
    public void onBindViewHolder(ZimessViewHolder zimessViewHolder, int i) {
        ParseZimess zimess = zimessList.get(i);

        String name = zimess.getUser().getString("name");
        zimessViewHolder.lblAliasUsuario.setText(name != null ? name : zimess.getUser().getUsername());

        //Estableciendo Imagen;
        globalApplication.setAvatarRoundedResize(zimess.getUser().getParseFile("avatar"), zimessViewHolder.imgAvatar, 100, 100);

        zimessViewHolder.lblUsername.setText(null);// zimess.getUser().getParseUser();
        zimessViewHolder.lblCantComments.setText(zimess.getCantComment() > 0 ? Integer.toString(zimess.getCantComment()) : "");
        zimessViewHolder.lblCantFavs.setText(zimess.getCantFavorite() > 0 ? Integer.toString(zimess.getCantFavorite()) : "");


        //cambiar icono cuando es favorito
        if (zimess.isMyFavorite(currentUser.getObjectId())) {
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
        zimess.setValueDistancia(mDistance.getDistancia());
        zimessViewHolder.lblDistance.setText(zimess.getDescDistancia());
        zimessViewHolder.lblDistance.setBackgroundResource(getResourceRibbon(mDistance.getDistancia()));
    }

    @Override
    public int getItemCount() {
        return zimessList != null ? zimessList.size() : 0;
    }


    @Override
    public void onItemClick(View view, int position) {
        ParseZimess zimess = zimessList.get(position);
//        View sharedImage = view.findViewById(R.id.imgAvatarItem);
//        globalApplication.setCustomParseUser(zimess.getUser());
//        UserProfileActivity.launch(
//                (Activity) context, position, sharedImage);
        ImageView imgFav = (ImageView) view.findViewById(R.id.imgFav);
        TextView lblCantFavs = (TextView) view.findViewById(R.id.lblCantFavs);
        switch (view.getId()) {
            case R.id.imgAvatarItem:
                //Ir la perfil del usuario
                globalApplication.setCustomParseUser(zimess.getUser());
                Intent intent = new Intent(context, UserProfileActivity.class);
                context.startActivity(intent);
                break;

            case R.id.lyFavorito:
                HashMap params = new HashMap<String, Object>();
                params.put("zimessId", zimess.getObjectId());
                //Marcar/desmarcar como favorito
                if (zimess != null && zimess.isMyFavorite(currentUser.getObjectId())) {
                    zimess.removeFavorites(Arrays.asList(currentUser.getObjectId()));
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
                    zimess.addFavorites(currentUser.getObjectId());
                    zimess.saveInBackground();

                    callParseFunction("AddZimessFavorite", params);

                    lblCantFavs.setText(Integer.toString(zimess.getCantFavorite() + 1));
                    imgFav.setImageResource(R.drawable.ic_icon_fav_color);

                    if (!zimess.getUser().equals(currentUser)) {
                        String nameCurrentUser = currentUser.getString("name") != null ? currentUser.getString("name") : currentUser.getUsername();
                        new SendPushTask(zimess.getObjectId(), zimess.getUser(), currentUser.getObjectId(), String.format("%s le gusta tu Zimes", nameCurrentUser), String.format("%s...", zimess.getZimessText().length() > 60 ? zimess.getZimessText().substring(0, 60) : zimess.getZimessText()), SendPushTask.PUSH_FAVORITE).execute();
                    }
                }
                try {
                    //Actualizar el Zimess
                    zimessList.set(position, (ParseZimess) zimessList.get(position).fetch());
                    //Toast.makeText(context, "Add favorite", Toast.LENGTH_SHORT).show();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                view.playSoundEffect(SoundEffectConstants.CLICK);
                break;

            default:
                //Ir al Zimess
                globalApplication.setTempZimess(zimess);
                Intent intent2 = new Intent((Activity) context, DetailZimessActivity.class);
                if (context instanceof MyZimessActivity)
                    intent2.putExtra("contextClass", MyZimessActivity.class.getSimpleName());
                context.startActivity(intent2);
                break;
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

        public ItemClickListener listener;

        public TextView lblAliasUsuario,
                lblUsername,
                lblMessage,
                lblDistance,
                lblTimePass,
                lblCantComments, lblCantFavs;
        public ImageView imgComment, imgFav, imgAvatar;
        public LinearLayout lyFavorito;

        public ZimessViewHolder(View vista, ItemClickListener listener) {
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

            this.listener = listener;
        }

        @Override
        public void onClick(View view) {
            listener.onItemClick(view, getAdapterPosition());
        }
    }


}


