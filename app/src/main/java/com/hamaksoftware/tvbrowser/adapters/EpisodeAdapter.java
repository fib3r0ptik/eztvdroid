package com.hamaksoftware.tvbrowser.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hamaksoftware.tvbrowser.R;
import com.hamaksoftware.tvbrowser.utils.Utility;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.Calendar;
import java.util.List;

import info.besiera.api.models.Episode;

public class EpisodeAdapter extends BaseAdapter {
    public Context context;
    public List<Episode> listings;

    private class ViewHolder {
        TextView title;
        TextView extended_info;
        LinearLayout rowHolder;

    }

    public EpisodeAdapter(Context context, List<Episode> listing) {
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
            holder.rowHolder = (LinearLayout) convertView.findViewById(R.id.row_holder);
            holder.title = (TextView) convertView.findViewById(R.id.home_title);
            holder.extended_info = (TextView) convertView.findViewById(R.id.home_ext_info);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        final Episode entry = listings.get(position);

        if (position % 2 == 0) {
            holder.rowHolder.setBackgroundColor(Color.WHITE);
        } else {
            holder.rowHolder.setBackgroundColor(Color.rgb(227, 242, 249));
        }

        /*row.title = item.getString("title");
        row.filesize = Utility.getFancySize(item.getLong("size"));
        DateFormat formatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
        Date dte = formatter.parse(item.getString("pubdate"));
        row.elapsed = Utility.getInstance(ctx).getPrettytime().format(dte);
        row.showId = Integer.parseInt(item.getString("show_id"));*/

        PrettyTime p = new PrettyTime();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(entry.getPubdate());
        String extInfo = Utility.getFancySize(entry.getSize()) + " - " + p.format(cal.getTime());

        holder.title.setText(entry.getTitle());
        holder.extended_info.setText(extInfo);


        return convertView;
    }
}
