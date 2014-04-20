package com.hamaksoftware.eztvdroid.asynctasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import com.hamaksoftware.eztvdroid.fragments.IAsyncTaskListener;
import com.hamaksoftware.eztvdroid.models.EZTVShowItem;
import com.hamaksoftware.eztvdroid.utils.ShowHandler;
import com.hamaksoftware.eztvdroid.utils.Utility;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class GetSubscriberCount extends AsyncTask<Void, Void, String>{
    public static final String ASYNC_ID = "GETSUBSCRIBERCOUNT";
    private EZTVShowItem show;
    private Context ctx;
    private ShowHandler sh;
    public IAsyncTaskListener asyncTaskListener;


    public GetSubscriberCount(Context ctx, EZTVShowItem show){
        this.show = show;
        sh = new ShowHandler(ctx);
        this.ctx  = ctx;
    }

    @Override
    protected void onPreExecute(){
        asyncTaskListener.onTaskWorking(ASYNC_ID);
    }

    @Override
    protected String doInBackground(Void... voids) {
        ArrayList<NameValuePair> param = new ArrayList<NameValuePair>(2);
        param.add(new BasicNameValuePair("show_id", show.showId+""));
        param.add(new BasicNameValuePair("method", "getShowExtendedInfo"));
        return Utility.getInstance(ctx).doPostRequest(param);
    }
    @Override
    protected void onPostExecute(String response) {
       asyncTaskListener.onTaskCompleted(response,ASYNC_ID);
    }
}
