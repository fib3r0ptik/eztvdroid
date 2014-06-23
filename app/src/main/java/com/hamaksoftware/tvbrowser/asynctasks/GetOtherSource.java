package com.hamaksoftware.tvbrowser.asynctasks;

import android.content.Context;
import android.os.AsyncTask;

import com.hamaksoftware.tvbrowser.fragments.IAsyncTaskListener;
import com.hamaksoftware.tvbrowser.models.RSSItem;
import com.hamaksoftware.tvbrowser.utils.FeedParser;

import java.util.ArrayList;

public class GetOtherSource extends AsyncTask<Void, Void, ArrayList<RSSItem>> {
    public static final String ASYNC_ID = "GETFEED";
    private String uri;
    private Context ctx;

    public IAsyncTaskListener asyncTaskListener;
    private FeedParser fp;

    public GetOtherSource(Context ctx, String uri) {
        this.uri = uri;

        this.ctx = ctx;
        fp = new FeedParser(ctx, uri);
    }

    @Override
    protected void onPreExecute() {
        asyncTaskListener.onTaskWorking(ASYNC_ID);
    }

    @Override
    protected ArrayList<RSSItem> doInBackground(Void... voids) {
        ArrayList<RSSItem> items = new ArrayList<RSSItem>(0);
        return fp.getFeed().itemlist;
    }

    @Override
    protected void onPostExecute(ArrayList<RSSItem> data) {
        asyncTaskListener.onTaskCompleted(data, ASYNC_ID);
    }

}
