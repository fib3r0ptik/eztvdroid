package com.hamaksoftware.tvbrowser.asynctasks;

import android.os.AsyncTask;

import com.hamaksoftware.tvbrowser.fragments.IAsyncTaskListener;

import info.besiera.api.APIRequest;
import info.besiera.api.APIRequestException;
import info.besiera.api.models.Show;


public class GetShowDetails extends AsyncTask<Void, Void, Show> {
    public static final String ASYNC_ID = "GETSHOWDETAILS";
    public IAsyncTaskListener asyncTaskListener;

    private int showId;

    public GetShowDetails(int showId) {
        this.showId = showId;
    }

    @Override
    protected void onPreExecute() {
        asyncTaskListener.onTaskWorking(ASYNC_ID);
    }

    @Override
    protected Show doInBackground(Void... voids) {
        try {
            APIRequest apiRequest = new APIRequest();
            Show show = apiRequest.getShow(showId);
            return show;
        } catch (APIRequestException e) {
            asyncTaskListener.onTaskError(e, ASYNC_ID);
        }

        return null;
    }

    @Override
    protected void onPostExecute(Show data) {
        asyncTaskListener.onTaskCompleted(data, ASYNC_ID);
    }
}
