package com.hamaksoftware.tvbrowser.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.hamaksoftware.tvbrowser.utils.AppPref;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import info.besiera.api.APIRequest;
import info.besiera.api.APIRequestException;

public class MarkDownload extends AsyncTask<Void, Void, Void> {
    public static final String ASYNC_ID = "MARKDOWNLOAD";
    private int showId;
    private String title;
    private Context ctx;
    private AppPref pref;
    private int season;
    private int episode;


    public MarkDownload(Context ctx, String title, int showId) {
        this.title = title;
        this.showId = showId;
        this.ctx = ctx;
        pref = new AppPref(ctx);
    }

    public MarkDownload(Context ctx, int season, int episode, int showId) {
        this.season = season;
        this.episode = episode;
        this.showId = showId;
        this.ctx = ctx;
        pref = new AppPref(ctx);
    }


    @Override
    protected Void doInBackground(Void... voids) {
        if (title == null) {
            try {
                APIRequest apiRequest = new APIRequest();
                apiRequest.markDownload(pref.getDeviceId(), showId, season, episode);
            } catch (APIRequestException ex) {
                Log.e("api", "API:Markdownload" + ex.getStatus().toString());
            } catch (Exception exx) {
                Log.e("markdownload", exx.getMessage());
            }
        } else {
            Pattern p = Pattern.compile("(.*?)S?(\\d{1,2})E?(\\d{2})(.*)", Pattern.DOTALL);
            Matcher matcher = p.matcher(title);
            String s = "";
            String e = "";
            //String t = "";
            if (matcher.find()) {
                //t = matcher.group(1);
                s = matcher.group(2);
                e = matcher.group(3);
            }

            try {
                APIRequest apiRequest = new APIRequest();
                apiRequest.markDownload(pref.getDeviceId(), showId, Integer.parseInt(s), Integer.parseInt(e));
            } catch (APIRequestException ex) {
                Log.e("api", "API:Markdownload" + ex.getStatus().toString());
            } catch (Exception exx) {
                Log.e("markdownload", exx.getMessage());
            }
        }
        return null;
    }

}
