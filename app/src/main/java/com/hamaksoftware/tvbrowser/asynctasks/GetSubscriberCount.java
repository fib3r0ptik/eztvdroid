package com.hamaksoftware.tvbrowser.asynctasks;

import android.content.Context;
import android.os.AsyncTask;

import com.hamaksoftware.tvbrowser.fragments.IAsyncTaskListener;
import com.hamaksoftware.tvbrowser.models.Show;
import com.hamaksoftware.tvbrowser.utils.ShowHandler;
import com.hamaksoftware.tvbrowser.utils.Utility;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;

public class GetSubscriberCount extends AsyncTask<Void, Void, String>{
    public static final String ASYNC_ID = "GETSUBSCRIBERCOUNT";
    private Show show;
    private Context ctx;
    private ShowHandler sh;
    public IAsyncTaskListener asyncTaskListener;


    public GetSubscriberCount(Context ctx, Show show){
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
