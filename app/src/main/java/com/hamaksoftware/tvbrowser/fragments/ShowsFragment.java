package com.hamaksoftware.tvbrowser.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.hamaksoftware.tvbrowser.R;
import com.hamaksoftware.tvbrowser.activities.Main;
import com.hamaksoftware.tvbrowser.adapters.ShowAdapter;
import com.hamaksoftware.tvbrowser.asynctasks.GetShows;
import com.hamaksoftware.tvbrowser.asynctasks.Subscribe;
import com.hamaksoftware.tvbrowser.utils.Utility;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;

import java.util.ArrayList;


import fr.castorflex.android.circularprogressbar.CircularProgressDrawable;
import info.besiera.api.APIRequestException;
import info.besiera.api.models.Show;

public class ShowsFragment extends Fragment implements IAsyncTaskListener {

    private ProgressDialog progress;
    protected PullToRefreshGridView lv;
    public ShowAdapter adapter;
    protected Main base;


    private boolean force;


    AdapterView.OnItemClickListener itemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final Show show = adapter.shows.get(position);
            final CharSequence[] items = {getString(R.string.dialog_subscribe), getString(R.string.dialog_view)};

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(show.getTitle());
            builder.setItems(items, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    if (items[item].equals(getString(R.string.dialog_view))) {
                        base = (Main) getActivity();
                        Bundle args = new Bundle();
                        args.putInt("show_id", show.getShowId());
                        base.launchFragment(R.string.fragment_tag_show_detail, args, false);
                    }

                    if (items[item].equals(getString(R.string.dialog_subscribe))) {
                        Subscribe s = new Subscribe(getActivity(),show.getShowId());
                        s.asyncTaskListener = ShowsFragment.this;
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
        final View rootView = inflater.inflate(R.layout.my_shows, container, false);
        base = (Main) getActivity();
        base.toggleHintLayout(false);

        lv = (PullToRefreshGridView) rootView.findViewById(R.id.myshow_grid);
        lv.getRefreshableView().setOnItemClickListener(itemClick);

        progress = new ProgressDialog(getActivity());
        progress.setIndeterminateDrawable(new CircularProgressDrawable
                .Builder(getActivity())
                .colors(getResources().getIntArray(R.array.gplus_colors))
                .sweepSpeed(1f)
                .style(CircularProgressDrawable.Style.NORMAL).build());
        //View empty = inflater.inflate(R.layout.latest_empty, container, false);
        //lv.getRefreshableView().setEmptyView(empty);

        if (adapter == null) {
            adapter = new ShowAdapter(getActivity(), new ArrayList<Show>(0));
        }

        lv.getRefreshableView().setAdapter(adapter);
        lv.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<GridView>() {
            @Override
            public void onRefresh(PullToRefreshBase<GridView> refreshView) {
                lv.setRefreshing();
                force = true;
                onActivityDrawerClosed();
            }
        });

        base.invalidateOptionsMenu();

        Bundle payload = getArguments();
        if (payload != null) {
            if (payload.containsKey("force")) {
                force = payload.getBoolean("force");
            }
        }

        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();
        force = false;
        base.currentFragmentTag = R.string.fragment_tag_shows;
        base.invalidateOptionsMenu();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if(force || adapter.shows.size() <= 0) {
            onActivityDrawerClosed();
        }
    }

    @Override
    public void onTaskCompleted(Object data, String ASYNC_ID) {
        if (data != null) {
            if (ASYNC_ID.equalsIgnoreCase(GetShows.ASYNC_ID)) {
                lv.onRefreshComplete();
                adapter.setShows((ArrayList<Show>) data);
                adapter.notifyDataSetChanged();
            }

            if (ASYNC_ID.equalsIgnoreCase(Subscribe.ASYNC_ID)) {
                Boolean success = (Boolean) data;
                base.showToast(success ? getString(R.string.message_subscription_successful) : getString(R.string.message_subscription_failure)
                        , Toast.LENGTH_LONG);
            }

        }

        if(progress != null) progress.dismiss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        base.currentFragmentTag = 0;
        base.invalidateOptionsMenu();
        //base.setTitle(getString(R.string.app_name));
        base.toggleHintLayout(true);
    }


    public void refreshData(boolean force) {
        this.force = force;
        onActivityDrawerClosed();
        this.force = false;
    }

    public void onActivityDrawerClosed() {
        GetShows async = new GetShows(getActivity());
        async.asyncTaskListener = this; //set this class as observer to listen to asynctask events
        async.execute();
        force = false;
    }


    @Override
    public void onTaskWorking(String ASYNC_ID) {
        //base.showToast(getString(R.string.loader_working),Toast.LENGTH_SHORT);
        progress.setMessage(getString(R.string.loader_working));
        progress.show();
    }

    @Override
    public void onTaskProgressUpdate(int progress, String ASYNC_ID) {

    }

    @Override
    public void onTaskProgressMax(int max, String ASYNC_ID) {

    }

    @Override
    public void onTaskUpdateMessage(String message, String ASYNC_ID) {

    }

    @Override
    public void onTaskError(final Exception e, String ASYNC_ID) {
        if(ASYNC_ID.equalsIgnoreCase(GetShows.ASYNC_ID)){
            base.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    APIRequestException _e = (APIRequestException)e;
                    Utility.showDialog(getActivity(), null, _e.getStatus().getDescription(), "Okay", null, false, null);
                    lv.onRefreshComplete();
                }
            });
        }
        if(progress != null) progress.dismiss();
    }

}
