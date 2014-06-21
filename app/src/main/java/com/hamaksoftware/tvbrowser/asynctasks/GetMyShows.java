package com.hamaksoftware.tvbrowser.asynctasks;

import android.content.Context;
import android.os.AsyncTask;

import com.hamaksoftware.tvbrowser.fragments.IAsyncTaskListener;
import com.hamaksoftware.tvbrowser.models.Show;
import com.hamaksoftware.tvbrowser.utils.ShowHandler;

import java.util.ArrayList;

public class GetMyShows extends AsyncTask<Void, Void, ArrayList<Show>> {
    public static final String ASYNC_ID = "GETMYSHOWS";
    private int page;
    private Context ctx;
    private ShowHandler sh;

    public IAsyncTaskListener asyncTaskListener;

    public GetMyShows(Context ctx) {
        sh = new ShowHandler(ctx);
        this.ctx = ctx;
    }

    @Override
    protected void onPreExecute() {
        asyncTaskListener.onTaskWorking(ASYNC_ID);
    }

    @Override
    protected ArrayList<Show> doInBackground(Void... voids) {
        return sh.getMyShows();
    }

    @Override
    protected void onPostExecute(ArrayList<Show> data) {
        asyncTaskListener.onTaskCompleted(data, ASYNC_ID);
    }
}
