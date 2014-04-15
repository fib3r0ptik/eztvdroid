package com.hamaksoftware.eztvdroid.fragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.hamaksoftware.eztvdroid.R;
import com.hamaksoftware.eztvdroid.activities.Main;
import com.hamaksoftware.eztvdroid.adapters.EZTVItemAdapter;
import com.hamaksoftware.eztvdroid.adapters.ShowItemAdapter;
import com.hamaksoftware.eztvdroid.asynctasks.GetLatestShow;
import com.hamaksoftware.eztvdroid.asynctasks.GetShows;
import com.hamaksoftware.eztvdroid.models.EZTVRow;
import com.hamaksoftware.eztvdroid.models.EZTVShowItem;
import com.hamaksoftware.eztvdroid.utils.Utility;

import java.util.ArrayList;
import java.util.List;

public class ShowsFragment extends Fragment implements IAsyncTaskListener{
	

	protected ListView lv;
	protected ShowItemAdapter adapter;
	protected Main base;
	
	private ProgressDialog dialog;

    public boolean force;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.shows, container, false);
        base =  (Main)getActivity();

        
        lv = (ListView)rootView.findViewById(R.id.lshows_list);

        dialog = new ProgressDialog(getActivity());
        dialog.setIndeterminate(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setMessage(getString(R.string.loader_working));

        View empty = inflater.inflate(R.layout.latest_empty,container,false);
        lv.setEmptyView(empty);

        if(adapter == null) {
            adapter = new ShowItemAdapter(getActivity(),new ArrayList<EZTVShowItem>(0));
        }

        lv.setAdapter(adapter);



        return rootView;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        //fragmentListener.onFragmentViewCreated();
        onActivityDrawerClosed();
    }

	@Override
	public void onTaskCompleted(ArrayList<?> data) {
		if(data.size() <= 0){
			String title = getResources().getString(R.string.loader_title_request_result);
			String msg = getResources().getString(R.string.result_listing_error);
			String btnPosTitle = getResources().getString(R.string.dialog_button_ok);
			Utility.showDialog(getActivity(), title, msg, btnPosTitle, null, false, null);
		}else{
			adapter.listings = (List<EZTVShowItem>)data;
			adapter.notifyDataSetChanged();
		}
		
		dialog.dismiss();
	}


	public void onActivityDrawerClosed() {
        if(force || adapter.listings.size() <=0){
            GetShows async =  new GetShows(getActivity(), force);
            async.asyncTaskListener = this; //set this class as observer to listen to asynctask events
            async.execute();
        }
	}


	@Override
	public void onTaskWorking() {
		dialog.show();
	}

	@Override
	public void onTaskProgressUpdate(int progress) {
        dialog.setProgress(progress);
	}

	@Override
	public void onTaskProgressMax(int max) {
        dialog.setMax(max);
	}

	@Override
	public void onTaskUpdateMessage(String message) {
        dialog.setMessage(message);
	}

	@Override
	public void onTaskError(Exception e) {
        dialog.setMessage("Error: " + e.getMessage());
	}

}
