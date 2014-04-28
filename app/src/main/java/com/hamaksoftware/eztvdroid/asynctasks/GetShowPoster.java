package com.hamaksoftware.eztvdroid.asynctasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import com.hamaksoftware.eztvdroid.fragments.IAsyncTaskListener;
import com.hamaksoftware.eztvdroid.models.Show;
import com.hamaksoftware.eztvdroid.utils.ShowHandler;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class GetShowPoster extends AsyncTask<Void, Void, Drawable>{
    public static final String ASYNC_ID = "GETSHOWPOSTER";
    private Show show;
    private Context ctx;
    private ShowHandler sh;
    public IAsyncTaskListener asyncTaskListener;


    public GetShowPoster(Context ctx, Show show){
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
        } catch (Exception e) {
            return null;
        }
    }
    @Override
    protected void onPostExecute(Drawable d) {
       asyncTaskListener.onTaskCompleted(d,ASYNC_ID);
    }
}
