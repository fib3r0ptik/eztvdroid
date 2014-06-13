package com.hamaksoftware.eztvpal.asynctasks;

import android.content.Context;
import android.os.AsyncTask;

import com.hamaksoftware.eztvpal.fragments.IAsyncTaskListener;
import com.hamaksoftware.eztvpal.models.Show;
import com.hamaksoftware.eztvpal.utils.ShowHandler;
import com.hamaksoftware.eztvpal.utils.Utility;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;

public class CheckForNewEpisode extends AsyncTask<Void, Void, String>{
    public static final String ASYNC_ID = "CheckForNewEpisode";
    private Show show;
    private Context ctx;
    private ShowHandler sh;
    public IAsyncTaskListener asyncTaskListener;


    public CheckForNewEpisode(Context ctx){
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
        ShowHandler sh = new ShowHandler(ctx);
        ArrayList<Show> myShows =  sh.getMyShows();
        if(myShows.size() > 0){
            StringBuilder sb = new StringBuilder();
            for(Show show: myShows){
                sb.append(show.showId).append(",");
            }

            sb.delete(sb.length()-1, sb.length());

            ArrayList<NameValuePair> param = new ArrayList<NameValuePair>(2);
            param.add(new BasicNameValuePair("show_ids",sb.toString()));
            param.add(new BasicNameValuePair("method", "checkForNewEpisode"));
            return Utility.getInstance(ctx).doPostRequest(param);
        }else{
            return "[]";
        }

    }
    @Override
    protected void onPostExecute(String response) {
       asyncTaskListener.onTaskCompleted(response,ASYNC_ID);
    }
}
