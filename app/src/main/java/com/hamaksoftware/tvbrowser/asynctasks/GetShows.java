package com.hamaksoftware.tvbrowser.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.hamaksoftware.tvbrowser.fragments.IAsyncTaskListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import info.besiera.api.APIRequest;
import info.besiera.api.APIRequestException;
import info.besiera.api.models.Show;

public class GetShows extends AsyncTask<Void, Void, ArrayList<Show>> {
    public static final String ASYNC_ID = "GETSHOWS";
    private Context ctx;
    public IAsyncTaskListener asyncTaskListener;

    public GetShows(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    protected void onPreExecute() {
        asyncTaskListener.onTaskWorking(ASYNC_ID);
    }

    @Override
    protected ArrayList<Show> doInBackground(Void... voids) {
        try {
            APIRequest apiRequest = new APIRequest();
            int cacheSize = 15 * 1024 * 1024; // 10 MiB
            try {
                apiRequest.setClientCacheInfo(cacheSize, ctx.getCacheDir().getAbsolutePath());
            } catch (IOException e) {
                Log.e("api", "Unable to set Cache Information for API Request.");
            }

            ArrayList<Show> shows = new ArrayList<Show>(apiRequest.getShows());
            Collections.sort(shows, new Comparator<Show>() {
                @Override
                public int compare(Show show, Show show2) {
                    return show2.getSubscriberCount() - show.getSubscriberCount();
                }
            });
            return shows;
        } catch (APIRequestException e) {
            asyncTaskListener.onTaskError(e, ASYNC_ID);
        }
        return null;
    }

    @Override
    protected void onPostExecute(ArrayList<Show> data) {
        asyncTaskListener.onTaskCompleted(data, ASYNC_ID);
    }
}
