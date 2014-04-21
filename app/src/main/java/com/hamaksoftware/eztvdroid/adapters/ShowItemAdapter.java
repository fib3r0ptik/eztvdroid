package com.hamaksoftware.eztvdroid.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hamaksoftware.eztvdroid.R;
import com.hamaksoftware.eztvdroid.models.EZTVShowItem;

import java.util.ArrayList;
import java.util.List;

public class ShowItemAdapter extends BaseAdapter implements Filterable {
    public Context context;
    public List<EZTVShowItem> shows;
    public List<EZTVShowItem> copy;

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                shows = (List<EZTVShowItem>) results.values;
                notifyDataSetChanged();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                FilterResults results = new FilterResults();
                ArrayList<EZTVShowItem> filteredShows = new ArrayList<EZTVShowItem>();

                constraint = constraint.toString().toLowerCase();
                if(constraint.length() > 0) {
                    for (int i = 0; i < copy.size(); i++) {
                        EZTVShowItem show = copy.get(i);
                        if (show.title.toLowerCase().startsWith(constraint.toString())) {
                            filteredShows.add(show);
                        }
                    }
                }else{
                    filteredShows.addAll(copy);
                }

                results.count = filteredShows.size();
                results.values = filteredShows;


                return results;
            }
        };

        return filter;
    }

    private class ViewHolder{
        LinearLayout rowHolder;
        TextView title;
        CheckBox chk;

    }

    public ShowItemAdapter(Context context, ArrayList<EZTVShowItem> shows) {
        this.context = context;
        this.shows = shows;
        this.copy = new ArrayList<EZTVShowItem>(shows);
    }

    public void setShows(ArrayList<EZTVShowItem> shows){
        this.shows = shows;
        this.copy = new ArrayList<EZTVShowItem>(shows);
    }

    public int getCount() {
        return shows.size();
    }

    public Object getItem(int position) {
        return shows.get(position);
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


        final EZTVShowItem entry = shows.get(position);

        if(position % 2 == 0){
            holder.rowHolder.setBackgroundColor(Color.WHITE);
        }else{
            holder.rowHolder.setBackgroundResource(R.color.alt_blue);
        }

        holder.title.setText(entry.title);

        return convertView;
    }
}
