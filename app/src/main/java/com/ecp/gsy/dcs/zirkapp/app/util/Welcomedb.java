package com.ecp.gsy.dcs.zirkapp.app.util;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by Elder on 20/12/2014.
 */

@DatabaseTable(tableName = "welcome")
public class Welcomedb {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(index = true, canBeNull = false)
    public String runWelcome;

    public Welcomedb() {
    }

    public Welcomedb(String runWelcome) {
        this.runWelcome = runWelcome;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Welcomedb welcomedb = (Welcomedb) o;

        if (runWelcome != null ? !runWelcome.equals(welcomedb.runWelcome) : welcomedb.runWelcome != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return runWelcome != null ? runWelcome.hashCode() : 0;
    }

    public int getId() {
        return id;
    }

    public String getRunWelcome() {
        return runWelcome;
    }

    public void setRunWelcome(String runWelcome) {
        this.runWelcome = runWelcome;
    }
}
