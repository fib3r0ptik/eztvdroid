package com.hamaksoftware.tvbrowser.asynctasks;

import android.content.Context;
import android.os.AsyncTask;

import com.hamaksoftware.tvbrowser.fragments.IAsyncTaskListener;
import com.hamaksoftware.tvbrowser.torrentcontroller.ClientType;
import com.hamaksoftware.tvbrowser.torrentcontroller.TransmissionHandler;
import com.hamaksoftware.tvbrowser.torrentcontroller.UtorrentHandler;
import com.hamaksoftware.tvbrowser.utils.AppPref;

import info.besiera.api.models.Episode;

public class SendTorrent extends AsyncTask<Void, Void, Boolean> {
    public static final String ASYNC_ID = "SENDTORRENT";
    private Episode item;
    private Context ctx;
    private AppPref pref;
    public IAsyncTaskListener asyncTaskListener;


    public SendTorrent(Context ctx, Episode item) {
        this.item = item;
        this.ctx = ctx;
        pref = new AppPref(ctx);
    }

    @Override
    protected void onPreExecute() {
        asyncTaskListener.onTaskWorking(ASYNC_ID);
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        ClientType type = ClientType.valueOf(pref.getClientType());
        try {
            switch (type) {
                case UTORRENT:
                    UtorrentHandler uh = new UtorrentHandler(ctx);
                    uh.setOptions(pref.getClientIPAddress(), pref.getClientUsername(), pref.getClientPassword(),
                            pref.getClientPort(), pref.getAuth());
                    uh.addTorrent(item.getLinks().get(0));
                    return uh.lastStatusResult;
                case TRANSMISSION:
                    TransmissionHandler th = new TransmissionHandler(ctx);
                    th.setOptions(pref.getClientIPAddress(), pref.getClientUsername(), pref.getClientPassword(),
                            pref.getClientPort(), pref.getAuth());
                    th.addTorrent(item.getLinks().get(0));
                    return th.lastStatusResult;
            }
        } catch (Exception e) {
            asyncTaskListener.onTaskError(e, ASYNC_ID);
        }

        return false;
    }

    @Override
    protected void onPostExecute(Boolean d) {
        asyncTaskListener.onTaskCompleted(d, ASYNC_ID);
    }
}
