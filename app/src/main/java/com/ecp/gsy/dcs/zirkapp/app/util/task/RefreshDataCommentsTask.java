package com.ecp.gsy.dcs.zirkapp.app.util.task;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ecp.gsy.dcs.zirkapp.app.util.adapters.CommentsAdapter;
import com.ecp.gsy.dcs.zirkapp.app.util.beans.ZimessComment;
import com.ecp.gsy.dcs.zirkapp.app.util.parse.DataParseHelper;
import com.parse.ParseObject;

import java.util.ArrayList;

/**
 * Created by Elder on 08/03/2015.
 */
public class RefreshDataCommentsTask extends AsyncTask<String, Void, ArrayList<ZimessComment>> {

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
    protected ArrayList<ZimessComment> doInBackground(String... strings) {
        ArrayList<ZimessComment> arrayListComment = new ArrayList<ZimessComment>();

        for (ParseObject comment : DataParseHelper.findComments(strings[0])) {
            ZimessComment zcomment = new ZimessComment();
            zcomment.setCommentText(comment.get("commentText").toString());
            zcomment.setUserComment(comment.getParseUser("user"));
            zcomment.setCreateAt(comment.getCreatedAt());

            arrayListComment.add(zcomment);
        }

        return arrayListComment;
    }

    @Override
    protected void onPostExecute(ArrayList<ZimessComment> arrayListComment) {
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
