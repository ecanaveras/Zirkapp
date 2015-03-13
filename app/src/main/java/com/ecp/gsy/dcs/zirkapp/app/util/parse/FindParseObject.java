package com.ecp.gsy.dcs.zirkapp.app.util.parse;

import android.util.Log;

import com.ecp.gsy.dcs.zirkapp.app.util.locations.Location;
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
public class FindParseObject {

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
        try {
            listParseComments = query.find();
        } catch (ParseException e) {
            Log.e("Parse.Comments", e.getMessage());
        }

        return listParseComments;
    }

    /**
     * Devuelve la cantidad de comentarios de un Zimess
     *
     * @param zimessId
     * @return
     */
    public static Integer findCountComments(String zimessId) {
        List<ParseObject> cantComments = new ArrayList<ParseObject>();
        //Buscar por Zimess
        ParseQuery<ParseObject> query = ParseQuery.getQuery("ParseZCountComment");
        query.whereEqualTo("zimessId", ParseObject.createWithoutData("ParseZimess", zimessId));
        try {
            cantComments = query.find();
        } catch (ParseException e) {
            Log.e("Parse.Count.Comments", e.getMessage());
        }

        return cantComments.size() > 0 ? new Integer(cantComments.get(0).get("count_comment").toString()) : 0;
    }

    /**
     * Busca Zimess de acuerdo a la posicion
     *
     * @param currentLocation
     * @param cantKmAround
     * @return
     */
    public static List<ParseObject> findZimess(Location currentLocation, int cantKmAround) {
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
}
