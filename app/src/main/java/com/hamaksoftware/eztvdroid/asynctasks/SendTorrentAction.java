package com.hamaksoftware.eztvdroid.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.hamaksoftware.eztvdroid.fragments.IAsyncTaskListener;
import com.hamaksoftware.eztvdroid.models.Episode;
import com.hamaksoftware.eztvdroid.torrentcontroller.ClientType;
import com.hamaksoftware.eztvdroid.torrentcontroller.TorrentAction;
import com.hamaksoftware.eztvdroid.torrentcontroller.TransmissionHandler;
import com.hamaksoftware.eztvdroid.torrentcontroller.UtorrentHandler;
import com.hamaksoftware.eztvdroid.utils.AppPref;

public class SendTorrentAction extends AsyncTask<Void, Void, Boolean>{
    public static final String ASYNC_ID = "SENDTORRENTACTION";
    public String hashes;
    public TorrentAction action;
    private AppPref pref;
    public IAsyncTaskListener asyncTaskListener;
    private Context ctx;

    public SendTorrentAction(Context ctx, String hashes, TorrentAction action){
        this.hashes = hashes;
        this.action = action;
        this.ctx = ctx;
        if(pref == null) pref = new AppPref(ctx);
    }

    public SendTorrentAction(Context ctx){
        this.ctx = ctx;
        if(pref == null) pref = new AppPref(ctx);
    }

    @Override
    protected void onPreExecute(){
        asyncTaskListener.onTaskWorking(ASYNC_ID);
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        ClientType type = ClientType.valueOf(pref.getClientType());
        try{
            switch (type) {
                case UTORRENT:
                    UtorrentHandler uh = new UtorrentHandler(ctx);
                    uh.setOptions(pref.getIPAddress(),pref.getClientUsername(), pref.getClientPassword(),
                            pref.getClientPort(), pref.getAuth());
                    uh.sendAction(action,hashes);
                    return uh.lastStatusResult;
                case TRANSMISSION:
                    TransmissionHandler th = new TransmissionHandler(ctx);
                    th.setOptions(pref.getIPAddress(),pref.getClientUsername(), pref.getClientPassword(),
                            pref.getClientPort(), pref.getAuth());
                    th.sendAction(action,hashes);
                    return th.lastStatusResult;
            }
        }catch(Exception e){
            asyncTaskListener.onTaskError(e,ASYNC_ID);
        }

        return false;
    }
    @Override
    protected void onPostExecute(Boolean d) {
       asyncTaskListener.onTaskCompleted(d,ASYNC_ID);
    }
}
