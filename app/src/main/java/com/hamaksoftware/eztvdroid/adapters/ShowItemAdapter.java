package com.hamaksoftware.eztvdroid.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hamaksoftware.eztvdroid.R;
import com.hamaksoftware.eztvdroid.models.EZTVShowItem;

import java.util.List;

public class ShowItemAdapter extends BaseAdapter{
    public Context context;
    public List<EZTVShowItem> listings;

    private class ViewHolder{
        LinearLayout rowHolder;
        TextView title;
        CheckBox chk;

    }

    public ShowItemAdapter(Context context, List<EZTVShowItem> listing) {
        this.context = context;
        this.listings = listing;
    }


    public int getCount() {
        return listings.size();
    }

    public Object getItem(int position) {
        return listings.get(position);
    }

    public long getItemId(int position) {
        return position;
    }



    public View getView(int position, View convertView, ViewGroup viewGroup) {

        ViewHolder holder = null;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.shows_row, null);
            holder = new ViewHolder();
            holder.rowHolder = (LinearLayout)convertView.findViewById(R.id.show_row_holder);
            holder.title = (TextView) convertView.findViewById(R.id.show_title);
            holder.chk = (CheckBox) convertView.findViewById(R.id.shows_chk_item);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder)convertView.getTag();
        }


        final EZTVShowItem entry = listings.get(position);

        if(position % 2 == 0){
            holder.rowHolder.setBackgroundColor(Color.WHITE);
        }else{
            holder.rowHolder.setBackgroundResource(R.color.alt_blue);
        }

        holder.title.setText(entry.title);

        return convertView;
    }
}
