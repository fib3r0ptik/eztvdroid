package com.hamaksoftware.eztvdroid.adapters;

import android.content.Context;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hamaksoftware.eztvdroid.R;
import com.hamaksoftware.eztvdroid.torrentcontroller.ClientType;
import com.hamaksoftware.eztvdroid.torrentcontroller.TorrentItem;
import com.hamaksoftware.eztvdroid.utils.AppPref;
import com.hamaksoftware.eztvdroid.utils.Utility;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class DownloadAdapter extends BaseAdapter{
    private Context ctx;
    private AppPref pref;

    private static final int STATUS_STARTED = 1;
    private static final int STATUS_CHECKING = 2;
    private static final int STATUS_STARTAFTERCHECK = 4;
    private static final int STATUS_CHECKED = 8;
    private static final int STATUS_ERROR = 16;
    private static final int STATUS_PAUSED = 32;
    private static final int STATUS_QUEUED = 64;
    private static final int STATUS_LOADED = 128;

    private LayoutInflater inflater;
    public ArrayList<TorrentItem> items;

    final long KILOBYTE = 1024L;
    final long MEGABYTE = 1024L * 1024L;
    final long GIGABYTE = 1024L * 1024L * 1024L;
    final int PERCENT_COMPLETED = 1000;

    private ClientType type;

    public DownloadAdapter(Context ctx) {
        this.ctx = ctx;
        pref = new AppPref(ctx);
        this.items = new ArrayList<TorrentItem>(0);
        inflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        type = ClientType.valueOf(pref.getClientType());
        //type = ClientType.values()[Integer.parseInt(pref.getClientType())];
    }

    public int getTotalUploadSpeed(){
        int ttl = 0;
        for(TorrentItem item: items){
            ttl+=item.getUploadSpeed();
        }

        return ttl;
    }

    public int getTotalDownloadSpeed(){
        int ttl = 0;
        for(TorrentItem item: items){
            ttl+=item.getDownloadSpeed();
        }
        return ttl;
    }

    public String getCompletedHashes(){
        try{
            StringBuilder sb = new StringBuilder();
            if(type==ClientType.UTORRENT) sb.append("&");
            for(TorrentItem item: items){
                if(item.getPercent() >= 1000) {
                    switch (type) {
                        case UTORRENT:
                            sb.append("hash=").append(item.getHash())
                                    .append("&");
                            break;
                        case TRANSMISSION:
                            sb.append("\"" + item.getHash()).append("\",");
                            break;
                    }
                }

            }

            if(items.size()>0){
                return sb.toString().substring(0, sb.toString().length() - 1);
            }else{
                return "";
            }

        }catch(Exception e){
            return "";
        }
    }

    public String getHashes(){
        try{
            StringBuilder sb = new StringBuilder();
            if(type==ClientType.UTORRENT) sb.append("&");
            for(TorrentItem item: items){
                switch (type) {
                    case UTORRENT:
                        sb.append("hash=").append(item.getHash())
                                .append("&");
                        break;
                    case TRANSMISSION:
                        sb.append("\""+item.getHash()).append("\",");
                        break;
                }

            }

            if(items.size()>0){
                return sb.toString().substring(0, sb.toString().length() - 1);
            }else{
                return "";
            }

        }catch(Exception e){
            return "";
        }
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int i) {
        return items.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    class ViewHolder{
        TextView title;
        LinearLayout barHolder;
        View txtPercent;
        TextView eta;
        TextView tvPeers;
        TextView tvConnection;
        TextView tvDownloaded;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {

        try {
            WindowManager windowManager = (WindowManager)ctx
                    .getSystemService(Context.WINDOW_SERVICE);

            Display display = windowManager.getDefaultDisplay();


            int sw = display.getWidth();
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.download_row, null);
                holder = new ViewHolder();
                holder.title = (TextView) convertView.findViewById(R.id.downloads_title);
                holder.barHolder = (LinearLayout) convertView.findViewById(R.id.download_bar);
                holder.txtPercent = (View) convertView.findViewById(R.id.download_percent);
                holder.eta = (TextView)convertView.findViewById(R.id.downloads_eta);
                holder.tvDownloaded = (TextView) convertView.findViewById(R.id.downloads_downloaded);
                holder.tvPeers = (TextView) convertView.findViewById(R.id.download_peers);
                holder.tvConnection = (TextView) convertView.findViewById(R.id.download_connection);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder)convertView.getTag();
            }

            TorrentItem entry = items.get(position);
            String title = entry.getName();

            holder.title.setText(title);

            double size = (double) entry.getSize();
            if(size < 0) size *= -1.0;
            double downloaded = (((double) entry.getPercent() / 10) / 100) * (double) size;
            if(downloaded < 0) downloaded *= -1.0;

            DecimalFormat Currency = new DecimalFormat("#0.00");
            String sd = "";
            if ((downloaded / MEGABYTE) > 1000) {
                sd = Currency.format((double) (downloaded / GIGABYTE))
                        + " GB";
            } else {
                sd = Currency.format((double) (downloaded / MEGABYTE))
                        + " MB";
            }

            String ssize = "";
            if ((size / MEGABYTE) > 1000) {
                ssize = Currency.format((double) (size / GIGABYTE)) + " GB";
            } else {
                ssize = Currency.format((double) (size / MEGABYTE)) + " MB";
            }

            double fperc = (downloaded / size) * 100;
            String perc = Integer.toString((int) fperc);
            String finaltxt = "";

            double percentage_remaining = entry.getPercent() / 10;

            String converted = Utility.convertSecs(entry.getETA());
            String sETA = "";



            if (type== ClientType.UTORRENT) {
                if (entry.getPercent() >= PERCENT_COMPLETED){
                    holder.barHolder.setBackgroundResource(R.drawable.shape_completed);
                    holder.txtPercent.setBackgroundResource(R.drawable.shape_completed);

                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT);
                    holder.txtPercent.setLayoutParams(layoutParams);

                    holder.eta.setText(ctx.getResources().getString(R.string.download_completed));
                }else{
                    double finalW = (((double) percentage_remaining / 100) * (double) (sw - 10));
                    holder.txtPercent.setBackgroundResource(R.drawable.shape_started);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                            (int) finalW,
                            LinearLayout.LayoutParams.MATCH_PARENT);
                    holder.txtPercent.setLayoutParams(layoutParams);
                    holder.barHolder.setBackgroundResource(R.drawable.shape_queued);

                    if(Utility.hasStatus(STATUS_STARTED, entry.getStatus())){
                        holder.eta.setText(Utility.convertSecs(entry.getETA()));
                        holder.txtPercent.setBackgroundResource(R.drawable.shape_started);
                    }else{
                        System.out.println(entry.getStatus());

                        if(entry.getStatus() == STATUS_CHECKED + STATUS_LOADED){
                            holder.eta.setText("Stopped");
                            holder.txtPercent.setBackgroundResource(R.drawable.shape_stopped);
                        }else if(entry.getStatus() == STATUS_CHECKED + STATUS_LOADED + STATUS_QUEUED) {
                            holder.eta.setText("Queued");
                            holder.txtPercent.setBackgroundResource(R.drawable.shape_queued);
                        }else if(entry.getStatus() == STATUS_CHECKED + STATUS_LOADED + STATUS_QUEUED + STATUS_PAUSED){
                            holder.eta.setText("Paused");
                            holder.txtPercent.setBackgroundResource(R.drawable.shape_paused);
                        }else if(entry.getStatus() == STATUS_ERROR){
                            holder.eta.setText("Error");
                            holder.txtPercent.setBackgroundResource(R.drawable.shape_error);
                        }
                        /*
                        if(Utility.hasStatus(STATUS_PAUSED,entry.getStatus())){
                            holder.eta.setText("Paused");
                            holder.txtPercent.setBackgroundResource(R.drawable.shape_paused);
                        }else if(Utility.hasStatus(STATUS_ERROR,entry.getStatus())){
                            holder.eta.setText("Error");
                            holder.txtPercent.setBackgroundResource(R.drawable.shape_paused);
                        }else if(Utility.hasStatus(STATUS_QUEUED,entry.getStatus())){
                            holder.eta.setText("Queued");
                            holder.txtPercent.setBackgroundResource(R.drawable.shape_queued);
                        }else if(STATUS_LOADED + STATUS_CHECKED == entry.getStatus()){
                            holder.eta.setText("Stopped");
                            holder.txtPercent.setBackgroundResource(R.drawable.shape_paused);
                        }
                        */


                        /*
                        switch (entry.getStatus()) {
                            case STATUS_CHECKED + STATUS_LOADED + STATUS_PAUSED + STATUS_QUEUED:
                                holder.eta.setText("Paused");
                                break;
                            case STATUS_CHECKED + STATUS_LOADED + STATUS_QUEUED:
                                holder.eta.setText("Queued");
                                holder.txtPercent.setBackgroundResource(R.drawable.shape_queued);

                                break;
                            case STATUS_ERROR:
                                holder.eta.setText("Error");
                                holder.txtPercent.setBackgroundResource(R.drawable.shape_paused);
                                break;
                            case STATUS_LOADED + STATUS_CHECKED:
                                holder.eta.setText("Stopped");
                                holder.txtPercent.setBackgroundResource(R.drawable.shape_queued);
                                break;
                            default:
                                holder.txtPercent.setBackgroundResource(R.drawable.shape_queued);
                                break;
                        }*/
                    }
                }


            }


            if (type==ClientType.TRANSMISSION) {
                switch (entry.getStatus()) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                        holder.barHolder.setBackgroundResource(R.drawable.shape_queued);
                        break;
                    case 4:
                        double finalW = (((double) percentage_remaining / 100) * (double) (sw - 10));
                        holder.txtPercent.setBackgroundResource(R.drawable.shape_started);
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                (int) finalW,
                                LinearLayout.LayoutParams.MATCH_PARENT);
                        holder.txtPercent.setLayoutParams(layoutParams);
                        holder.barHolder.setBackgroundResource(R.drawable.shape_queued);
                        break;
                    case 5:
                    case 6:
                        holder.barHolder.setBackgroundResource(R.drawable.shape_completed);
                        holder.txtPercent.setBackgroundResource(R.drawable.shape_completed);
                        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT);
                        holder.txtPercent.setLayoutParams(layoutParams2);
                        break;
                }

                if(entry.getStatus() < 5){
                    sETA = converted.equals("")? "N/A":converted;
                }else{
                    sETA = converted.equals("")? "Completed":converted;
                }
                holder.eta.setText(sETA);
            }

            holder.tvDownloaded.setText(sd + "/" + ssize + " - " + perc + "%");


            String ds = "";
            if ((entry.getDownloadSpeed() / KILOBYTE) > 1000) {
                ds = Currency.format((double) ((double) entry
                        .getDownloadSpeed() / MEGABYTE)) + " MB";
            } else {
                ds = Currency.format((double) ((double) entry
                        .getDownloadSpeed() / KILOBYTE)) + " KB";
            }

            String us = "";
            if ((entry.getUploadSpeed() / KILOBYTE) > 1000) {
                us = Currency.format((double) ((double) entry
                        .getUploadSpeed() / MEGABYTE)) + " MB";
            } else {
                us = Currency.format((double) ((double) entry
                        .getUploadSpeed() / KILOBYTE)) + " KB";
            }

            if (type==ClientType.UTORRENT) {
                finaltxt = entry.getSeedersCon() + "("
                        + entry.getSeedersAll() + ") Seeders, "
                        + entry.getPeersCon() + "(" + entry.getPeersAll()
                        + ") Peers.";
            } else if (type==ClientType.TRANSMISSION) {
                finaltxt = entry.getPeersCon() + "(" + entry.getPeersAll() + ") Peers.";
            }


            holder.tvPeers.setText(finaltxt);
            holder.tvConnection.setText(ctx.getResources().getString(R.string.arrow_down) +ds+"/s "
                    + ctx.getResources().getString(R.string.arrow_up)+ us+"/s");


        } catch (Exception e) {
            e.printStackTrace();
        }

        return convertView;

    }
}
