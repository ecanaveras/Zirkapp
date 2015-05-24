package com.ecp.gsy.dcs.zirkapp.app.util.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.ecp.gsy.dcs.zirkapp.app.activities.DetailZimessActivity;
import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.DataParseHelper;

/**
 * Created by Elder on 22/03/2015.
 */
public class DeleteDataZimessTask extends AsyncTask<String, Void, Boolean> {

    private ProgressDialog progressDialog;
    private Context context;

    public DeleteDataZimessTask(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(context.getString(R.string.msgDeleting));
        progressDialog.show();
    }

    @Override
    protected Boolean doInBackground(String... params) {
        Boolean deleteOk = false;
        if (params.length > 0 && params[0] != null) {
            deleteOk = DataParseHelper.deleteDataZimess(params[0]);
        }
        return deleteOk;
    }

    @Override
    protected void onPostExecute(Boolean deleteOk) {
        if (deleteOk) {
            Toast.makeText(context, context.getString(R.string.msgZimessDeleteOk), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, context.getString(R.string.msgZimessDeleteFailed), Toast.LENGTH_LONG).show();
        }

        if (context instanceof DetailZimessActivity) {
            DetailZimessActivity activity = (DetailZimessActivity) context;
            activity.isZimessUpdated = deleteOk;
            activity.onBackPressed();
        }

        progressDialog.dismiss();
    }


}
