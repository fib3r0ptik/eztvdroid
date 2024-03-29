package com.hamaksoftware.tvbrowser.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.hamaksoftware.tvbrowser.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.pollexor.Thumbor;

import java.util.ArrayList;
import java.util.List;

import info.besiera.api.models.Show;

public class ShowAdapter extends BaseAdapter implements Filterable {
    public Context context;
    public List<Show> shows;
    public List<Show> copy;

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                shows = (List<Show>) results.values;
                notifyDataSetChanged();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                FilterResults results = new FilterResults();
                ArrayList<Show> filteredShows = new ArrayList<Show>();

                constraint = constraint.toString().toLowerCase();
                if (constraint.length() > 0) {
                    for (int i = 0; i < copy.size(); i++) {
                        Show show = copy.get(i);
                        if (show.getTitle().toLowerCase().startsWith(constraint.toString())) {
                            filteredShows.add(show);
                        }
                    }
                } else {
                    filteredShows.addAll(copy);
                }

                results.count = filteredShows.size();
                results.values = filteredShows;


                return results;
            }
        };

        return filter;
    }

    private class ViewHolder {
        TextView title;
        ImageView img;

    }

    public ShowAdapter(Context context, ArrayList<Show> shows) {
        this.context = context;
        this.shows = shows;
        this.copy = new ArrayList<Show>(shows);
    }

    public void setShows(ArrayList<Show> shows) {
        this.shows = shows;
        this.copy = new ArrayList<Show>(shows);
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
            convertView = inflater.inflate(R.layout.show_row, null);
            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.show_title);
            holder.img = (ImageView) convertView.findViewById(R.id.imgGrid);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        final Show entry = shows.get(position);
        holder.title.setText(entry.getTitle());
        //String url = "http://hamaksoftware.com/myeztv/api-beta.php?method=t&id=" + entry.getShowId();
        Thumbor thumbor = Thumbor.create("http://besiera.info:8888/");
        String url = thumbor.buildImage("http://besiera.info/apibackend/tvimg/" + entry.getShowId() + ".jpg")
                .resize(250, 250).smart().toUrl();

        ImageLoader.getInstance().displayImage(url, holder.img);

        return convertView;
    }
}
