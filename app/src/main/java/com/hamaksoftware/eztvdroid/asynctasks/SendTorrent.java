package com.hamaksoftware.eztvdroid.asynctasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;

import com.hamaksoftware.eztvdroid.fragments.IAsyncTaskListener;
import com.hamaksoftware.eztvdroid.models.EZTVRow;
import com.hamaksoftware.eztvdroid.models.EZTVShowItem;
import com.hamaksoftware.eztvdroid.torrentcontroller.UtorrentHandler;
import com.hamaksoftware.eztvdroid.utils.AppPref;
import com.hamaksoftware.eztvdroid.utils.ShowHandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SendTorrent extends AsyncTask<Void, Void, Boolean>{
    public static final String ASYNC_ID = "SENDTORRENT";
    private EZTVRow item;
    private Context ctx;
    private AppPref pref;
    public IAsyncTaskListener asyncTaskListener;


    public SendTorrent(Context ctx, EZTVRow item){
        this.item = item;
        this.ctx  = ctx;
        pref = new AppPref(ctx);
    }

    @Override
    protected void onPreExecute(){
        asyncTaskListener.onTaskWorking(ASYNC_ID);
    }

    @Override
    protected Boolean doInBackground(Void... voids) {

        if(pref.getClientType().equals("UTORRENT")){
            UtorrentHandler uh=new UtorrentHandler(ctx);
            uh.setOptions(pref.getIPAddress(),pref.getClientUsername(),pref.getClientPassword(),pref.getClientPort(),pref.getAuth());
            Log.i("link", item.links.get(0));
            try{
                uh.addTorrent(item.links.get(0));
                return uh.lastStatusResult;
            }catch(Exception e){
                return false;
            }
        }

        return false;
    }
    @Override
    protected void onPostExecute(Boolean d) {
       asyncTaskListener.onTaskCompleted(d,ASYNC_ID);
    }
}
