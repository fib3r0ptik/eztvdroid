package com.hamaksoftware.tvbrowser.gcm;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;
import com.hamaksoftware.tvbrowser.R;
import com.hamaksoftware.tvbrowser.activities.Main;
import com.hamaksoftware.tvbrowser.asynctasks.MarkDownload;
import com.hamaksoftware.tvbrowser.asynctasks.SendTorrent;
import com.hamaksoftware.tvbrowser.fragments.IAsyncTaskListener;
import com.hamaksoftware.tvbrowser.utils.AppPref;

import java.util.ArrayList;
import java.util.List;

import info.besiera.api.APIRequest;
import info.besiera.api.APIRequestException;
import info.besiera.api.models.Episode;
import info.besiera.api.models.Subscription;

public class GcmIntentService extends IntentService implements IAsyncTaskListener {
    public static final String TAG = "gcm";
    public static final int NOTIFICATION_ID = 1;
    NotificationCompat.Builder builder;
    AppPref pref;
    private NotificationManager mNotificationManager;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    ;

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
                Log.i("msg", message);
                if (pref.getUseSubscription()) {
                    GCMServerMessage template = new Gson().fromJson(message, GCMServerMessage.class);
                    List<String> showIds = template.getData();
                    APIRequest apiRequest = new APIRequest();
                    ArrayList<Subscription> filtered = new ArrayList<Subscription>(0);
                    List<Subscription> _newShow = new ArrayList<Subscription>(0);
                    try {
                        List<Subscription> myShows = apiRequest.getMyShows(pref.getDeviceId());
                        for (Subscription s : myShows) {
                            if (showIds.contains(s.getShow().getShowId() + "")) {
                                filtered.add(s);
                            }
                        }

                        if (filtered.size() > 0) {
                            List<Subscription> newShows = apiRequest.getUnseenSubscription(pref.getDeviceId());
                            for (Subscription _s : newShows) {
                                for (Subscription f : filtered) {
                                    if (_s.getShow().getShowId() == f.getShow().getShowId()) {
                                        _newShow.add(_s);
                                    }
                                }
                            }
                        }

                    } catch (APIRequestException e) {
                        Log.e("api", e.getStatus().toString());
                    }
                    if (_newShow.size() > 0) {
                        sendNotification(_newShow);
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
    private void sendNotification(List<Subscription> subscriptions) {
        PendingIntent contentIntent = null;
        Intent home = new Intent(this, Main.class);
        StringBuilder sb = new StringBuilder();
        ArrayList<String> links = new ArrayList<String>(0);
        int i = 0;
        for (Subscription subscription : subscriptions) {
            String link = null;
            switch (QualityType.values()[pref.getAutoSendQuality()]) {
                case HD_ONLY:
                    if (subscription.getShow().getHdlink() != null) {
                        link = subscription.getShow().getHdlink();
                    }
                    break;
                case HD_FIRST:
                    if (subscription.getShow().getHdlink() != null) {
                        link = subscription.getShow().getHdlink();
                    } else if (subscription.getShow().getLink() != null) {
                        link = subscription.getShow().getLink();
                    }
                    break;
                case LOW_QUALITY_ONLY:
                    if (subscription.getShow().getLink() != null) {
                        link = subscription.getShow().getLink();
                    }
                    break;
            }

            if (pref.getAutoSend() && pref.getClientIPAddress().length() > 3 && link != null) {
                Episode ep = new Episode();
                ArrayList<String> _links = new ArrayList<String>(0);
                _links.add(link.substring(0, 2).equalsIgnoreCase("//") ? "http:" + link : link);
                ep.setLinks(_links);
                SendTorrent sendTorrent = new SendTorrent(getApplicationContext(), ep);
                sendTorrent.asyncTaskListener = this;
                sendTorrent.execute();

                MarkDownload markDownload = new MarkDownload(getApplicationContext(), subscription.getShow().getSeason(),
                        subscription.getShow().getEpisode(), subscription.getShow().getShowId());
                markDownload.execute();

            }


            if (i < 5) {
                sb.append(subscription.getShow().getTitle()).append(" - Season: ")
                        .append(subscription.getShow().getSeason()).append(" Episode: ")
                        .append(subscription.getShow().getEpisode()).append("\n");
            }
            i++;
        }

        contentIntent = PendingIntent.getActivity(this, 0, home, PendingIntent.FLAG_CANCEL_CURRENT);


        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(R.drawable.notification);

        if (sound != null) mBuilder.setSound(sound);

        String more = links.size() - 5 <= 0 ? "" : (links.size() - 5) + "";

        mBuilder.setAutoCancel(true)
                .setContentTitle("New episodes found.")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(sb.toString()))
                .setContentInfo(more)
                .setSmallIcon(R.drawable.notification)
                .setContentText(sb.toString())
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.notification));


        try {
            mBuilder.setContentIntent(contentIntent);
            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        } catch (Exception e) {
            Log.e("notification:", e.getMessage());
        }

    }

    @Override
    public void onTaskCompleted(Object data, String ASYNC_ID) {
    }

    @Override
    public void onTaskWorking(String ASYNC_ID) {

    }

    @Override
    public void onTaskProgressUpdate(int progress, String ASYNC_ID) {

    }

    @Override
    public void onTaskProgressMax(int max, String ASYNC_ID) {

    }

    @Override
    public void onTaskUpdateMessage(String message, String ASYNC_ID) {

    }

    @Override
    public void onTaskError(Exception e, String ASYNC_ID) {

    }

    public enum QualityType {HD_ONLY, HD_FIRST, LOW_QUALITY_ONLY}

}
