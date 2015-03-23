package com.ecp.gsy.dcs.zirkapp.app.util.parse;

import android.util.Log;

import com.ecp.gsy.dcs.zirkapp.app.util.locations.Location;
import com.parse.DeleteCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Elder on 11/03/2015.
 */
public class DataParseHelper {

    private static boolean deleteOk;

    /**
     * Busca el perfil del usuario
     *
     * @param parseUser
     * @return
     */
    public static ParseObject findProfile(ParseUser parseUser) {
        //Buscar perfil del comentario
        ParseQuery<ParseObject> query = ParseQuery.getQuery("ParseZProfile");
        query.whereEqualTo("user", parseUser);
        List<ParseObject> listProfile = new ArrayList<ParseObject>();
        try {
            listProfile = query.find();
        } catch (ParseException e) {
            Log.e("Parse.Profile", e.getMessage());
        }

        return listProfile.size() > 0 ? listProfile.get(0) : null;
    }

    /**
     * Busca Zimess de acuerdo a la posicion
     *
     * @param currentLocation
     * @param cantKmAround
     * @return
     */
    public static List<ParseUser> findUsersLocation(ParseUser currentUser, Location currentLocation, int cantKmAround) {
        List<ParseUser> listUsers = new ArrayList<ParseUser>();
        //Buscar Zimess
        ParseGeoPoint parseGeoPoint = new ParseGeoPoint(currentLocation.getLatitud(), currentLocation.getLongitud());
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereNotEqualTo("objectId", currentUser.getObjectId());
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
     * Busca los comentarios de un Zimess
     *
     * @param zimessId
     * @return
     */
    public static List<ParseObject> findComments(String zimessId) {
        List<ParseObject> listParseComments = new ArrayList<ParseObject>();
        //Buscar por Zimess
        ParseQuery<ParseObject> query = ParseQuery.getQuery("ParseZComment");
        query.whereEqualTo("zimessId", ParseObject.createWithoutData("ParseZimess", zimessId));
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
     * Busca Zimess de acuerdo a la posicion
     *
     * @param currentLocation
     * @param cantKmAround
     * @return
     */
    public static List<ParseObject> findZimessLocation(Location currentLocation, int cantKmAround) {
        List<ParseObject> listZimess = new ArrayList<ParseObject>();
        //Buscar Zimess
        ParseGeoPoint parseGeoPoint = new ParseGeoPoint(currentLocation.getLatitud(), currentLocation.getLongitud());
        ParseQuery<ParseObject> query = ParseQuery.getQuery("ParseZimess");
        query.whereWithinKilometers("location", parseGeoPoint, cantKmAround);
        query.include("user");
        query.orderByDescending("createdAt");
        try {
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
