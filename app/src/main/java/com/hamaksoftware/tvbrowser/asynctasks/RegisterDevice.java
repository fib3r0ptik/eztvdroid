package com.hamaksoftware.tvbrowser.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.hamaksoftware.tvbrowser.fragments.IAsyncTaskListener;

import info.besiera.api.APIRequest;
import info.besiera.api.APIRequestException;

public class RegisterDevice extends AsyncTask<Void, Void, Boolean> {
    public static final String ASYNC_ID = "REGISTERDEVICE";
    private Context ctx;
    public IAsyncTaskListener asyncTaskListener;
    private String deviceId;
    private String registrationId;

    public RegisterDevice(Context ctx, String deviceId, String registrationId) {
        this.ctx = ctx;
        this.deviceId = deviceId;
        this.registrationId = registrationId;
    }

    @Override
    protected void onPreExecute() {
        asyncTaskListener.onTaskWorking(ASYNC_ID);
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        APIRequest apiRequest = new APIRequest();
        try {
            return apiRequest.registerDevice(deviceId, registrationId);
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
