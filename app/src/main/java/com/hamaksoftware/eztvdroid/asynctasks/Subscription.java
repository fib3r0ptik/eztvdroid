package com.hamaksoftware.eztvdroid.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.hamaksoftware.eztvdroid.fragments.IAsyncTaskListener;
import com.hamaksoftware.eztvdroid.models.Show;
import com.hamaksoftware.eztvdroid.utils.AppPref;
import com.hamaksoftware.eztvdroid.utils.Utility;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Subscription extends AsyncTask<Void, Void, Boolean>{
    public static final String ASYNC_ID = "SUBSCRIPTION";
    private Show show;
    private Context ctx;
    private AppPref pref;
    public IAsyncTaskListener asyncTaskListener;


    public Subscription(Context ctx, Show show){
        this.show = show;
        this.ctx  = ctx;
        pref = new AppPref(ctx);
    }

    @Override
    protected void onPreExecute(){
        asyncTaskListener.onTaskWorking(ASYNC_ID);
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        boolean ret;
        ArrayList<NameValuePair> param = new ArrayList<NameValuePair>(2);
        if(show.isSubscribed){
            param.add(new BasicNameValuePair("method", "unsubscribe"));
        }else{
            param.add(new BasicNameValuePair("method", "subscribe"));
        }
        param.add(new BasicNameValuePair("dev_id", pref.getDeviceId()));
        param.add(new BasicNameValuePair("show_id", show.showId+""));
        String res = Utility.getInstance(ctx).doPostRequest(param);
        Log.i("uri",res);
        try {
            JSONObject obj = new JSONObject(res);
            ret = (obj.getInt("success") == 1);
        } catch (JSONException e) {
            ret = false;
        }


        return ret;

    }
    @Override
    protected void onPostExecute(Boolean d) {
       asyncTaskListener.onTaskCompleted(d,ASYNC_ID);
    }
}
