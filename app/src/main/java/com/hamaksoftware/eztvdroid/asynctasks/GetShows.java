package com.hamaksoftware.eztvdroid.asynctasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.hamaksoftware.eztvdroid.R;
import com.hamaksoftware.eztvdroid.fragments.IAsyncTaskListener;
import com.hamaksoftware.eztvdroid.models.EZTVRow;
import com.hamaksoftware.eztvdroid.models.EZTVShowItem;
import com.hamaksoftware.eztvdroid.utils.AppPref;
import com.hamaksoftware.eztvdroid.utils.EZTVScraper;
import com.hamaksoftware.eztvdroid.utils.ShowHandler;
import com.hamaksoftware.eztvdroid.utils.Utility;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class GetShows extends AsyncTask<Void, Void, ArrayList<EZTVShowItem>>{
    private int page;
    private Context ctx;
    private ShowHandler sh;
    private boolean forced;
    private AppPref pref;

    public IAsyncTaskListener asyncTaskListener;

    public GetShows(Context ctx, boolean forced){
        this.page = page;
        sh = new ShowHandler(ctx);
        this.ctx  = ctx;
        this.forced = forced;
        pref = new AppPref(ctx);
    }

    @Override
    protected void onPreExecute(){
        asyncTaskListener.onTaskWorking();
    }

    @Override
    protected ArrayList<EZTVShowItem> doInBackground(Void... voids) {
        ArrayList<EZTVShowItem> shows = new ArrayList<EZTVShowItem>(0);
        asyncTaskListener.onTaskUpdateMessage("Reloading/Caching shows...");
        int count = sh.getCount();
        try {

            if(forced || count<= 0){
                int ctr = 0;
                ArrayList<NameValuePair> param = new ArrayList<NameValuePair>(1);
                param.add(new BasicNameValuePair("method", "getShows"));
                String response = Utility.getInstance(ctx).doPostRequest(param);
                JSONArray jShows = new JSONArray(response);
                Log.i("api", "ttl shows:"+ jShows.length());
                //my shows
                param = new ArrayList<NameValuePair>(3);
                param.add(new BasicNameValuePair("dev_id",pref.getDeviceId()));
                param.add(new BasicNameValuePair("method", "getMyshows"));
                response = Utility.getInstance(ctx).doPostRequest(param);
                JSONArray myShows = new JSONArray(response);
                Log.i("api", "ttl my shows:"+ myShows.length());

                asyncTaskListener.onTaskProgressMax(jShows.length());
                sh.deleteAll();
                for(int i = 0;  i < jShows.length();i++){
                    EZTVShowItem show = new EZTVShowItem();
                    JSONObject item = jShows.getJSONObject(i);
                    show.title = item.getString("title");
                    show.status = item.getString("status");
                    show.showId = Integer.parseInt(item.getString("id"));

                    String append = null;
                    for(int j = 0; j < myShows.length();j++){
                        JSONObject obj = myShows.getJSONObject(j);
                        int id = obj.getInt("id");
                        if(id == show.showId){
                            append = ctx.getResources().getString(R.string.tab_show_myshow);
                            show.isSubscribed = true;
                            break;
                        }else{
                            append = "";
                        }
                    }

                    sh.addShow(show);
                    shows.add(show);
                    asyncTaskListener.onTaskProgressUpdate(ctr);
                    ctr++;
                }

            }else{
                shows = sh.getAllShows();
            }
            //cloneShowItems();
        } catch (JSONException e) {
            Log.e("err", e.getMessage());
        }finally{
            sh.close();
        }

        return shows;
    }

    @Override
    protected void onPostExecute(ArrayList<EZTVShowItem> data) {
        asyncTaskListener.onTaskCompleted(data);
    }
}
