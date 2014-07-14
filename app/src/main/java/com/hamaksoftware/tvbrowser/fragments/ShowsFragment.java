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
import android.widget.ListView;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.hamaksoftware.tvbrowser.R;
import com.hamaksoftware.tvbrowser.activities.Main;
import com.hamaksoftware.tvbrowser.adapters.ShowAdapter;
import com.hamaksoftware.tvbrowser.asynctasks.GetShows;
import com.hamaksoftware.tvbrowser.asynctasks.Subscription;
import com.hamaksoftware.tvbrowser.models.Show;
import com.hamaksoftware.tvbrowser.utils.Utility;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;

import java.util.ArrayList;
import java.util.List;

public class ShowsFragment extends Fragment implements IAsyncTaskListener {


    protected PullToRefreshGridView lv;
    public ShowAdapter adapter;
    protected Main base;

    private ProgressDialog dialog;

    public boolean force;


    AdapterView.OnItemClickListener itemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final Show show = adapter.shows.get(position);
            final CharSequence[] items = {getString(R.string.dialog_subscribe), getString(R.string.dialog_view)};

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(show.title);
            builder.setItems(items, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    if (items[item].equals(getString(R.string.dialog_view))) {
                        base = (Main) getActivity();
                        Bundle args = new Bundle();
                        args.putInt("show_id", show.showId);
                        base.launchFragment(R.string.fragment_tag_show_detail, args, false);
                    }

                    if (items[item].equals(getString(R.string.dialog_subscribe))) {
                        Subscription s = new Subscription(getActivity(), show);
                        s.isSubscribe = true;
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

        if (dialog == null) {
            dialog = new ProgressDialog(getActivity());
        }

        dialog.setIndeterminate(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setMessage(getString(R.string.loader_working));

        View empty = inflater.inflate(R.layout.latest_empty, container, false);
        lv.getRefreshableView().setEmptyView(empty);

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
        //fragmentListener.onFragmentViewCreated();
        onActivityDrawerClosed();
    }

    @Override
    public void onTaskCompleted(Object data, String ASYNC_ID) {
        if (data != null) {
            if (ASYNC_ID.equalsIgnoreCase(GetShows.ASYNC_ID)) {
                lv.onRefreshComplete();
                List<Show> d = (List<Show>) data;
                if (d.size() <= 0) {
                    String title = getResources().getString(R.string.loader_title_request_result);
                    String msg = getResources().getString(R.string.result_listing_error);
                    String btnPosTitle = getResources().getString(R.string.dialog_button_ok);
                    Utility.showDialog(getActivity(), title, msg, btnPosTitle, null, false, null);
                } else {
                    adapter.setShows((ArrayList<Show>) d);
                    adapter.notifyDataSetChanged();
                }
            }

            if (ASYNC_ID.equalsIgnoreCase(Subscription.ASYNC_ID)) {
                Show show = (Show) data;
                base.showToast(show != null ? getString(R.string.message_subscription_successful) : getString(R.string.message_subscription_failure)
                        , Toast.LENGTH_LONG);
            }

        }
        dialog.dismiss();
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
        int count = new Select().from(Show.class).count();
        if (force || count <= 0 || adapter.getCount() <= 0) {
            GetShows async = new GetShows(getActivity(), force);
            async.asyncTaskListener = this; //set this class as observer to listen to asynctask events
            async.execute();
        }
    }


    @Override
    public void onTaskWorking(String ASYNC_ID) {
        dialog.show();
    }

    @Override
    public void onTaskProgressUpdate(int progress, String ASYNC_ID) {
        dialog.setProgress(progress);
    }

    @Override
    public void onTaskProgressMax(int max, String ASYNC_ID) {
        dialog.setMax(max);
    }

    @Override
    public void onTaskUpdateMessage(String message, String ASYNC_ID) {
        dialog.setMessage(message);
    }

    @Override
    public void onTaskError(Exception e, String ASYNC_ID) {
        dialog.setMessage("Error: " + e.getMessage());
    }

}
