package com.hamaksoftware.eztvpal.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.hamaksoftware.eztvpal.R;
import com.hamaksoftware.eztvpal.activities.Main;
import com.hamaksoftware.eztvpal.adapters.EpisodeAdapter;
import com.hamaksoftware.eztvpal.asynctasks.GetLatestShow;
import com.hamaksoftware.eztvpal.asynctasks.SendTorrent;
import com.hamaksoftware.eztvpal.asynctasks.Subscription;
import com.hamaksoftware.eztvpal.models.Episode;
import com.hamaksoftware.eztvpal.models.Show;
import com.hamaksoftware.eztvpal.utils.ShowHandler;
import com.hamaksoftware.eztvpal.utils.Utility;

import java.util.ArrayList;

public class LatestFragment extends Fragment implements IAsyncTaskListener{

	protected ListView lv;
	protected EpisodeAdapter adapter;
	protected View footer;
	protected Main base;

	
	private ProgressDialog dialog;
    public boolean force;

    AdapterView.OnItemClickListener itemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final Episode row = adapter.listings.get(position);
            final CharSequence[] items = {getString(R.string.dialog_open),getString(R.string.dialog_copy),getString(R.string.dialog_send), getString(R.string.dialog_subscribe),getString(R.string.dialog_view)};

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


                    if(items[item].equals(getString(R.string.dialog_copy))){

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
                            Utility.getInstance(getActivity()).copyTextToClipBoard(links.get(pos));
                            base.showToast(getString(R.string.message_copy_text_successful),Toast.LENGTH_LONG);
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
                            send.asyncTaskListener = LatestFragment.this;
                            send.execute();
                        }
                    }

                    if(items[item].equals(getString(R.string.dialog_view))){
                        base = (Main)getActivity();
                        ShowHandler showHandler = new ShowHandler(getActivity());
                        if(showHandler.getCount() > 0) {
                            System.out.println(row.showId + ":" + row.title);
                            Bundle args = new Bundle();
                            args.putInt("show_id", row.showId);
                            base.launchFragment(R.string.fragment_tag_show_detail, args, true);
                        }else{
                            base.showToast("Please refresh shows section first.", Toast.LENGTH_LONG);
                        }
                    }

                    if(items[item].equals(getString(R.string.dialog_subscribe))){
                        Show show = new Show();
                        show.showId = row.showId;
                        Subscription s = new Subscription(getActivity(),show);
                        s.isSubscribe = true;
                        s.asyncTaskListener = LatestFragment.this;
                        s.execute();
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
        base.toggleHintLayout(false);
        footer = inflater.inflate(R.layout.footer, null,false);
        footer.setVisibility(View.GONE);

        lv = (ListView)rootView.findViewById(R.id.latest_list_feed);
        lv.setFooterDividersEnabled(true);
        lv.addFooterView(footer);

        
        dialog = new ProgressDialog(getActivity());
        dialog.setIndeterminate(true);

        //View empty = inflater.inflate(R.layout.latest_empty,container,false);
        //lv.setEmptyView(empty);

        if(adapter == null) {
            adapter = new EpisodeAdapter(getActivity(),new ArrayList<Episode>(0));
        }


        lv.setAdapter(adapter);
        lv.setOnItemClickListener(itemClick);
        Button btn = (Button)footer.findViewById(R.id.btnLoadMore);
        btn.setVisibility(View.VISIBLE);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                force = false;
                onActivityDrawerClosed();
            }
        });

        Bundle args = getArguments();
        if(args != null) force = getArguments().getBoolean("force");

        base.invalidateOptionsMenu();

        return rootView;
    }

    @Override
    public void onResume(){
        super.onResume();
        base.currentFragmentTag = R.string.fragment_tag_latest;
        base.invalidateOptionsMenu();
        if(adapter.getCount() > 0){
            footer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if(force || adapter.listings.size() <= 0){
            adapter.listings.clear();
            onActivityDrawerClosed();
        }
    }

	@Override
	public void onTaskCompleted(Object data, String ASYNC_ID) {
        if(data != null){
            if(ASYNC_ID.equalsIgnoreCase(GetLatestShow.ASYNC_ID)) {
                ArrayList<Episode> d = (ArrayList<Episode>) data;
                if (d.size() <= 0) {
                    String title = getResources().getString(R.string.loader_title_request_result);
                    String msg = getResources().getString(R.string.result_listing_error);
                    String btnPosTitle = getResources().getString(R.string.dialog_button_ok);
                    Utility.showDialog(getActivity(), title, msg, btnPosTitle, null, false, null);
                } else {
                    adapter.listings.addAll(d);
                    adapter.notifyDataSetChanged();
                    footer.setVisibility(View.VISIBLE);
                }
            }

            if(ASYNC_ID.equalsIgnoreCase(SendTorrent.ASYNC_ID)){
                boolean success = (Boolean)data;
                base.showToast(success?"Torrent sent successfully.":"Warning: Failed to send torrent.", Toast.LENGTH_LONG);
            }

            if(ASYNC_ID.equalsIgnoreCase(Subscription.ASYNC_ID)){
                boolean success = (Boolean)data;
                base.showToast(success ? getString(R.string.message_subscription_successful): getString(R.string.message_subscription_failure)
                        , Toast.LENGTH_LONG);
            }
        }

		
		dialog.dismiss();
        force = false;
	}

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        base.currentFragmentTag = 0;
        base.invalidateOptionsMenu();
        base.setTitle(getString(R.string.app_name));
        base.toggleHintLayout(true);
    }



	public void onActivityDrawerClosed() {
        if(force) adapter.listings.clear();
		GetLatestShow async =  new GetLatestShow(getActivity(),base.currentPage++);
		async.asyncTaskListener = this; //set this class as observer to listen to asynctask events
		async.execute();

	}


	@Override
	public void onTaskWorking(String ASYNC_ID) {
        dialog.setMessage(getString(R.string.loader_working));
		dialog.show();
	}

	@Override
	public void onTaskProgressUpdate(int progress, String ASYNC_ID) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTaskProgressMax(int max, String ASYNC_ID) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTaskUpdateMessage(String message, String ASYNC_ID) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTaskError(Exception e, String ASYNC_ID) {
		// TODO Auto-generated method stub
		
	}

}
