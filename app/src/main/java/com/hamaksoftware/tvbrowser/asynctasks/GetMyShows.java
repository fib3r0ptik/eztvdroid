package com.hamaksoftware.tvbrowser.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.hamaksoftware.tvbrowser.fragments.IAsyncTaskListener;
import com.hamaksoftware.tvbrowser.utils.AppPref;

import java.io.IOException;
import java.util.List;

import info.besiera.api.APIRequest;
import info.besiera.api.APIRequestException;
import info.besiera.api.models.Subscription;

public class GetMyShows extends AsyncTask<Void, Void, List<Subscription>>{
    public AppPref pref;
    public static final String ASYNC_ID = "GETMYSHOWS";
    private int page;
    private Context ctx;

    public IAsyncTaskListener asyncTaskListener;

    public GetMyShows(Context ctx) {
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
        int cacheSize = 15 * 1024 * 1024; // 10 MiB
        try {
            apiRequest.setClientCacheInfo(cacheSize, ctx.getCacheDir().getAbsolutePath());
        }catch (IOException e){
            Log.e("api","Unable to set Cache Information for API Request.");
        }
        try {
            return apiRequest.getMyShows(pref.getDeviceId());
        } catch (APIRequestException e) {
            Log.e("api:getmyshows",e.getStatus().toString());
            asyncTaskListener.onTaskError(e, ASYNC_ID);
        }

        return null;
    }

    @Override
    protected void onPostExecute(List<Subscription> data) {
        asyncTaskListener.onTaskCompleted(data, ASYNC_ID);
    }
}
