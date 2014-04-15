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

public class GetLatestShow extends AsyncTask<Void, Void, ArrayList<EZTVRow>>{
    private int page;
    private Context ctx;
    private ShowHandler sh;
    public IAsyncTaskListener asyncTaskListener;

    public GetLatestShow(Context ctx,int page){
        this.page = page;
        sh = new ShowHandler(ctx);
        this.ctx  = ctx;
    }

    @Override
    protected void onPreExecute(){
        asyncTaskListener.onTaskWorking();
    }

    @Override
    protected ArrayList<EZTVRow> doInBackground(Void... voids) {
        ArrayList<EZTVRow> items = new ArrayList<EZTVRow>(0);
        try{
            ArrayList<NameValuePair> param = new ArrayList<NameValuePair>(2);
            param.add(new BasicNameValuePair("page", page+""));
            param.add(new BasicNameValuePair("method", "getLatest"));
            String response = Utility.getInstance(ctx).doPostRequest(param);
            JSONObject jResponse = new JSONObject(response);
            if(jResponse.getInt("err") == 0){
                JSONArray latest = jResponse.getJSONArray("data");
                for(int i = 0; i < latest.length();i++){
                    JSONObject item = latest.getJSONObject(i);
                    if(!item.getString("show_id").equals("add")){
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
                }

                //cloneItems();
            }else{
                Log.i("err", response);
            }
        }catch (Exception e) {
            Log.e("err",e.getMessage());
        }
        return items;
    }
    @Override
    protected void onPostExecute(ArrayList<EZTVRow> data) {
        asyncTaskListener.onTaskCompleted(data);
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
