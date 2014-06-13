package com.hamaksoftware.eztvpal.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hamaksoftware.eztvpal.R;
import com.hamaksoftware.eztvpal.models.RSSItem;
import com.hamaksoftware.eztvpal.utils.Utility;

import java.util.ArrayList;

public class RSSAdapter extends BaseAdapter{
    public Context ctx;
    public ArrayList<RSSItem> items;


    private class ViewHolder{
        LinearLayout holder;
        TextView title;
        TextView info;
    }

    public RSSAdapter(Context ctx) {
        this.ctx = ctx;
        items = new ArrayList<RSSItem>(0);
    }


    @Override
    public int getCount() {
        return items.size();
    }

    public Object getItem(int position) {
        return items.get(position);
    }

    public long getItemId(int position) {
        return position;
    }



    public View getView(int position, View convertView, ViewGroup viewGroup) {

        ViewHolder holder = null;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater)ctx
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.feed_item, null);
            holder = new ViewHolder();
            holder.holder = (LinearLayout)convertView.findViewById(R.id.feed_row_holder);
            holder.title = (TextView)convertView.findViewById(R.id.feed_title);
            holder.info = (TextView)convertView.findViewById(R.id.feed_ext_info);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder)convertView.getTag();
        }


        final RSSItem entry = items.get(position);

        if(position % 2 == 0){
            holder.holder.setBackgroundColor(Color.WHITE);
        }else{
            holder.holder.setBackgroundResource(R.color.alt_blue);
        }

        holder.title.setText(entry.title);
        holder.info.setText(Utility.getFancySize(entry.filesize) + " " + Utility.getElapsed(entry.pubdate));

        return convertView;
    }
}
