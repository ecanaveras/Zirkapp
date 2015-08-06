package com.ecp.gsy.dcs.zirkapp.app.util.parse;

import android.util.Log;

import com.ecp.gsy.dcs.zirkapp.app.util.locations.Location;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.models.ParseZComment;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.models.ParseZNotifi;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.models.ParseZVisit;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.models.ParseZimess;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Elder on 11/03/2015.
 */
public class DataParseHelper {

    private static boolean deleteOk;

    /**
     * Busca Usuarios de acuerdo a la posicion
     *
     * @param currentLocation
     * @param cantKmAround
     * @return
     */
    public static List<ParseUser> findUsersLocation(ParseUser currentUser, Location currentLocation, int cantKmAround) {
        List<ParseUser> listUsers = new ArrayList<>();
        //Buscar Usuarios
        ParseGeoPoint parseGeoPoint = new ParseGeoPoint(currentLocation.getLatitud(), currentLocation.getLongitud());
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        //Buscar y excluir usuario actual
        ParseQuery<ParseUser> innerQuery = ParseUser.getQuery();
        innerQuery.whereEqualTo("objectId", currentUser.getObjectId());
        query.whereDoesNotMatchKeyInQuery("objectId", "objectId", innerQuery);
        //Buscar usuarios en el rango de Km
        query.whereWithinKilometers("location", parseGeoPoint, cantKmAround);
        query.whereEqualTo("online", true);
        query.orderByAscending("name");
        try {
            listUsers = query.find();
        } catch (ParseException e) {
            Log.e("Parse.Users", e.getMessage());
        }

        return listUsers;
    }


    /**
     * Busca Usuarios de acuerdo a un lista de Ids
     *
     * @param usersSearch
     * @return
     */
    public static List<ParseUser> findUsersList(ArrayList<String> usersSearch) {
        List<ParseUser> listUsers = new ArrayList<>();
        //Buscar Usuarios
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereContainedIn("objectId", usersSearch);
        try {
            listUsers = query.find();
        } catch (ParseException e) {
            Log.e("Parse.Users", e.getMessage());
        }

        return listUsers;
    }


    /**
     * Busca los comentarios de un Zimess
     *
     * @param zimess
     * @return
     */
    public static List<ParseZComment> findComments(ParseZimess zimess) {
        List<ParseZComment> listParseComments = new ArrayList<>();
        //Buscar por Zimess
        ParseQuery<ParseZComment> query = ParseQuery.getQuery(ParseZComment.class);
        query.whereEqualTo(ParseZComment.ZIMESS_ID, zimess);
        query.include(ParseZComment.USER);
        query.orderByAscending("createdAt");
        try {
            listParseComments = query.find();
        } catch (ParseException e) {
            Log.e("Parse.Comments", e.getMessage());
        }

        return listParseComments;
    }

    /**
     * Busca notificaciones
     *
     * @param receptorUser
     * @return
     */
    public static List<ParseZNotifi> findNotifications(ParseUser receptorUser) {
        List<ParseZNotifi> parseObjects = new ArrayList<>();
        ParseQuery<ParseZNotifi> query = ParseQuery.getQuery(ParseZNotifi.class);
        query.whereEqualTo(ParseZNotifi.RECEPTOR_USER, receptorUser);
        query.include(ParseZNotifi.SENDER_USER);
        query.include(ParseZNotifi.ZIMESS_TARGET);
        query.orderByDescending("createdAt");
        query.setLimit(30);
        try {
            parseObjects = query.find();
        } catch (ParseException e) {
            Log.e("Parse.Notifi", e.getMessage());
        }

        return parseObjects;
    }

    /**
     * Busca Zimess de acuerdo a la posicion
     *
     * @param currentLocation
     * @param cantMaxKmAround
     * @return
     */
    public static List<ParseZimess> findZimessLocation(Location currentLocation, int cantMinKmAround, int cantMaxKmAround, int sortZimess) {
        List<ParseZimess> listZimess = new ArrayList<>();
        //Buscar Zimess
        ParseGeoPoint parseGeoPoint = new ParseGeoPoint(currentLocation.getLatitud(), currentLocation.getLongitud());
        //Distancia Maxima
        ParseQuery<ParseZimess> query = ParseQuery.getQuery(ParseZimess.class);
        query.whereWithinKilometers(ParseZimess.LOCATION, parseGeoPoint, cantMaxKmAround);
        //Distancia minima
        if (cantMinKmAround != -1) {
            //Buscar y excluir el rango minimo
            ParseQuery<ParseZimess> innerQuery = ParseQuery.getQuery(ParseZimess.class);
            innerQuery.whereWithinKilometers(ParseZimess.LOCATION, parseGeoPoint, cantMinKmAround);
            query.whereDoesNotMatchKeyInQuery("objectId", "objectId", innerQuery);
        }

        //Orden
        switch (sortZimess) {
            case 1:
                query.whereNear(ParseZimess.LOCATION, parseGeoPoint);
                //query.orderByDescending("location");
                break;
            case 2: //No se usa
                query.whereNear(ParseZimess.LOCATION, parseGeoPoint);
                query.orderByAscending(ParseZimess.LOCATION);
                break;
            default:
                query.orderByDescending("createdAt");
                break;
        }

        try {
            query.include(ParseZimess.USER);
            listZimess = query.find();
        } catch (ParseException e) {
            Log.e("Parse.Zimess", e.getMessage());
        }

        return listZimess;
    }

    /**
     * Busca Zimess de acuerdo al zimessId
     *
     * @param zimessId
     * @return
     */
    public static ParseZimess findZimess(String zimessId) {
        List<ParseZimess> listZimess = new ArrayList<>();
        //Buscar Zimess
        ParseQuery<ParseZimess> query = ParseQuery.getQuery(ParseZimess.class);
        query.whereEqualTo("objectId", zimessId);
        try {
            listZimess = query.find();
        } catch (ParseException e) {
            Log.e("Parse.Zimess", e.getMessage());
        }

        return listZimess.size() > 0 ? listZimess.get(0) : null;
    }

    /**
     * Busca Zimess de acuerdo al usuario
     *
     * @param parseUser
     * @return
     */
    public static List<ParseZimess> findZimess(ParseUser parseUser) {
        List<ParseZimess> listZimess = new ArrayList<>();
        //Buscar Zimess
        ParseQuery<ParseZimess> query = ParseQuery.getQuery(ParseZimess.class);
        query.whereEqualTo(ParseZimess.USER, parseUser);
        query.orderByDescending("createdAt");
        try {
            listZimess = query.find();
        } catch (ParseException e) {
            Log.e("Parse.Zimess", e.getMessage());
        }

        return listZimess;
    }

    /**
     * Devuelve la cantidad Zimess de acuerdo al usuario
     *
     * @param parseUser
     * * @deprecated No se usa, estos datos ahora se almacenan en ParseUser
     * @return
     */
    public static Integer findCountZimess(ParseUser parseUser) {
        Integer cantZimess = 0;
        //Buscar Zimess
        ParseQuery<ParseZimess> query = ParseQuery.getQuery(ParseZimess.class);
        query.whereEqualTo(ParseZimess.USER, parseUser);
        try {
            cantZimess = query.count();
        } catch (ParseException e) {
            Log.e("Parse.Count.Zimess", e.getMessage());
        }

        return cantZimess;
    }

    /**
     * Busca los datos de la visita del perfil
     *
     * @param parseUser
     * @deprecated No se usa, estos datos ahora se almacenan en ParseUser
     * @return
     */
    public static ParseZVisit findDataVisit(ParseUser parseUser) {
        List<ParseZVisit> listVisita = new ArrayList<>();
        ParseQuery<ParseZVisit> query = ParseQuery.getQuery(ParseZVisit.class);
        query.whereEqualTo("user", parseUser);
        try {
            listVisita = query.find();
        } catch (ParseException e) {
            Log.e("Parse.Visita", e.getMessage());
        }

        return listVisita.size() > 0 ? listVisita.get(0) : null;
    }


    /**
     * Busca un usuarios de acuerdo a su objectId
     *
     * @param userId
     * @return
     */
    public static ParseUser findUser(String userId) {
        List<ParseUser> parseUsers = new ArrayList<>();
        //Buscar Usuario
        ParseQuery query = ParseUser.getQuery();
        query.whereEqualTo("objectId", userId);
        try {
            parseUsers = query.find();
        } catch (ParseException e) {
            Log.e("Parse.Users", e.getMessage());
        }

        return parseUsers.size() > 0 ? parseUsers.get(0) : null;
    }

    /**
     * Busca un usuarios de acuerdo a su parseUser
     *
     * @param username
     * @return
     */
    public static ParseUser findUserName(String username) {
        List<ParseUser> parseUsers = new ArrayList<>();
        //Buscar Usuario
        ParseQuery query = ParseUser.getQuery();
        query.whereEqualTo("username", username);
        try {
            parseUsers = query.find();
        } catch (ParseException e) {
            Log.e("Parse.Users", e.getMessage());
        }

        return parseUsers.size() > 0 ? parseUsers.get(0) : null;
    }

    //##############################################################
    //                          DELETE
    //##############################################################

    /**
     * Elimina el zimess con todos sus comentarios y demas.
     *
     * @return
     */
    public static boolean deleteDataZimess(final ParseZimess zimess) {
        deleteOk = false;
        if (zimess != null) {
            //Buscar los comentarios. No necesario porque se usa ParseCloud
            /*List<ParseZComment> listParseComments = findComments(zimess);
            if (listParseComments.size() > 0) {
                //Eliminar los comentarios
                ParseObject.deleteAllInBackground(listParseComments);
            }*/
            //Eliminar Zimess
            //ParseObject zimessDelete = findZimess(zimess.getObjectId());
            try {
                zimess.delete();
                //Usando ParseCloud
                ParseCloud.callFunctionInBackground("ParseZimess", new HashMap<String, Object>(), new FunctionCallback<String>() {
                    public void done(String result, ParseException e) {
                        if (e != null) {
                            Log.e("Parse.Cloud.Zimess", e.getMessage());
                        }
                    }
                });
                deleteOk = true;
            } catch (ParseException e1) {
                Log.e("Parse.delete.Zimess", e1.getMessage());
            }
        }
        return deleteOk;
    }
}
