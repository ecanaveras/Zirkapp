package com.ecp.gsy.dcs.zirkapp.app.util.parse;

import android.util.Log;

import com.ecp.gsy.dcs.zirkapp.app.util.locations.Location;
import com.ecp.gsy.dcs.zirkapp.app.util.task.SendPushTask;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

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
        List<ParseUser> listUsers = new ArrayList<ParseUser>();
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
        List<ParseUser> listUsers = new ArrayList<ParseUser>();
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
     * @param zimessId
     * @return
     */
    public static List<ParseObject> findComments(String zimessId) {
        List<ParseObject> listParseComments = new ArrayList<ParseObject>();
        //Buscar por Zimess
        ParseQuery<ParseObject> query = ParseQuery.getQuery("ParseZComment");
        //Buscar Zimess a filtrar
        ParseQuery<ParseObject> innerquery = ParseQuery.getQuery("ParseZimess");
        innerquery.whereEqualTo("objectId", zimessId);
        query.whereMatchesKeyInQuery("zimessId", "objectId", innerquery);//ParseObject.createWithoutData("ParseZimess", zimessId));
        query.include("user");
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
    public static List<ParseObject> findNotifications(ParseUser receptorUser) {
        List<ParseObject> parseObjects = new ArrayList<>();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("ParseZNotifi");
        query.whereEqualTo("receptorUser", receptorUser);
        query.include("senderUser");
        query.include("userTarget");
        query.include("zimessTarget");
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
    public static List<ParseObject> findZimessLocation(Location currentLocation, int cantMinKmAround, int cantMaxKmAround, int sortZimess) {
        List<ParseObject> listZimess = new ArrayList();
        //Buscar Zimess
        ParseGeoPoint parseGeoPoint = new ParseGeoPoint(currentLocation.getLatitud(), currentLocation.getLongitud());
        //Distancia Maxima
        ParseQuery<ParseObject> query = ParseQuery.getQuery("ParseZimess");
        query.whereWithinKilometers("location", parseGeoPoint, cantMaxKmAround);
        //Distancia minima
        if (cantMinKmAround != -1) {
            //Buscar y excluir el rango minimo
            ParseQuery<ParseObject> innerQuery = ParseQuery.getQuery("ParseZimess");
            innerQuery.whereWithinKilometers("location", parseGeoPoint, cantMinKmAround);
            query.whereDoesNotMatchKeyInQuery("objectId", "objectId", innerQuery);
        }

        //Orden
        switch (sortZimess) {
            case 1:
                query.whereNear("location", parseGeoPoint);
                //query.orderByDescending("location");
                break;
            case 2: //No se usa
                query.whereNear("location", parseGeoPoint);
                query.orderByAscending("location");
                break;
            default:
                query.orderByDescending("createdAt");
                break;
        }

        try {
            query.include("user");
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
    public static ParseObject findZimess(String zimessId) {
        List<ParseObject> listZimess = new ArrayList<ParseObject>();
        //Buscar Zimess
        ParseQuery<ParseObject> query = ParseQuery.getQuery("ParseZimess");
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
    public static List<ParseObject> findZimess(ParseUser parseUser) {
        List<ParseObject> listZimess = new ArrayList<ParseObject>();
        //Buscar Zimess
        ParseQuery<ParseObject> query = ParseQuery.getQuery("ParseZimess");
        query.whereEqualTo("user", parseUser);
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
     * @return
     */
    public static Integer findCountZimess(ParseUser parseUser) {
        Integer cantZimess = 0;
        //Buscar Zimess
        ParseQuery<ParseObject> query = ParseQuery.getQuery("ParseZimess");
        query.whereEqualTo("user", parseUser);
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
     * @return
     */
    public static ParseObject findDataVisit(ParseUser parseUser) {
        List<ParseObject> listVisita = new ArrayList<ParseObject>();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("ParseZVisit");
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

    //##############################################################
    //                          DELETE
    //##############################################################

    /**
     * Elimina el zimess con todos sus comentarios y demas.
     *
     * @return
     */
    public static boolean deleteDataZimess(final String zimessId) {
        deleteOk = false;
        if (zimessId != null) {
            //Buscar los comentarios.
            List<ParseObject> listParseComments = new ArrayList<ParseObject>();
            listParseComments = findComments(zimessId);
            //Eliminar los comentarios
            ParseObject.deleteAllInBackground(listParseComments);
            //Eliminar Zimess
            ParseObject zimess = findZimess(zimessId);
            try {
                zimess.delete();
                deleteOk = true;
            } catch (ParseException e1) {
                Log.e("Parse.delete.Zimess", e1.getMessage());
            }
        }
        return deleteOk;
    }
}
