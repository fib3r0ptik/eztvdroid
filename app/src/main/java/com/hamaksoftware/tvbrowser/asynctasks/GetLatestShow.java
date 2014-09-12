package com.hamaksoftware.tvbrowser.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.hamaksoftware.tvbrowser.fragments.IAsyncTaskListener;
import com.hamaksoftware.tvbrowser.utils.Utility;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import info.besiera.api.APIRequest;
import info.besiera.api.APIRequestException;
import info.besiera.api.models.Episode;

public class GetLatestShow extends AsyncTask<Void, Void, List<Episode>> {
    public static final String ASYNC_ID = "GETLATESTSHOW";
    private int page;
    private Context ctx;
    public IAsyncTaskListener asyncTaskListener;

    public GetLatestShow(Context ctx, int page) {
        this.page = page;
        this.ctx = ctx;
    }

    @Override
    protected void onPreExecute() {
        asyncTaskListener.onTaskWorking(ASYNC_ID);
    }

    @Override
    protected List<Episode> doInBackground(Void... voids) {
        APIRequest apiRequest = new APIRequest();
        try {
            List<Episode> episodes = apiRequest.getLatest(page);
            return episodes;
        } catch (APIRequestException e) {
            asyncTaskListener.onTaskError(e,ASYNC_ID);
        }

        return null;
    }

    @Override
    protected void onPostExecute(List<Episode> data) {
        asyncTaskListener.onTaskCompleted(data, ASYNC_ID);
    }

}
