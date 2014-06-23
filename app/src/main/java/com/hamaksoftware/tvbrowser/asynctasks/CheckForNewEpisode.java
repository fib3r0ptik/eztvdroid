package com.hamaksoftware.tvbrowser.asynctasks;

import android.content.Context;
import android.os.AsyncTask;

import com.activeandroid.query.Select;
import com.hamaksoftware.tvbrowser.fragments.IAsyncTaskListener;
import com.hamaksoftware.tvbrowser.models.Show;
import com.hamaksoftware.tvbrowser.utils.Utility;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

public class CheckForNewEpisode extends AsyncTask<Void, Void, String> {
    public static final String ASYNC_ID = "CheckForNewEpisode";
    private Show show;
    private Context ctx;
    public IAsyncTaskListener asyncTaskListener;


    public CheckForNewEpisode(Context ctx) {
        this.show = show;
        this.ctx = ctx;
    }

    @Override
    protected void onPreExecute() {
        asyncTaskListener.onTaskWorking(ASYNC_ID);
    }

    @Override
    protected String doInBackground(Void... voids) {
        List<Show> myShows = new Select().from(Show.class).where("isSubscribed=?", true).execute();
        if (myShows.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (Show show : myShows) {
                sb.append(show.showId).append(",");
            }

            sb.delete(sb.length() - 1, sb.length());

            ArrayList<NameValuePair> param = new ArrayList<NameValuePair>(2);
            param.add(new BasicNameValuePair("show_ids", sb.toString()));
            param.add(new BasicNameValuePair("method", "checkForNewEpisode"));
            return Utility.getInstance(ctx).doPostRequest(param);
        } else {
            return "[]";
        }

    }

    @Override
    protected void onPostExecute(String response) {
        asyncTaskListener.onTaskCompleted(response, ASYNC_ID);
    }
}
