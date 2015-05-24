package com.ecp.gsy.dcs.zirkapp.app.util.beans;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by Elder on 19/05/2015.
 */
@DatabaseTable(tableName = "handlerlogin")
public class HandlerLogindb {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(index = true, canBeNull = false)
    public boolean sessionActive;

    public HandlerLogindb() {
    }

    public HandlerLogindb(boolean sessionActive) {
        this.sessionActive = sessionActive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HandlerLogindb that = (HandlerLogindb) o;

        if (id != that.id) return false;
        return sessionActive == that.sessionActive;

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (sessionActive ? 1 : 0);
        return result;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isSessionActive() {
        return sessionActive;
    }

    public void setSessionActive(boolean sessionActive) {
        this.sessionActive = sessionActive;
    }
}
