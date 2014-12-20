package com.ecp.gsy.dcs.zirkapp.app.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.ecp.gsy.dcs.zirkapp.app.R;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

/**
 * Created by Elder on 20/12/2014.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    public static final String DATABASE_NAME = "zirkapp.db";
    public static final int DATABASE_VERSION = 1;

    //Objetos DAO para acceder a las tablas db
    private Dao<Welcomedb, Integer> welcomedbDao = null;
    private RuntimeExceptionDao<Welcomedb, Integer> welcomedbRuntimeDao = null;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config);
    }

    /**
     * Este metodo es invocado al crear la base de datos, usualmente se hace llamado a los metodos createTable
     * para crear las tablas.
     *
     * @param db
     * @param dbSource
     */
    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource dbSource) {
        try {
            Log.i(DatabaseHelper.class.getSimpleName(), "onCreate()");
            TableUtils.createTable(dbSource, Welcomedb.class);
        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getSimpleName(), "Imposible crear la base de datos", e);
            throw new RuntimeException(e);
        }
    }


    /**
     * Este metodo es utilizado cuando se actualiza la aplicacion y la base de datos tiene una version superior
     * permite el ajuste de los datos para aplicarlos a la nueva version.
     *
     * @param db
     * @param dbSource
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource dbSource, int oldVersion, int newVersion) {
        try {
            Log.i(DatabaseHelper.class.getSimpleName(), "onUpgrade()");
            TableUtils.dropTable(dbSource, Welcomedb.class, true);
            //Creamos nuevamente las tablas
            onCreate(db, dbSource);
        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getSimpleName(), "Imposible eliminar la base de datos", e);
            throw new RuntimeException(e);
        }
    }

    public Dao<Welcomedb, Integer> getWelcomedbDao() throws SQLException {
        if(welcomedbDao == null) welcomedbDao = getDao(Welcomedb.class);
        return welcomedbDao;
    }

    public RuntimeExceptionDao<Welcomedb, Integer> getWelcomedbRuntimeDao() {
        if(welcomedbRuntimeDao == null) welcomedbRuntimeDao = getRuntimeExceptionDao(Welcomedb.class);
        return welcomedbRuntimeDao;
    }

    @Override
    public void close() {
        super.close();
        welcomedbDao = null;
        welcomedbRuntimeDao = null;
    }
}
