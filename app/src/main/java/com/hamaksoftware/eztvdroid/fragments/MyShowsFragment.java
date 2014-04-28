package com.hamaksoftware.eztvdroid.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.hamaksoftware.eztvdroid.R;
import com.hamaksoftware.eztvdroid.activities.Main;
import com.hamaksoftware.eztvdroid.adapters.MyShowAdapter;
import com.hamaksoftware.eztvdroid.asynctasks.CheckForNewEpisode;
import com.hamaksoftware.eztvdroid.asynctasks.GetMyShows;
import com.hamaksoftware.eztvdroid.models.Show;
import com.hamaksoftware.eztvdroid.utils.Utility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MyShowsFragment extends Fragment implements IAsyncTaskListener{
	

	protected GridView lv;
	public MyShowAdapter adapter;
	protected Main base;

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
        final View rootView = inflater.inflate(R.layout.my_shows, container, false);
        base =  (Main)getActivity();

        
        lv = (GridView)rootView.findViewById(R.id.myshow_grid);
        lv.setOnItemClickListener(itemClick);

        if(adapter == null) {
            adapter = new MyShowAdapter(getActivity());
            adapter.setShows(new ArrayList<Show>(0));
        }

        lv.setAdapter(adapter);

        base.invalidateOptionsMenu();

        return rootView;
    }


    @Override
    public void onResume(){
        super.onResume();
        base.currentFragmentTag = R.string.fragment_tag_myshows;
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
            if(ASYNC_ID.equalsIgnoreCase(GetMyShows.ASYNC_ID)){
                List<Show> d = (List<Show>) data;
                if (d.size() <= 0) {
                    String title = getResources().getString(R.string.loader_title_request_result);
                    String msg = getResources().getString(R.string.result_listing_error);
                    String btnPosTitle = getResources().getString(R.string.dialog_button_ok);
                    Utility.showDialog(getActivity(), title, msg, btnPosTitle, null, false, null);
                } else {
                    adapter.setShows((ArrayList<Show>) d);
                    adapter.notifyDataSetChanged();

                    if(adapter.shows.size() > 0){
                        CheckForNewEpisode chk = new CheckForNewEpisode(getActivity());
                        chk.asyncTaskListener = this;
                        chk.execute();
                    }

                }
            }

            if(ASYNC_ID.equalsIgnoreCase(CheckForNewEpisode.ASYNC_ID)){
                String resp = (String)data;
                try {
                    JSONArray arr = new JSONArray(resp);
                    for(int i = 0; i < arr.length();i++){
                        JSONObject item = arr.getJSONObject(i);
                        for(Show showItem:adapter.shows){
                            //System.out.println(showItem.showId + ":" + item.getInt("id"));
                            if(showItem.showId == item.getInt("id")){
                                showItem.hasNewEpisode = true;
                                break;
                            }

                        }
                    }

                    adapter.notifyDataSetChanged();
                }catch (Exception e){
                    System.out.println(e.getMessage());
                }

            }
        }

	}

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        base.setTitle(getString(R.string.app_name));
    }


    public void refreshData(boolean force){
        onActivityDrawerClosed();
        force = false;
    }

	public void onActivityDrawerClosed() {
        if(adapter.shows.size() <=0){
            GetMyShows async = new GetMyShows(getActivity());
            async.asyncTaskListener = this;
            async.execute();
        }
	}


	@Override
	public void onTaskWorking(String ASYNC_ID) {

	}

	@Override
	public void onTaskProgressUpdate(int progress,String ASYNC_ID) {

	}

	@Override
	public void onTaskProgressMax(int max,String ASYNC_ID){
	}

	@Override
	public void onTaskUpdateMessage(String message,String ASYNC_ID) {

	}

	@Override
	public void onTaskError(Exception e,String ASYNC_ID) {

	}

}
