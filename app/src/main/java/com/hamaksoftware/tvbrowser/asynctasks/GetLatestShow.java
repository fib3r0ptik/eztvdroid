package com.hamaksoftware.tvbrowser.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.hamaksoftware.tvbrowser.fragments.IAsyncTaskListener;
import com.hamaksoftware.tvbrowser.models.Episode;
import com.hamaksoftware.tvbrowser.utils.Utility;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class GetLatestShow extends AsyncTask<Void, Void, ArrayList<Episode>> {
    public static final String ASYNC_ID = "GETLATESTSHOW";
    private int page;
    private Context ctx;
    public IAsyncTaskListener asyncTaskListener;

    public GetLatestShow(Context ctx, int page) {
        this.page = page;
        this.ctx = ctx;
    }

    @Override
    protected void onPreExecute() {
        asyncTaskListener.onTaskWorking(ASYNC_ID);
    }

    @Override
    protected ArrayList<Episode> doInBackground(Void... voids) {

        ArrayList<Episode> items = new ArrayList<Episode>(0);
        try {
            ArrayList<NameValuePair> param = new ArrayList<NameValuePair>(2);
            param.add(new BasicNameValuePair("page", page + ""));
            param.add(new BasicNameValuePair("method", "getLatest"));
            String response = Utility.getInstance(ctx).doPostRequest(param);
            JSONObject jResponse = new JSONObject(response);


            if (jResponse.getInt("err") == 0) {
                JSONArray latest = jResponse.getJSONArray("data");
                for (int i = 0; i < latest.length(); i++) {
                    JSONObject item = latest.getJSONObject(i);
                    if (!item.getString("show_id").equals("add")) {
                        Episode row = new Episode();
                        row.title = item.getString("title");
                        row.filesize = Utility.getFancySize(item.getLong("size"));
                        DateFormat formatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
                        Date dte = formatter.parse(item.getString("pubdate"));
                        row.elapsed = Utility.getInstance(ctx).getPrettytime().format(dte);
                        row.showId = Integer.parseInt(item.getString("show_id"));

                        JSONArray jLinks = item.getJSONArray("links");
                        for (int j = 0; j < jLinks.length(); j++) {
                            row.links.add(jLinks.getString(j));
                        }

                        //row.isFavorite = isFavorite(row.showId);
                        items.add(row);
                    }
                }

                //cloneItems();
            } else {
                Log.i("err", response);
            }
        } catch (Exception e) {
            Log.e("err", e.getMessage());
        }
        return items;
    }

    @Override
    protected void onPostExecute(ArrayList<Episode> data) {
        asyncTaskListener.onTaskCompleted(data, ASYNC_ID);
    }

    /*
    public boolean isFavorite(int showId){
        boolean isFav = false;
        if(showId==187) return false;
        try{
            Show row = sh.getShow(showId);
            isFav = row.isSubscribed;
        }catch(Exception e){
            return false;
        }
        return isFav;
    }
    */
}
