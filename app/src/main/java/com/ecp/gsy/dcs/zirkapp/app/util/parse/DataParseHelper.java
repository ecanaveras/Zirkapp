package com.ecp.gsy.dcs.zirkapp.app.util.parse;

import android.util.Log;

import com.ecp.gsy.dcs.zirkapp.app.util.locations.Location;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.models.ParseZComment;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.models.ParseZFavorite;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.models.ParseZLastMessage;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.models.ParseZNotifi;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.models.ParseZimess;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Elder on 11/03/2015.
 */
public class DataParseHelper {

    /**
     * Busca Usuarios de acuerdo a la posicion
     *
     * @param currentLocation
     * @param cantKmAround
     * @return
     */
    public static List<ParseUser> findUsersLocation(ParseUser currentUser, Location currentLocation, int cantKmAround, String gender) {
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
        query.whereNear("location", parseGeoPoint);
        if (gender != null) {
            query.whereEqualTo("gender", gender);
        }
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
     * Busca los favoritos de un Usuario
     *
     * @param parseUser
     * @return
     */
    public static List<ParseZFavorite> findFavorites(ParseUser parseUser) {
        List<ParseZFavorite> listParseFavorites = new ArrayList<>();
        //Buscar por Zimess
        ParseQuery<ParseZFavorite> query = ParseQuery.getQuery(ParseZFavorite.class);
        query.whereEqualTo(ParseZFavorite.USER, parseUser);
        query.orderByAscending("createdAt");
        try {
            listParseFavorites = query.find();
        } catch (ParseException e) {
            Log.e("Parse.Favorites", e.getMessage());
        }

        return listParseFavorites;
    }


    /**
     * Retorna el favorito de un zimess y usuario
     *
     * @param zimess
     * @param currentUser
     * @return
     */
    public static ParseZFavorite findFavorite(ParseZimess zimess, ParseUser currentUser) {
        //Buscar por Zimess
        ParseQuery<ParseZFavorite> query = ParseQuery.getQuery(ParseZFavorite.class);
        query.whereEqualTo(ParseZFavorite.ZIMESS_ID, zimess);
        query.whereEqualTo(ParseZFavorite.USER, currentUser);
        query.setLimit(1);
        try {
            return query.getFirst();
        } catch (ParseException e) {
            Log.e("Parse.Favorites", e.getMessage());
        }

        return null;
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
        query.include(ParseZNotifi.ZIMESS_TARGET + ".user");
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

        //Limite de Zimess
        query.setLimit(300);

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

    public static List<ParseZLastMessage> findLastMessage(ParseUser currentUser) {
        List<ParseZLastMessage> parseZLastMessages = new ArrayList<>();
        ParseQuery<ParseZLastMessage> querySender = ParseQuery.getQuery(ParseZLastMessage.class);
        querySender.whereEqualTo(ParseZLastMessage.SENDER_ID, currentUser);

        ParseQuery<ParseZLastMessage> queryRecipient = ParseQuery.getQuery(ParseZLastMessage.class);
        queryRecipient.whereEqualTo(ParseZLastMessage.RECIPIENT_ID, currentUser);

        String[] userId = {currentUser.getObjectId()};
        ParseQuery<ParseZLastMessage> query = ParseQuery.or(Arrays.asList(querySender, queryRecipient));
        query.whereNotContainedIn(ParseZLastMessage.DELETE_FOR, Arrays.asList(userId));
        query.include(ParseZLastMessage.SENDER_ID);
        query.include(ParseZLastMessage.RECIPIENT_ID);
        query.include(ParseZLastMessage.ZMESSAGE_ID);
        query.orderByDescending("updatedAt");
        try {
            parseZLastMessages = query.find();
        } catch (ParseException e) {
            Log.e("Parse.LastMessage", e.getMessage());
        }
        return parseZLastMessages;
    }

    //##############################################################
    //                          DELETE
    //##############################################################

    /**
     * Elimina el zimess con todos sus comentarios y demas.
     *
     * @return
     */
    public static boolean deleteDataZimess(ParseZimess zimess) {
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
                return true;
            } catch (ParseException e1) {
                Log.e("Parse.delete.Zimess", e1.getMessage());
            }
        }
        return false;
    }


    /**
     * Elimina el favorito de un Zimess
     *
     * @return
     */
    public static boolean deleteZFavorite(ParseZFavorite zFavorite) {
        if (zFavorite != null) {
            try {
                zFavorite.delete();
                //Usando ParseCloud
                ParseCloud.callFunctionInBackground("ParseZFavorite", new HashMap<String, Object>(), new FunctionCallback<String>() {
                    public void done(String result, ParseException e) {
                        if (e != null) {
                            Log.e("Parse.Cloud.Favorite", e.getMessage());
                        }
                    }
                });
                return true;
            } catch (ParseException e1) {
                Log.e("Parse.delete.Favorite", e1.getMessage());
            }
        }
        return false;
    }
}
