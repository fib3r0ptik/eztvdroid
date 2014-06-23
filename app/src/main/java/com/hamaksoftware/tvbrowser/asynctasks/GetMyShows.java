package com.hamaksoftware.tvbrowser.asynctasks;

import android.content.Context;
import android.os.AsyncTask;

import com.activeandroid.query.Select;
import com.hamaksoftware.tvbrowser.fragments.IAsyncTaskListener;
import com.hamaksoftware.tvbrowser.models.Show;

import java.util.ArrayList;
import java.util.List;

public class GetMyShows extends AsyncTask<Void, Void, ArrayList<Show>> {
    public static final String ASYNC_ID = "GETMYSHOWS";
    private int page;
    private Context ctx;

    public IAsyncTaskListener asyncTaskListener;

    public GetMyShows(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    protected void onPreExecute() {
        asyncTaskListener.onTaskWorking(ASYNC_ID);
    }

    @Override
    protected ArrayList<Show> doInBackground(Void... voids) {
        List<Show> shows = new Select().from(Show.class).where("isSubscribed=?", true).execute();
        return (ArrayList<Show>) shows;
    }

    @Override
    protected void onPostExecute(ArrayList<Show> data) {
        asyncTaskListener.onTaskCompleted(data, ASYNC_ID);
    }
}
