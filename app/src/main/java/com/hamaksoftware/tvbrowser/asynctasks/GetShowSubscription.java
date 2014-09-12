package com.hamaksoftware.tvbrowser.asynctasks;

import android.content.Context;
import android.os.AsyncTask;

import com.hamaksoftware.tvbrowser.fragments.IAsyncTaskListener;
import com.hamaksoftware.tvbrowser.utils.AppPref;

import info.besiera.api.APIRequest;
import info.besiera.api.APIRequestException;
import info.besiera.api.models.Show;

public class GetShowSubscription extends AsyncTask<Void, Void, Show> {
    public static final String ASYNC_ID = "GETSHOWSUBSCRIPTION";
    public IAsyncTaskListener asyncTaskListener;

    private int showId;
    private AppPref pref;

    public GetShowSubscription(Context ctx,int showId) {
        pref = new AppPref(ctx);
        this.showId = showId;
    }

    @Override
    protected void onPreExecute() {
        asyncTaskListener.onTaskWorking(ASYNC_ID);
    }

    @Override
    protected Show doInBackground(Void... voids) {
        APIRequest apiRequest = new APIRequest();
        try {
            return apiRequest.isSubsribed(pref.getDeviceId(), showId);
        } catch (APIRequestException e) {
            asyncTaskListener.onTaskError(e,ASYNC_ID);
        }

        return null;
    }

    @Override
    protected void onPostExecute(Show data) {
        asyncTaskListener.onTaskCompleted(data, ASYNC_ID);
    }
}
