package com.hamaksoftware.tvbrowser.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.hamaksoftware.tvbrowser.fragments.IAsyncTaskListener;

import info.besiera.api.APIRequest;
import info.besiera.api.APIRequestException;

public class UnRegisterDevice extends AsyncTask<Void, Void, Boolean> {
    public static final String ASYNC_ID = "UNREGISTERDEVICE";
    private Context ctx;
    public IAsyncTaskListener asyncTaskListener;
    private String deviceId;

    public UnRegisterDevice(Context ctx, String deviceId) {
        this.ctx = ctx;
        this.deviceId = deviceId;
    }

    @Override
    protected void onPreExecute() {
        asyncTaskListener.onTaskWorking(ASYNC_ID);
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        APIRequest apiRequest = new APIRequest();
        try {
            return apiRequest.unRegisterDevice(deviceId);
        } catch (APIRequestException e) {
            Log.e("err", e.getStatus().toString());
        }

        return null;
    }

    @Override
    protected void onPostExecute(Boolean data) {
        asyncTaskListener.onTaskCompleted(data, ASYNC_ID);
    }
}
