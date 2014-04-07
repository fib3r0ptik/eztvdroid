package com.hamaksoftware.eztvdroid.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.hamaksoftware.eztvdroid.R;
import com.hamaksoftware.eztvdroid.models.EZTVRow;
import com.hamaksoftware.eztvdroid.models.EZTVShowItem;

import java.util.List;

public class EZTVItemAdapter extends BaseAdapter{
    public Context context;
    public List<EZTVRow> listings;

    private class ViewHolder{
        TextView title;
        TextView extended_info;
        CheckBox chk;
        ImageView watched;
        ImageView fav;
        ImageView show;
        LinearLayout rowHolder;

    }

    public EZTVItemAdapter(Context context, List<EZTVRow> listing) {
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
            convertView = inflater.inflate(R.layout.home_row, null);
            holder = new ViewHolder();
            holder.rowHolder = (LinearLayout)convertView.findViewById(R.id.row_holder);
            holder.title = (TextView) convertView.findViewById(R.id.home_title);
            holder.chk = (CheckBox) convertView.findViewById(R.id.home_chk_item);
            holder.extended_info = (TextView) convertView.findViewById(R.id.home_ext_info);
            holder.title = (TextView) convertView.findViewById(R.id.home_title);
            holder.fav = (ImageView) convertView.findViewById(R.id.imgfav);
            holder.watched = (ImageView) convertView.findViewById(R.id.imgcheck);
            holder.show = (ImageView) convertView.findViewById(R.id.imgshow);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder)convertView.getTag();
        }


        final EZTVRow entry = listings.get(position);

        if(position % 2 == 0){
            holder.rowHolder.setBackgroundColor(Color.WHITE);
        }else{
            holder.rowHolder.setBackgroundResource(R.color.alt_blue);
        }

        if(entry.showId > 0 && entry.showId != 187){
            holder.show.setVisibility(View.VISIBLE);
            holder.fav.setVisibility(entry.isFavorite?View.VISIBLE:View.GONE);
            holder.watched.setVisibility(entry.isWatched?View.VISIBLE:View.GONE);
            if(entry.isWatched){
                holder.title.setTextColor(Color.parseColor("#ff999999"));
                holder.extended_info.setTextColor(Color.parseColor("#ff999999"));
            }else{
                holder.title.setTextColor(Color.parseColor("#ff000000"));
                holder.extended_info.setTextColor(Color.parseColor("#ff000000"));
            }

            /*if(hasShows){
                EZTVShowItem sitem = sh.getShow(entry.showId);
                if(sitem != null){
                    convertView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View arg0) {
                            Intent showDetails = new Intent(getApplicationContext(),ShowDetails.class);
                            showDetails.putExtra("show_id", entry.showId);
                            startActivity(showDetails);
                        }
                    });
                }
            }*/

        }else{
            convertView.setOnClickListener(null);
            holder.show.setVisibility(View.GONE);
            holder.fav.setVisibility(View.GONE);
            holder.watched.setVisibility(View.GONE);
            holder.title.setTextColor(Color.parseColor("#ff000000"));
            holder.extended_info.setTextColor(Color.parseColor("#ff000000"));
        }



        /*
        if(ids != null){
            for(int i = 0; i < ids.length;i++){
                int id = Integer.valueOf(ids[i]);
                if(id == entry.showId){
                    holder.title.setTextColor(Color.rgb(255, 136, 13));
                    holder.title.setTypeface(null, Typeface.BOLD);
                    break;
                }else{
                    holder.title.setTypeface(null, Typeface.NORMAL);
                    holder.title.setTextColor(Color.BLACK);
                }
            }
        }
        */

        /*
        holder.chk.setOnCheckedChangeListener(chkChangeList);
        holder.chk.setTag(position);
        holder.chk.setChecked(entry.isSelected);
        */

        holder.title.setText(entry.title);
        holder.extended_info.setText(entry.filesize + " - " + entry.elapsed);

        return convertView;
    }
}
