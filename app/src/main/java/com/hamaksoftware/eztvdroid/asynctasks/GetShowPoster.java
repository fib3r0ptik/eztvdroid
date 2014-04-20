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
import com.hamaksoftware.eztvdroid.utils.ShowHandler;
import com.hamaksoftware.eztvdroid.utils.Utility;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class GetShowPoster extends AsyncTask<Void, Void, Drawable>{
    public static final String ASYNC_ID = "GETSHOWPOSTER";
    private EZTVShowItem show;
    private Context ctx;
    private ShowHandler sh;
    public IAsyncTaskListener asyncTaskListener;


    public GetShowPoster(Context ctx, EZTVShowItem show){
        this.show = show;
        sh = new ShowHandler(ctx);
        this.ctx  = ctx;
    }

    @Override
    protected void onPreExecute(){
        asyncTaskListener.onTaskWorking(ASYNC_ID);
    }

    @Override
    protected Drawable doInBackground(Void... voids) {
        try {
            URL url = new URL("http://hamaksoftware.com/myeztv/tvimg/"+show.showId+".jpg");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            if(bitmap == null) return null;

            Bitmap croppedBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getWidth());
            return new BitmapDrawable(ctx.getResources(), croppedBmp);
        } catch (IOException e) {
            return null;
        }
    }
    @Override
    protected void onPostExecute(Drawable d) {
       asyncTaskListener.onTaskCompleted(d,ASYNC_ID);
    }
}
