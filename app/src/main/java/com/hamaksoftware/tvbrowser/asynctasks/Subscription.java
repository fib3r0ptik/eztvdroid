package com.hamaksoftware.tvbrowser.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.hamaksoftware.tvbrowser.fragments.IAsyncTaskListener;
import com.hamaksoftware.tvbrowser.models.Show;
import com.hamaksoftware.tvbrowser.utils.AppPref;
import com.hamaksoftware.tvbrowser.utils.Utility;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Subscription extends AsyncTask<Void, Void, Show> {
    public static final String ASYNC_ID = "SUBSCRIPTION";
    private Show show;
    private Context ctx;
    private AppPref pref;
    public IAsyncTaskListener asyncTaskListener;
    public boolean isSubscribe;

    public Subscription(Context ctx, Show show) {
        this.show = show;
        this.ctx = ctx;
        pref = new AppPref(ctx);
    }

    @Override
    protected void onPreExecute() {
        asyncTaskListener.onTaskWorking(ASYNC_ID);
    }

    @Override
    protected Show doInBackground(Void... voids) {
        boolean ret;
        ArrayList<NameValuePair> param = new ArrayList<NameValuePair>(3);

        param.add(new BasicNameValuePair("method", isSubscribe ? "subscribe" : "unsubscribe"));
        param.add(new BasicNameValuePair("dev_id", pref.getDeviceId()));
        param.add(new BasicNameValuePair("show_id", show.showId + ""));
        String res = Utility.getInstance(ctx).doPostRequest(param);
        Log.i("uri", res);
        try {
            JSONObject obj = new JSONObject(res);
            ret = (obj.getInt("success") == 1);
            if (ret) {
                show.isSubscribed = isSubscribe;
                show.save();
            }
        } catch (JSONException e) {
            ret = false;
        }


        return show;

    }

    @Override
    protected void onPostExecute(Show d) {
        asyncTaskListener.onTaskCompleted(d, ASYNC_ID);
    }
}
