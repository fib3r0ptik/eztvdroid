package com.hamaksoftware.tvbrowser.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.hamaksoftware.tvbrowser.R;
import com.hamaksoftware.tvbrowser.fragments.IAsyncTaskListener;
import com.hamaksoftware.tvbrowser.models.Show;
import com.hamaksoftware.tvbrowser.utils.AppPref;
import com.hamaksoftware.tvbrowser.utils.Utility;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GetShows extends AsyncTask<Void, Void, ArrayList<Show>> {
    public static final String ASYNC_ID = "GETSHOWS";
    private int page;
    private Context ctx;
    private boolean forced;
    private AppPref pref;

    public IAsyncTaskListener asyncTaskListener;

    public GetShows(Context ctx, boolean forced) {
        this.page = page;
        this.ctx = ctx;
        this.forced = forced;
        pref = new AppPref(ctx);
    }

    @Override
    protected void onPreExecute() {
        asyncTaskListener.onTaskWorking(ASYNC_ID);
    }

    @Override
    protected ArrayList<Show> doInBackground(Void... voids) {
        ArrayList<Show> shows = new ArrayList<Show>(0);
        asyncTaskListener.onTaskUpdateMessage("Reloading/Caching shows...", ASYNC_ID);
        try {


            int count = new Select().from(Show.class).count();


            if (forced || count <= 0) {
                int ctr = 0;
                ArrayList<NameValuePair> param = new ArrayList<NameValuePair>(1);
                param.add(new BasicNameValuePair("method", "getShows"));
                String response = Utility.getInstance(ctx).doPostRequest(param);
                JSONArray jShows = new JSONArray(response);
                Log.i("api", "ttl shows:" + jShows.length());
                //my shows
                param = new ArrayList<NameValuePair>(3);
                param.add(new BasicNameValuePair("dev_id", pref.getDeviceId()));
                param.add(new BasicNameValuePair("method", "getMyshows"));
                response = Utility.getInstance(ctx).doPostRequest(param);
                JSONArray myShows = new JSONArray(response);
                Log.i("api", "ttl my shows:" + myShows.length());

                asyncTaskListener.onTaskProgressMax(jShows.length(), ASYNC_ID);

                new Delete().from(Show.class).execute();
                Show _show = new Select().from(Show.class).orderBy("RANDOM()").executeSingle();
                //System.out.println(_show);


                ActiveAndroid.beginTransaction();
                try {
                    for (int i = 0; i < jShows.length(); i++) {
                        Show show = new Show();
                        JSONObject item = jShows.getJSONObject(i);
                        show.title = item.getString("title");
                        show.status = item.getString("status");
                        show.showId = Integer.parseInt(item.getString("id"));

                        String append = null;
                        for (int j = 0; j < myShows.length(); j++) {
                            JSONObject obj = myShows.getJSONObject(j);
                            int id = obj.getInt("id");
                            if (id == show.showId) {
                                append = ctx.getResources().getString(R.string.tab_show_myshow);
                                show.isSubscribed = true;
                                break;
                            } else {
                                append = "";
                            }
                        }


                        show.save();
                        shows.add(show);
                        asyncTaskListener.onTaskProgressUpdate(ctr, ASYNC_ID);
                        ctr++;
                    }
                    ActiveAndroid.setTransactionSuccessful();
                } finally {
                    ActiveAndroid.endTransaction();
                }

            } else {
                List<Show> _shows = new Select().from(Show.class).execute();
                shows = (ArrayList<Show>) _shows;
            }
            //cloneShowItems();
        } catch (JSONException e) {
            Log.e("err", e.getMessage());
        }

        return shows;
    }

    @Override
    protected void onPostExecute(ArrayList<Show> data) {
        asyncTaskListener.onTaskCompleted(data, ASYNC_ID);
    }
}
