package com.ecp.gsy.dcs.zirkapp.app.util.beans;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.parse.ParseUser;

/**
 * Created by Elder on 05/08/2015.
 */
@DatabaseTable(tableName = "users_around")
public class UsersArounddb {

    @DatabaseField(id = true)
    public String userId;

    public UsersArounddb() {
    }

    public UsersArounddb(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
