package com.hamaksoftware.eztvpal.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.hamaksoftware.eztvpal.R;
import com.hamaksoftware.eztvpal.fragments.IAsyncTaskListener;
import com.hamaksoftware.eztvpal.models.Show;
import com.hamaksoftware.eztvpal.utils.AppPref;
import com.hamaksoftware.eztvpal.utils.ShowHandler;
import com.hamaksoftware.eztvpal.utils.Utility;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class GetShows extends AsyncTask<Void, Void, ArrayList<Show>>{
    public static final String ASYNC_ID = "GETSHOWS";
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
        asyncTaskListener.onTaskWorking(ASYNC_ID);
    }

    @Override
    protected ArrayList<Show> doInBackground(Void... voids) {
        ArrayList<Show> shows = new ArrayList<Show>(0);
        asyncTaskListener.onTaskUpdateMessage("Reloading/Caching shows...",ASYNC_ID);
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

                asyncTaskListener.onTaskProgressMax(jShows.length(),ASYNC_ID);
                sh.deleteAll();
                for(int i = 0;  i < jShows.length();i++){
                    Show show = new Show();
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
                    asyncTaskListener.onTaskProgressUpdate(ctr,ASYNC_ID);
                    ctr++;
                }

            }else{
                shows = sh.getAllShows();
            }
            //cloneShowItems();
        } catch (JSONException e) {
            Log.e("err", e.getMessage());
        }

        return shows;
    }

    @Override
    protected void onPostExecute(ArrayList<Show> data) {
        asyncTaskListener.onTaskCompleted(data,ASYNC_ID);
    }
}
