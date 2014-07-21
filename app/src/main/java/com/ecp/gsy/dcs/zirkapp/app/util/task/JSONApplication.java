package com.ecp.gsy.dcs.zirkapp.app.util.task;

import android.app.Application;
import android.content.Context;
import android.widget.ArrayAdapter;

/**
 * Created by Elder on 15/07/2014.
 */
public class JSONApplication extends Application {
    //Url de la API
    private final static String URL = "http://zirkapp.uni.me/api/v1.1/zsms";

    public void getData(Context context, ArrayAdapter arrayAdapter){
        //Actualizamos los datos del adpater atravez de un Asynctask
        new LoadMessagesTask(context, arrayAdapter, URL).execute();
    }

}
