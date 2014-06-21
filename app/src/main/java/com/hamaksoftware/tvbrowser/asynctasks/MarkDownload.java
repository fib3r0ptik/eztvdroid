package com.hamaksoftware.tvbrowser.asynctasks;

import android.content.Context;
import android.os.AsyncTask;

import com.hamaksoftware.tvbrowser.fragments.IAsyncTaskListener;
import com.hamaksoftware.tvbrowser.utils.AppPref;
import com.hamaksoftware.tvbrowser.utils.Utility;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarkDownload extends AsyncTask<Void, Void, Void> {
    public static final String ASYNC_ID = "MARKDOWNLOAD";
    private int showId;
    private String title;
    private Context ctx;
    private AppPref pref;
    public IAsyncTaskListener asyncTaskListener;


    public MarkDownload(Context ctx, String title, int showId) {
        this.title = title;
        this.showId = showId;
        this.ctx = ctx;
        pref = new AppPref(ctx);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Pattern p = Pattern.compile("(.*?)S?(\\d{1,2})E?(\\d{2})(.*)", Pattern.DOTALL);
        Matcher matcher = p.matcher(title);
        String s = "";
        String e = "";
        String t = "";
        if (matcher.find()) {
            t = matcher.group(1);
            s = matcher.group(2);
            e = matcher.group(3);
        }


        ArrayList<NameValuePair> param = new ArrayList<NameValuePair>(6);
        param.add(new BasicNameValuePair("dev_id", pref.getDeviceId()));
        param.add(new BasicNameValuePair("show_id", showId + ""));
        param.add(new BasicNameValuePair("title", t));
        param.add(new BasicNameValuePair("s", s));
        param.add(new BasicNameValuePair("e", e));
        param.add(new BasicNameValuePair("method", "markDownload"));

        Utility.getInstance(ctx).doPostRequest(param);

        return null;
    }

}
