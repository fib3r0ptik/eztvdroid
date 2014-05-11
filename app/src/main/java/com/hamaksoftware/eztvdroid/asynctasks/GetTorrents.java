package com.hamaksoftware.eztvdroid.asynctasks;

import android.content.Context;
import android.os.AsyncTask;

import com.hamaksoftware.eztvdroid.fragments.IAsyncTaskListener;
import com.hamaksoftware.eztvdroid.models.Episode;
import com.hamaksoftware.eztvdroid.torrentcontroller.ClientType;
import com.hamaksoftware.eztvdroid.torrentcontroller.TorrentItem;
import com.hamaksoftware.eztvdroid.torrentcontroller.TransmissionHandler;
import com.hamaksoftware.eztvdroid.torrentcontroller.UtorrentHandler;
import com.hamaksoftware.eztvdroid.torrentcontroller.ViewFilter;
import com.hamaksoftware.eztvdroid.utils.AppPref;

import java.util.ArrayList;

public class GetTorrents extends AsyncTask<Void, Void, ArrayList<TorrentItem>>{
    public static final String ASYNC_ID = "GETTORRENTS";
    private Context ctx;
    private AppPref pref;
    public IAsyncTaskListener asyncTaskListener;
    private ViewFilter filter;

    public GetTorrents(Context ctx, ViewFilter filter){
        pref = new AppPref(ctx);
        this.ctx  = ctx;
        this.filter = filter;
    }

    @Override
    protected void onPreExecute(){
        asyncTaskListener.onTaskWorking(ASYNC_ID);
    }

    @Override
    protected ArrayList<TorrentItem> doInBackground(Void... voids) {
        ArrayList<Episode> items = new ArrayList<Episode>(0);
        ClientType type = ClientType.valueOf(pref.getClientType());
        try{
            switch (type) {
                case UTORRENT:
                    UtorrentHandler uh = new UtorrentHandler(ctx);
                    uh.setOptions(pref.getClientIPAddress(),pref.getClientUsername(), pref.getClientPassword(),
                            pref.getClientPort(), pref.getAuth());
                    uh.currentFilter = filter;
                    return uh.getTorrents();
                case TRANSMISSION:
                    TransmissionHandler th = new TransmissionHandler(ctx);
                    th.setOptions(pref.getClientIPAddress(),pref.getClientUsername(), pref.getClientPassword(),
                            pref.getClientPort(), pref.getAuth());
                    th.currentFilter = filter;
                    return th.getTorrents();
            }
        }catch(Exception e){
            asyncTaskListener.onTaskError(e,ASYNC_ID);
        }

        return null;
    }
    @Override
    protected void onPostExecute(ArrayList<TorrentItem> data) {
        asyncTaskListener.onTaskCompleted(data,ASYNC_ID);
    }

}
