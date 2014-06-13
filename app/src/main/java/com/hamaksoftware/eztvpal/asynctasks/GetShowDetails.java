package com.hamaksoftware.eztvpal.asynctasks;

import android.content.Context;
import android.os.AsyncTask;

import com.hamaksoftware.eztvpal.fragments.IAsyncTaskListener;
import com.hamaksoftware.eztvpal.models.Show;
import com.hamaksoftware.eztvpal.utils.Utility;

public class GetShowDetails extends AsyncTask<Void, Void, String>{
    public static final String ASYNC_ID = "GETSHOWDETAILS";
    private Show show;
    private Context ctx;
    //private ShowHandler sh;
    public IAsyncTaskListener asyncTaskListener;


    public GetShowDetails(Context ctx, Show show){
        this.show = show;
        //sh = new ShowHandler(ctx);
        this.ctx  = ctx;
    }

    @Override
    protected void onPreExecute(){
        asyncTaskListener.onTaskWorking(ASYNC_ID);
    }

    @Override
    protected String doInBackground(Void... voids) {
        return Utility.getInstance(ctx).getShowDetails(show.title);
    }

    @Override
    protected void onPostExecute(String response) {
       asyncTaskListener.onTaskCompleted(response,ASYNC_ID);
    }
}
