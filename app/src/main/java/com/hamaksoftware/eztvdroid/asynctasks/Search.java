package com.hamaksoftware.eztvdroid.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.hamaksoftware.eztvdroid.fragments.IAsyncTaskListener;
import com.hamaksoftware.eztvdroid.models.EZTVRow;
import com.hamaksoftware.eztvdroid.models.EZTVShowItem;
import com.hamaksoftware.eztvdroid.utils.ShowHandler;
import com.hamaksoftware.eztvdroid.utils.Utility;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class Search extends AsyncTask<Void, Void, ArrayList<EZTVRow>>{
    public static final String ASYNC_ID = "SEARCH";
    private Context ctx;
    private ShowHandler sh;
    private String query;
    private boolean byId;

    public IAsyncTaskListener asyncTaskListener;

    public Search(Context ctx, String query, boolean byId){
        sh = new ShowHandler(ctx);
        this.ctx  = ctx;
        this.query = query;
        this.byId = byId;
    }

    @Override
    protected void onPreExecute(){
        asyncTaskListener.onTaskWorking(ASYNC_ID);
    }

    @Override
    protected ArrayList<EZTVRow> doInBackground(Void... voids) {
        ArrayList<EZTVRow> items = new ArrayList<EZTVRow>(0);
        String response = "";
        try{
            ArrayList<NameValuePair> param = new ArrayList<NameValuePair>(4);
            if(byId) param.add(new BasicNameValuePair("show_id", query));
            param.add(new BasicNameValuePair("byid", byId+""));
            param.add(new BasicNameValuePair("query", query));
            param.add(new BasicNameValuePair("method", "search"));
            response = Utility.getInstance(ctx).doPostRequest(param);
            //Log.i("search",response);
            JSONObject jResponse = new JSONObject(response);
            if(jResponse.getInt("err") == 0){
                JSONArray latest = jResponse.getJSONArray("data");
                for(int i = 0; i < latest.length();i++){
                    JSONObject item = latest.getJSONObject(i);
                    EZTVRow row = new EZTVRow();
                    row.title = item.getString("title");
                    row.filesize = Utility.getFancySize(item.getLong("size"));
                    row.elapsed = Utility.getElapsed(item.getString("pubdate"));
                    row.showId = Integer.parseInt(item.getString("show_id"));

                    JSONArray jLinks = item.getJSONArray("links");
                    for(int j = 0; j < jLinks.length();j++){
                        row.links.add(jLinks.getString(j));
                    }
                    row.isFavorite = isFavorite(row.showId);
                    items.add(row);
                }
            }else{
                Log.i("err",response);
            }
        }catch (Exception e) {
            Log.i("err", response);
        }
        return items;
    }
    @Override
    protected void onPostExecute(ArrayList<EZTVRow> data) {
        asyncTaskListener.onTaskCompleted(data,ASYNC_ID);
    }

    public boolean isFavorite(int showId){
        boolean isFav;
        if(showId==187) return false;
        try{
            EZTVShowItem row = sh.getShow(showId);
            isFav = row.isSubscribed;
        }catch(Exception e){
            return false;
        }

        return isFav;
    }
}
