package com.hamaksoftware.eztvdroid.asynctasks;

import android.content.Context;
import android.os.AsyncTask;

import com.hamaksoftware.eztvdroid.fragments.IAsyncTaskListener;
import com.hamaksoftware.eztvdroid.models.Episode;
import com.hamaksoftware.eztvdroid.torrentcontroller.ClientType;
import com.hamaksoftware.eztvdroid.torrentcontroller.TransmissionHandler;
import com.hamaksoftware.eztvdroid.torrentcontroller.UtorrentHandler;
import com.hamaksoftware.eztvdroid.utils.AppPref;

public class VerifyClient extends AsyncTask<Void, Void, Boolean>{
    public static final String ASYNC_ID = "VERIFYCLIENT";
    private Episode item;
    private Context ctx;
    private AppPref pref;
    public IAsyncTaskListener asyncTaskListener;


    public VerifyClient(Context ctx){
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
        ClientType type = ClientType.valueOf(pref.getClientType());
        try{
            switch (type) {
                case UTORRENT:
                    UtorrentHandler uh = new UtorrentHandler(ctx);
                    uh.setOptions(pref.getClientIPAddress(),pref.getClientUsername(), pref.getClientPassword(),
                            pref.getClientPort(), pref.getAuth());
                    uh.getToken();
                    return uh.token.length() > 0;
                case TRANSMISSION:
                    TransmissionHandler th = new TransmissionHandler(ctx);
                    th.setOptions(pref.getClientIPAddress(),pref.getClientUsername(), pref.getClientPassword(),
                            pref.getClientPort(), pref.getAuth());
                    return th.getCode().length()>0;
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
