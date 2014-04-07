package com.hamaksoftware.eztvdroid.utils;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.hamaksoftware.eztvdroid.R;
import com.hamaksoftware.eztvdroid.activities.Main;
import com.hamaksoftware.eztvdroid.torrentcontroller.ClientType;
import com.hamaksoftware.eztvdroid.torrentcontroller.*;

import com.hamaksoftware.eztvdroid.utils.*;


import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class GcmIntentService extends IntentService{
	public static final String TAG = "gcm";
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;
    AppPref pref;
    public enum QualityType{HD_ONLY,LOW_QUALITY_ONLY,BOTH_QUALITY};

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        pref = new AppPref(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                //sendNotification("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {
                //sendNotification("Deleted messages on server: " + extras.toString());
            // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
            	
            	
            	
            	String message = extras.getString("message");
            	Log.i("msg",message);
            	if(pref.getUseSubscription()){
                	try{
                		JSONObject jObject = new JSONObject(message);
                		JSONArray data = jObject.getJSONArray("data");
                		StringBuilder sb = new StringBuilder();
                		for(int i = 0; i < data.length();i++){
                			sb.append(data.getInt(i)).append(",");
                		}
                		
                		String ids = sb.toString().substring(0,sb.toString().length()-1);
                		Log.i("ids",ids);
    	                ArrayList<NameValuePair> param = new ArrayList<NameValuePair>(3);
    	                param.add(new BasicNameValuePair("show_ids", ids));
    	                param.add(new BasicNameValuePair("dev_id",pref.getDeviceId()));
    	                param.add(new BasicNameValuePair("method", "showInfo"));
    	        		String response = Utility.getInstance(getApplicationContext()).doPostRequest(param);
    	        		if(!response.equals("[]")) sendNotification(response);
                	}catch(JSONException e){
                		//e.printStackTrace();
                	}
            	}

            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg) {
    	//Log.i("msg",msg);
    	boolean isValidJSON = false;
    	PendingIntent contentIntent = null;
        Intent home = new Intent(this, Main.class);
        //String[] links = null; 
        ArrayList<String> links = new ArrayList<String>(0);
    	StringBuilder sb = new StringBuilder();
    	try{
	    	JSONArray responseArr = new JSONArray(msg);
	    	StringBuilder ids = new StringBuilder();
	    	for(int i=0;i<responseArr.length();i++){
	    		JSONObject item = responseArr.getJSONObject(i);
	    		ids.append(item.getString("id")).append(",");
	    		
	    		String link = item.getString("latest_link");
	    		String hdlink = item.getString("latest_hdlink");
	    		
	    		switch (QualityType.values()[pref.getAutoSendQuality()]) {
				case HD_ONLY:
					if(!hdlink.equals("null")) links.add(hdlink);
					break;
				case LOW_QUALITY_ONLY:
					links.add(link);
					break;	
				default:
					links.add(link);
					if(!hdlink.equals("null")) links.add(hdlink);
					break;
				}

	    		if(i < 5) {
	    			sb.append(item.getString("title")).append(" - ")
		    		.append(item.getString("season"))
		    		.append(item.getString("episode")).append("\n");
	    		}

	    	}
	    	
	    	//Toast.makeText(getApplicationContext(), links.toString(), Toast.LENGTH_LONG).show();
	    	
	    	//Log.i("payload", msg);
	    	
	    	isValidJSON = true;
	    	Bundle payload = new Bundle();
	    	ProfileHandler phandler = new ProfileHandler(getApplicationContext());
	    	final ArrayList<ClientProfile> profiles = phandler.getAllProfiles();
	    	if(pref.getAutoSend() && profiles.size() > 0){
	    		String[] slinks = new String[links.size()];
	    		for(int j=0;j < links.size();j++){
	    			slinks[j] = links.get(j);
	    		}
	    		
	    		new AsyncTask<String, Void, Boolean>() {
	    			
					@Override
					protected Boolean doInBackground(String... links) {
						boolean success = false;
						ClientProfile p = profiles.get(0);
						ClientType type = ClientType.values()[p.clientType];
						for(int i=0; i < links.length;i++){
							switch(type){
							case UTORRENT:
								UtorrentHandler uh=new UtorrentHandler(getApplicationContext());
								uh.setOptions(p.host,p.username,p.password,p.port,p.useAuth);
								
								try{
									uh.addTorrent(links[i]);
									success = uh.lastStatusResult;
								}catch(Exception e){
									success = false;
								}
								break;
							case TRANSMISSION:
								TransmissionHandler th=new TransmissionHandler(getApplicationContext());
								th.setOptions(p.host,p.username,p.password,p.port,p.useAuth);
								try{
									th.addTorrent(links[i]);
									success = th.lastStatusResult;
								}catch(Exception e){
									success = false;
								}
								break;
							}
							
						}

						return success;
					}
		    			
				}.execute(slinks);

				payload.putString("filter", "Y");
	    		payload.putString("ids",ids.toString().substring(0,ids.toString().length()-1));
		    	home.putExtras(payload);
	    	}else{
	    		payload.putString("filter", "Y");
	    		payload.putString("ids",ids.toString().substring(0,ids.toString().length()-1));
	    		home.putExtras(payload);
	    	}

	    	
	    	
	    	
    	}catch(JSONException e){
    		//Log.i("err", e.getMessage());
    		isValidJSON = false;
    	}
    	
    	contentIntent = PendingIntent.getActivity(this, 0,home, PendingIntent.FLAG_CANCEL_CURRENT);
    	
    	if(isValidJSON){
    		Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
	        mNotificationManager = (NotificationManager)
	                this.getSystemService(Context.NOTIFICATION_SERVICE);
	
	        NotificationCompat.Builder mBuilder =
	                new NotificationCompat.Builder(this);
	        mBuilder.setSmallIcon(R.drawable.notification);
	        
	        if(sound !=null) mBuilder.setSound(sound);
	        
	        String more = links.size() - 5 <= 0 ?"":(links.size() - 5)+"";
	        
	        mBuilder.setAutoCancel(true)
	        .setContentTitle("New episodes found.")
	        .setStyle(new NotificationCompat.BigTextStyle()
	        .bigText(sb.toString()))
	        .setContentInfo(more)
			.setSmallIcon(R.drawable.notification)
			.setContentText(sb.toString())
	        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.notification));

	        
	        try{
	        	mBuilder.setContentIntent(contentIntent);
	        	mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	        }catch(Exception e){
	        	e.printStackTrace();
	        }
	        
    	}
    }

}
