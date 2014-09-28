package com.hamaksoftware.tvbrowser.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hamaksoftware.tvbrowser.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.pollexor.Thumbor;

import java.util.List;
import info.besiera.api.models.Subscription;

public class MyShowAdapter extends BaseAdapter {
    public Context ctx;
    public List<Subscription> subscriptions;
    public List<Subscription> unseen;


    private class ViewHolder {
        ImageView img;
        TextView txt;
        TextView title;
    }

    private boolean isUnseen(int showId){
        if(unseen == null) return false;
        for(Subscription s: unseen){
            if(s.getShow().getShowId() == showId){
                return true;
            }
        }
        return false;
    }
    public MyShowAdapter(Context context) {
        this.ctx = context;
    }

    public void setSubscriptions(List<Subscription> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public int getCount() {
        return subscriptions.size();
    }

    public Object getItem(int position) {
        return subscriptions.get(position);
    }

    public long getItemId(int position) {
        return position;
    }


    public View getView(int position, View convertView, ViewGroup viewGroup) {

        ViewHolder holder = null;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) ctx
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.my_show_row, null);
            holder = new ViewHolder();
            holder.img = (ImageView) convertView.findViewById(R.id.imgGrid);
            holder.txt = (TextView) convertView.findViewById(R.id.notification);
            holder.title = (TextView) convertView.findViewById(R.id.myshow_title);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Subscription subscription = subscriptions.get(position);
        Thumbor thumbor = Thumbor.create("http://besiera.info:8888/");
        String url = thumbor.buildImage("http://besiera.info/apibackend/tvimg/" + subscription.getShow().getShowId() + ".jpg")
                .resize(250,250).smart().toUrl();
        holder.title.setText(subscription.getShow().getTitle());
        ImageLoader.getInstance().displayImage(url, holder.img);
        holder.txt.setVisibility(isUnseen(subscription.getShow().getShowId())?View.VISIBLE:View.GONE);
        return convertView;
    }
}
