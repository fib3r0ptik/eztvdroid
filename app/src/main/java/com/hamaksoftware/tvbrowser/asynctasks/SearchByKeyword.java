package com.hamaksoftware.tvbrowser.asynctasks;

import android.os.AsyncTask;

import com.hamaksoftware.tvbrowser.fragments.IAsyncTaskListener;

import java.util.List;

import info.besiera.api.APIRequest;
import info.besiera.api.APIRequestException;
import info.besiera.api.models.Episode;

public class SearchByKeyword extends AsyncTask<Void, Void, List<Episode>> {
    public static final String ASYNC_ID = "SEARCHBYKEY";
    private String keyword;

    public IAsyncTaskListener asyncTaskListener;

    public SearchByKeyword(String keyword) {
        this.keyword = keyword;
    }

    @Override
    protected void onPreExecute() {
        asyncTaskListener.onTaskWorking(ASYNC_ID);
    }

    @Override
    protected List<Episode> doInBackground(Void... voids) {
        try {
            APIRequest apiRequest = new APIRequest();
            return apiRequest.searchByKeyword(keyword);
        } catch (APIRequestException e) {
            asyncTaskListener.onTaskError(e, ASYNC_ID);
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<Episode> data) {
        asyncTaskListener.onTaskCompleted(data, ASYNC_ID);
    }

}
