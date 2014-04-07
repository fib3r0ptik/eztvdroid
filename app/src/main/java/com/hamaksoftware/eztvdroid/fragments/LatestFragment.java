package com.hamaksoftware.eztvdroid.fragments;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
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
import com.hamaksoftware.eztvdroid.models.EZTVRow;
import com.hamaksoftware.eztvdroid.utils.Utility;

public class LatestFragment extends Fragment implements IAsyncTaskListener,IActivityListener{
	
	public IFragmentListener fragmentListener;

	protected ListView lv;
	protected EZTVItemAdapter adapter;
	protected View footer;
	protected Main base;
	
	private ProgressDialog dialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.home, container, false);
        base =  (Main)getActivity();

        footer = inflater.inflate(R.layout.footer, null,false);
        
		base.activityListener = this;
		fragmentListener = base;
        
        lv = (ListView)rootView.findViewById(R.id.latest_list_feed);
        lv.setFooterDividersEnabled(true);
        lv.addFooterView(footer);
        
        dialog = new ProgressDialog(getActivity());
        dialog.setIndeterminate(true);
        dialog.setMessage(getString(R.string.loader_working));
        
        adapter = new EZTVItemAdapter(getActivity(),new ArrayList<EZTVRow>(0));
        lv.setAdapter(adapter);
        

        Button btn = (Button)footer.findViewById(R.id.btnLoadMore);
        btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
                onActivityDrawerClosed();
			}
		});

        fragmentListener.onFragmentViewCreated();
        
        return rootView;
    }
	

	@SuppressWarnings("unchecked")
	@Override
	public void onTaskCompleted(ArrayList<?> data) {
		if(data.size() <= 0){
			String title = getResources().getString(R.string.loader_title_request_result);
			String msg = getResources().getString(R.string.result_listing_error);
			String btnPosTitle = getResources().getString(R.string.dialog_button_ok);
			
			Utility.showDialog(getActivity(), title, msg, btnPosTitle, null, false, null);
			
		}else{
			adapter.listings.addAll((List<EZTVRow>) data);
			adapter.notifyDataSetChanged();
		}
		
		dialog.dismiss();
	}

	
	@Override
	public void onActivityDrawerClosed() {
		GetLatestShow async =  new GetLatestShow(getActivity(),base.currentPage++);
		async.asyncTaskListener = this; //set this class as observer to listen to asynctask events
		async.execute();
	}

	@Override
	public void onTaskWorking() {
		dialog.show();
	}
	
	
	@Override
	public void onFragmentLaunched() {
		onActivityDrawerClosed();
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
