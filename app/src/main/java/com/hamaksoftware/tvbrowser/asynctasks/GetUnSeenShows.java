package com.hamaksoftware.tvbrowser.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.hamaksoftware.tvbrowser.fragments.IAsyncTaskListener;
import com.hamaksoftware.tvbrowser.utils.AppPref;

import java.util.List;

import info.besiera.api.APIRequest;
import info.besiera.api.APIRequestException;
import info.besiera.api.models.Subscription;

public class GetUnSeenShows extends AsyncTask<Void, Void, List<Subscription>> {
    public AppPref pref;
    public static final String ASYNC_ID = "GETUNSEENSHOWS";
    private Context ctx;

    public IAsyncTaskListener asyncTaskListener;

    public GetUnSeenShows(Context ctx) {
        this.ctx = ctx;
        pref = new AppPref(ctx);
    }

    @Override
    protected void onPreExecute() {
        asyncTaskListener.onTaskWorking(ASYNC_ID);
    }

    @Override
    protected List<Subscription> doInBackground(Void... voids) {
        APIRequest apiRequest = new APIRequest();
        try {
            return apiRequest.getUnseenSubscription(pref.getDeviceId());
        } catch (APIRequestException e) {
            Log.e("api:getmyunseen", e.getStatus().toString());
            asyncTaskListener.onTaskError(e, ASYNC_ID);
        }

        return null;
    }

    @Override
    protected void onPostExecute(List<Subscription> data) {
        asyncTaskListener.onTaskCompleted(data, ASYNC_ID);
    }
}
