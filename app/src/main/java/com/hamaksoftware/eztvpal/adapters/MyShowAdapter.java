package com.hamaksoftware.eztvpal.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hamaksoftware.eztvpal.R;
import com.hamaksoftware.eztvpal.models.Show;
import com.hamaksoftware.eztvpal.utils.ImageLoader;

import java.util.ArrayList;
import java.util.List;

public class MyShowAdapter extends BaseAdapter{
    public Context ctx;
    public List<Show> shows;

    private ImageLoader imageLoader;

    private class ViewHolder{
        ImageView img;
        TextView txt;
        TextView title;
    }

    public MyShowAdapter(Context context) {
        this.ctx = context;
        imageLoader = new ImageLoader(context);
    }

    public void setShows(ArrayList<Show> shows){
        this.shows = shows;
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
            LayoutInflater inflater = (LayoutInflater) ctx
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.my_show_row, null);
            holder = new ViewHolder();
            holder.img = (ImageView) convertView.findViewById(R.id.imgGrid);
            holder.txt = (TextView) convertView.findViewById(R.id.notification);
            holder.title = (TextView)convertView.findViewById(R.id.myshow_title);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder)convertView.getTag();
        }


        final Show entry = shows.get(position);
        String url = "http://hamaksoftware.com/myeztv/api-beta.php?method=t&id=" + entry.showId;
        //String url = "http://hamaksoftware.com/myeztv/tvimg/" + entry.showId + ".jpg";
        holder.title.setText(entry.title);
        holder.txt.setVisibility(entry.hasNewEpisode?View.VISIBLE:View.GONE);
        imageLoader.DisplayImage(url, holder.img);
        return convertView;
    }
}
