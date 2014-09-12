package com.hamaksoftware.tvbrowser.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.hamaksoftware.tvbrowser.fragments.IAsyncTaskListener;
import com.hamaksoftware.tvbrowser.utils.AppPref;

import info.besiera.api.APIRequest;
import info.besiera.api.APIRequestException;
import info.besiera.api.models.ResponseTemplate;

public class UnSubscribe extends AsyncTask<Void, Void, Boolean> {
    public static final String ASYNC_ID = "UNSUBSCRIBE";
    private int showId;
    private Context ctx;
    public IAsyncTaskListener asyncTaskListener;
    private AppPref pref;

    public UnSubscribe(Context ctx, int showId) {
        this.showId = showId;
        this.ctx = ctx;
        pref = new AppPref(ctx);
    }

    @Override
    protected void onPreExecute() {
        asyncTaskListener.onTaskWorking(ASYNC_ID);
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        APIRequest apiRequest = new APIRequest();
        try {
            return apiRequest.unSubscribe(pref.getDeviceId(), showId);
        } catch (APIRequestException e) {
            Log.e("err",e.getStatus().toString());
        }

        return null;
    }

    @Override
    protected void onPostExecute(Boolean data) {
        asyncTaskListener.onTaskCompleted(data, ASYNC_ID);
    }
}
