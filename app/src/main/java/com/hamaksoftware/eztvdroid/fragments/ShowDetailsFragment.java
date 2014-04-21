package com.hamaksoftware.eztvdroid.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hamaksoftware.eztvdroid.R;
import com.hamaksoftware.eztvdroid.activities.Main;
import com.hamaksoftware.eztvdroid.adapters.EZTVItemAdapter;
import com.hamaksoftware.eztvdroid.asynctasks.GetLatestShow;
import com.hamaksoftware.eztvdroid.asynctasks.GetShowPoster;
import com.hamaksoftware.eztvdroid.asynctasks.GetSubscriberCount;
import com.hamaksoftware.eztvdroid.asynctasks.Search;
import com.hamaksoftware.eztvdroid.asynctasks.SendTorrent;
import com.hamaksoftware.eztvdroid.asynctasks.Subscription;
import com.hamaksoftware.eztvdroid.models.EZTVRow;
import com.hamaksoftware.eztvdroid.models.EZTVShowItem;
import com.hamaksoftware.eztvdroid.utils.AppPref;
import com.hamaksoftware.eztvdroid.utils.ShowHandler;
import com.hamaksoftware.eztvdroid.utils.Utility;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class ShowDetailsFragment extends Fragment implements IAsyncTaskListener{

	protected ListView lv;
	protected View footer;
	protected Main base;

    private EZTVShowItem show;
    private ImageView img;
    private TextView d;
    private TextView s;
    private TextView status;
    private EZTVItemAdapter adapter;

	private ProgressDialog dialog;
    public boolean force;


    public void setShowDetails(int showId){
        show =  base.sh.getShow(showId);
        base.setTitle(show.title);
        status.setText(show.isSubscribed?"UNSUBSCRIBE":"SUBSCRIBE");
        if(show.isSubscribed){
            status.setBackgroundColor(getResources().getColor(R.color.torrent_completed));
            status.setTextColor(Color.WHITE);
        }else{
            status.setBackgroundColor(getResources().getColor(R.color.torrent_progress));
            status.setTextColor(Color.WHITE);
        }
    }


    AdapterView.OnItemClickListener itemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final EZTVRow row = adapter.listings.get(position-1);
            final CharSequence[] items = {getString(R.string.dialog_open),getString(R.string.dialog_send)};

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(row.title);
            builder.setItems(items, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    if(items[item].equals(getString(R.string.dialog_open))){

                        final ArrayList<String> links = (ArrayList<String>)row.links;
                        AlertDialog.Builder linkbuilder = new AlertDialog.Builder(getActivity());
                        final String[] slinks  = new String[links.size()];
                        int ctr = 0;
                        for(String link: links){
                            if(link.toLowerCase().contains("magnet")){
                                slinks[ctr] = "Magnet Link";
                            }else{
                                slinks[ctr] = "Link # " + (ctr + 1);
                            }

                            ctr++;
                        }

                        linkbuilder.setItems(slinks,new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int pos) {
                                try {
                                    Utility.getInstance(getActivity()).markDownload(row.title,row.showId);
                                    Intent i = new Intent(Intent.ACTION_VIEW);
                                    i.setData(Uri.parse(links.get(pos)));
                                    startActivity(i);
                                }catch(ActivityNotFoundException e){
                                    Utility.showDialog(getActivity(),getString(R.string.dialog_title_info),
                                            getString(R.string.unknown_handler),getString(R.string.dialog_button_ok),
                                            getString(R.string.dialog_button_close),true,null);
                                }
                            }
                        });

                        AlertDialog linkalert = linkbuilder.create();
                        linkalert.show();

                    }

                    if(items[item].equals(getString(R.string.dialog_send))){
                        if(base.pref.getClientName().length() < 2){
                            base.showToast("Set up a profile for a torrent client in the settings first.",Toast.LENGTH_LONG);
                        }else{
                            Utility.getInstance(getActivity()).markDownload(row.title,row.showId);
                            SendTorrent send = new SendTorrent(getActivity(),row);
                            send.asyncTaskListener = ShowDetailsFragment.this;
                            send.execute();
                        }
                    }

                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.home, container, false);
        base =  (Main)getActivity();

        final View headerView = inflater.inflate(R.layout.show_detail_header, null, false);


        adapter = new EZTVItemAdapter(getActivity(), new ArrayList<EZTVRow>(0));
        img = (ImageView)headerView.findViewById(R.id.poster);


        lv = (ListView)rootView.findViewById(R.id.latest_list_feed);
        lv.setVerticalScrollBarEnabled(false);
        lv.setScrollBarStyle(ListView.SCROLLBARS_OUTSIDE_OVERLAY);
        lv.setOnItemClickListener(itemClick);
        lv.addHeaderView(headerView);

        lv.setAdapter(adapter);

        dialog = new ProgressDialog(getActivity());
        dialog.setIndeterminate(true);

        Bundle payload = getArguments();

        show =  base.sh.getShow(payload.getInt("show_id"));
        base.setTitle(show.title);


        d = (TextView)rootView.findViewById(R.id.show_detail_downcount);
        s = (TextView)rootView.findViewById(R.id.show_detail_subcount);


        ((TextView)rootView.findViewById(R.id.show_details_title)).setText(show.title);

        status = (TextView)rootView.findViewById(R.id.show_detail_status);
        status.setText(show.isSubscribed?"UNSUBSCRIBE":"SUBSCRIBE");
        if(show.isSubscribed){
            status.setBackgroundColor(getResources().getColor(R.color.torrent_completed));
            status.setTextColor(Color.WHITE);
        }else{
            status.setBackgroundColor(getResources().getColor(R.color.torrent_progress));
            status.setTextColor(Color.WHITE);
        }

        status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Subscription subscription = new Subscription(getActivity(),show);
                subscription.asyncTaskListener = ShowDetailsFragment.this;
                subscription.execute();
            }
        });

        base.invalidateOptionsMenu();

        return rootView;
    }

    @Override
    public void onResume(){
        super.onResume();
        base.currentFragmentTag = R.string.fragment_tag_show_detail;
        base.invalidateOptionsMenu();
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if(force || adapter.listings.size() <= 0){
            adapter.listings.clear();
            onActivityDrawerClosed();
        }
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        base.setTitle(getString(R.string.app_name));
    }


	public void onActivityDrawerClosed() {
        GetShowPoster async = new GetShowPoster(getActivity(),show);
        async.asyncTaskListener = this;
        async.execute();

        GetSubscriberCount info = new GetSubscriberCount(getActivity(),show);
        info.asyncTaskListener = this;
        info.execute();

        Search search = new Search(getActivity(),show.showId+"",true);
        search.asyncTaskListener = this;
        search.execute();
	}


    @Override
    public void onTaskCompleted(Object data, String ASYNC_ID) {
        if(ASYNC_ID.equalsIgnoreCase(GetShowPoster.ASYNC_ID)) {
            Drawable d = (Drawable) data;
            if (d != null) {
                img.setImageDrawable(d);
            } else {
                img.setVisibility(View.GONE);
            }
        }

        if(ASYNC_ID.equalsIgnoreCase(Search.ASYNC_ID)){
            if(data != null){
                ArrayList<EZTVRow> items = (ArrayList<EZTVRow>)data;
                adapter.listings = items;
                adapter.notifyDataSetChanged();
            }else{
                Toast.makeText(getActivity(),"No Episodes for " + show.title + " found.",Toast.LENGTH_LONG).show();
            }

            dialog.dismiss();
        }

        if(ASYNC_ID.equalsIgnoreCase(GetSubscriberCount.ASYNC_ID)){
            if(data != null){
                String response = (String)data;
                try {
                    JSONArray arr = new JSONArray(response);
                    int sc = arr.getInt(0);
                    int dc = arr.getInt(1);

                    String text = dc+"\n"+"downloads";
                    SpannableStringBuilder sb = new SpannableStringBuilder(text);

                    StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD);
                    sb.setSpan(bss, 0, text.length()-9, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    d.setText(sb);

                    String text2 = sc+"\n"+"subscriptions";
                    SpannableStringBuilder sb2 = new SpannableStringBuilder(text2);

                    sb2.setSpan(bss, 0, text2.length()-13, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    s.setText(sb2);

                } catch (JSONException e) {
                    Log.e("async-info",e.getMessage());
                }
            }

        }

        if(ASYNC_ID.equalsIgnoreCase(SendTorrent.ASYNC_ID)){
            boolean success = (Boolean)data;
            base.showToast(success?"Torrent sent successfully.":"Warning: Failed to send torrent.", Toast.LENGTH_LONG);
        }

        if(ASYNC_ID.equalsIgnoreCase(Subscription.ASYNC_ID)){
            if(data != null){
                boolean res = (Boolean)data;
                if(res){
                    if(show.isSubscribed){
                        show.isSubscribed = false;
                        status.setBackgroundColor(getResources().getColor(R.color.torrent_progress));
                        status.setTextColor(Color.WHITE);
                        status.setText("SUBSCRIBE");
                    }else{
                        status.setBackgroundColor(getResources().getColor(R.color.torrent_completed));
                        status.setTextColor(Color.WHITE);
                        show.isSubscribed = true;
                        status.setText("UNSUBSCRIBE");
                    }
                    base.sh.updateShow(show);

                    GetSubscriberCount getSubscriberCount = new GetSubscriberCount(getActivity(),show);
                    getSubscriberCount.asyncTaskListener = ShowDetailsFragment.this;
                    getSubscriberCount.execute();

                }
            }
        }

    }

    @Override
	public void onTaskWorking(String ASYNC_ID) {
        if(ASYNC_ID.equalsIgnoreCase(Search.ASYNC_ID)){
            dialog.setMessage("Searching episodes for " + show.title);
            dialog.show();
        }
	}

	@Override
	public void onTaskProgressUpdate(int progress,String ASYNC_ID) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTaskProgressMax(int max,String ASYNC_ID) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTaskUpdateMessage(String message,String ASYNC_ID) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTaskError(Exception e,String ASYNC_ID) {
		// TODO Auto-generated method stub
		
	}

}
