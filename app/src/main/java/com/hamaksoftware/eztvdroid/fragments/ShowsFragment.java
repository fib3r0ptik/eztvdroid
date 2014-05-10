package com.hamaksoftware.eztvdroid.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.hamaksoftware.eztvdroid.R;
import com.hamaksoftware.eztvdroid.activities.Main;
import com.hamaksoftware.eztvdroid.adapters.ShowAdapter;
import com.hamaksoftware.eztvdroid.asynctasks.GetShows;
import com.hamaksoftware.eztvdroid.models.Show;
import com.hamaksoftware.eztvdroid.utils.ShowHandler;
import com.hamaksoftware.eztvdroid.utils.Utility;

import java.util.ArrayList;
import java.util.List;

public class ShowsFragment extends Fragment implements IAsyncTaskListener{
	

	protected ListView lv;
	public ShowAdapter adapter;
	protected Main base;
	
	private ProgressDialog dialog;

    public boolean force;


    AdapterView.OnItemClickListener itemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final Show show = adapter.shows.get(position);
            final CharSequence[] items = {getString(R.string.dialog_view)};

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(show.title);
            builder.setItems(items, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    if(items[item].equals(getString(R.string.dialog_view))){
                        base = (Main)getActivity();
                        Bundle args = new Bundle();
                        args.putInt("show_id", show.showId);
                        base.launchFragment(R.string.fragment_tag_show_detail, args, false);
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
        final View rootView = inflater.inflate(R.layout.shows, container, false);
        base =  (Main)getActivity();

        
        lv = (ListView)rootView.findViewById(R.id.lshows_list);
        lv.setOnItemClickListener(itemClick);

        if(dialog == null) {
            dialog = new ProgressDialog(getActivity());
        }

        dialog.setIndeterminate(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setMessage(getString(R.string.loader_working));

        View empty = inflater.inflate(R.layout.latest_empty,container,false);
        lv.setEmptyView(empty);

        if(adapter == null) {
            adapter = new ShowAdapter(getActivity(),new ArrayList<Show>(0));
        }

        lv.setAdapter(adapter);

        base.invalidateOptionsMenu();

        return rootView;
    }


    @Override
    public void onResume(){
        super.onResume();
        force = false;
        base.currentFragmentTag = R.string.fragment_tag_shows;
        base.invalidateOptionsMenu();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        //fragmentListener.onFragmentViewCreated();
        onActivityDrawerClosed();
    }

	@Override
	public void onTaskCompleted(Object data,String ASYNC_ID) {
        if(data != null) {
            if(ASYNC_ID.equalsIgnoreCase(GetShows.ASYNC_ID)){
                List<Show> d = (List<Show>) data;
                if (d.size() <= 0) {
                    String title = getResources().getString(R.string.loader_title_request_result);
                    String msg = getResources().getString(R.string.result_listing_error);
                    String btnPosTitle = getResources().getString(R.string.dialog_button_ok);
                    Utility.showDialog(getActivity(), title, msg, btnPosTitle, null, false, null);
                } else {
                    //adapter.listings = (List<Show>) d;
                    adapter.setShows((ArrayList<Show>) d);
                    adapter.notifyDataSetChanged();
                }
            }
        }
		dialog.dismiss();
	}

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        base.setTitle(getString(R.string.app_name));
    }


    public void refreshData(boolean force){
        this.force = force;
        onActivityDrawerClosed();
        this.force = false;
    }

	public void onActivityDrawerClosed() {
        ShowHandler sh = new ShowHandler(getActivity());
        if(force || sh.getCount() <= 0){
            GetShows async =  new GetShows(getActivity(), force);
            async.asyncTaskListener = this; //set this class as observer to listen to asynctask events
            async.execute();
        }
	}


	@Override
	public void onTaskWorking(String ASYNC_ID) {
		dialog.show();
	}

	@Override
	public void onTaskProgressUpdate(int progress,String ASYNC_ID) {
        dialog.setProgress(progress);
	}

	@Override
	public void onTaskProgressMax(int max,String ASYNC_ID) {
        dialog.setMax(max);
	}

	@Override
	public void onTaskUpdateMessage(String message,String ASYNC_ID) {
        dialog.setMessage(message);
	}

	@Override
	public void onTaskError(Exception e,String ASYNC_ID) {
        dialog.setMessage("Error: " + e.getMessage());
	}

}
