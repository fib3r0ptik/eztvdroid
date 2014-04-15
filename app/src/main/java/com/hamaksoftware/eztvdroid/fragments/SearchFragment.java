package com.hamaksoftware.eztvdroid.fragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.hamaksoftware.eztvdroid.R;
import com.hamaksoftware.eztvdroid.activities.Main;
import com.hamaksoftware.eztvdroid.adapters.EZTVItemAdapter;
import com.hamaksoftware.eztvdroid.asynctasks.GetLatestShow;
import com.hamaksoftware.eztvdroid.asynctasks.Search;
import com.hamaksoftware.eztvdroid.models.EZTVRow;
import com.hamaksoftware.eztvdroid.utils.Utility;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment implements IAsyncTaskListener{
	


	protected ListView lv;
	protected EZTVItemAdapter adapter;
	protected View footer;
	protected Main base;
	
	private ProgressDialog dialog;
    private String query;
    private boolean byId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.home, container, false);
        base =  (Main)getActivity();

        //footer = inflater.inflate(R.layout.footer, null,false);


        dialog = new ProgressDialog(getActivity());
        dialog.setIndeterminate(true);
        dialog.setMessage(getString(R.string.loader_searching));

        lv = (ListView)rootView.findViewById(R.id.latest_list_feed);
        //View empty = inflater.inflate(R.layout.search_empty,container,true);
        //lv.setEmptyView(empty);

        if(adapter == null) {
            adapter = new EZTVItemAdapter(getActivity(),new ArrayList<EZTVRow>(0));
        }

        lv.setAdapter(adapter);


        query = getArguments().getString("query");
        byId = getArguments().getBoolean("byId");

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        onActivityDrawerClosed();
    }

	@Override
	public void onTaskCompleted(ArrayList<?> data) {
        adapter.listings.addAll((List<EZTVRow>)data);
        adapter.notifyDataSetChanged();
		dialog.dismiss();
	}

	

	public void onActivityDrawerClosed() {
		Search async =  new Search(getActivity(),query,byId);
		async.asyncTaskListener = this; //set this class as observer to listen to asynctask events
		async.execute();
	}


    @Override
	public void onTaskWorking() {
		dialog.show();
	}
	


	@Override
	public void onTaskProgressUpdate(int progress) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTaskProgressMax(int max) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTaskUpdateMessage(String message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTaskError(Exception e) {
		// TODO Auto-generated method stub
		
	}

}
