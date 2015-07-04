package com.ecp.gsy.dcs.zirkapp.app.util.task;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ecp.gsy.dcs.zirkapp.app.util.adapters.CommentsAdapter;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.DataParseHelper;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.models.ParseZComment;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.models.ParseZimess;

import java.util.List;

/**
 * Created by Elder on 08/03/2015.
 */
public class RefreshDataCommentsTask extends AsyncTask<ParseZimess, Void, List<ParseZComment>> {

    private ProgressBar progressBar;
    private ListView listViewComment;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Context context;
    private TextView lblCantComments;

    public RefreshDataCommentsTask(Context context, ProgressBar progressBar, ListView listComment, SwipeRefreshLayout swipeRefreshLayout) {
        this.progressBar = progressBar;
        this.listViewComment = listComment;
        this.swipeRefreshLayout = swipeRefreshLayout;
        this.context = context;
    }

    public RefreshDataCommentsTask(Context context, ProgressBar progressBar, ListView listComment, SwipeRefreshLayout swipeRefreshLayout, TextView lblCantComments) {
        this.progressBar = progressBar;
        this.listViewComment = listComment;
        this.swipeRefreshLayout = swipeRefreshLayout;
        this.context = context;
        this.lblCantComments = lblCantComments;
    }

    @Override
    protected void onPreExecute() {
        if (progressBar != null)
            progressBar.setVisibility(View.VISIBLE);

        if (lblCantComments != null)
            lblCantComments.setText("0");

    }

    @Override
    protected List<ParseZComment> doInBackground(ParseZimess... zimess) {
        return DataParseHelper.findComments(zimess[0]);
    }

    @Override
    protected void onPostExecute(List<ParseZComment> arrayListComment) {
        CommentsAdapter adapterComment = new CommentsAdapter(context, arrayListComment);
        listViewComment.setAdapter(adapterComment);

        if (lblCantComments != null)
            lblCantComments.setText(Integer.toString(arrayListComment.size()));

        if (progressBar != null)
            progressBar.setVisibility(View.GONE);

        //scrollMyListViewToBottom(adapterComment.getCount() - 1);
        swipeRefreshLayout.setRefreshing(false);
    }

    private void scrollMyListViewToBottom(final Integer itemSelection) {
        listViewComment.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                listViewComment.setSelection(itemSelection);
            }
        });
    }
}
