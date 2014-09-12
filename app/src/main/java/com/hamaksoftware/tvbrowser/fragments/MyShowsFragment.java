package com.hamaksoftware.tvbrowser.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.hamaksoftware.tvbrowser.R;
import com.hamaksoftware.tvbrowser.activities.Main;
import com.hamaksoftware.tvbrowser.adapters.MyShowAdapter;
import com.hamaksoftware.tvbrowser.asynctasks.GetMyShows;
import com.hamaksoftware.tvbrowser.asynctasks.UnSubscribe;
import com.hamaksoftware.tvbrowser.utils.Utility;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;

import java.util.ArrayList;
import java.util.List;

import info.besiera.api.APIRequestException;
import info.besiera.api.models.Subscription;

public class MyShowsFragment extends Fragment implements IAsyncTaskListener {


    public MyShowAdapter adapter;
    protected PullToRefreshGridView lv;
    protected Main base;

    AdapterView.OnItemClickListener itemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final Subscription subscription = adapter.subscriptions.get(position);

            final CharSequence[] items = {getString(R.string.dialog_open),getString(R.string.dialog_copy),
                    getString(R.string.dialog_unsubscribe), getString(R.string.dialog_view)};

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(subscription.getShow().getTitle());
            builder.setItems(items, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {

                    if (items[item].equals(getString(R.string.dialog_open))) {
                        AlertDialog.Builder linkbuilder = new AlertDialog.Builder(getActivity());
                        final String[] _items = new String[2];
                        _items[0] = getString(R.string.dialog_getlink);
                        _items[1] = getString(R.string.dialog_gethdlink);
                        final String[] slinks = new String[2];
                        slinks[0] = subscription.getShow().getLink();
                        slinks[1] = subscription.getShow().getHdlink();
                        linkbuilder.setItems(_items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int pos) {
                                if(slinks[pos] == null){
                                    base.showToast(getString(R.string.message_empty_link), Toast.LENGTH_LONG);
                                }else{
                                    try {
                                        Utility.getInstance(getActivity()).markDownload(subscription.getShow().getTitle(), subscription.getShow().getShowId());
                                        Intent i = new Intent(Intent.ACTION_VIEW);
                                        i.setData(Uri.parse(slinks[pos]));
                                        startActivity(i);
                                    } catch (ActivityNotFoundException e) {
                                        Utility.showDialog(getActivity(), getString(R.string.dialog_title_info),
                                                getString(R.string.unknown_handler), getString(R.string.dialog_button_ok),
                                                getString(R.string.dialog_button_close), true, null);
                                    }
                                }
                            }
                        });

                        AlertDialog linkalert = linkbuilder.create();
                        linkalert.show();
                    }

                    if (items[item].equals(getString(R.string.dialog_copy))) {
                        AlertDialog.Builder linkbuilder = new AlertDialog.Builder(getActivity());
                        final String[] _items = new String[2];
                        _items[0] = getString(R.string.dialog_getlink);
                        _items[1] = getString(R.string.dialog_gethdlink);
                        final String[] slinks = new String[2];
                        slinks[0] = subscription.getShow().getLink();
                        slinks[1] = subscription.getShow().getHdlink();
                        linkbuilder.setItems(_items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int pos) {
                                if(slinks[pos] == null){
                                    base.showToast(getString(R.string.message_empty_link), Toast.LENGTH_LONG);
                                }else{
                                    Utility.getInstance(getActivity()).copyTextToClipBoard(slinks[pos]);
                                    base.showToast(getString(R.string.message_copy_text_successful), Toast.LENGTH_LONG);
                                }
                            }
                        });

                        AlertDialog linkalert = linkbuilder.create();
                        linkalert.show();
                    }

                    if (items[item].equals(getString(R.string.dialog_view))) {
                        base = (Main) getActivity();
                        Bundle args = new Bundle();
                        args.putInt("show_id", subscription.getShow().getShowId());
                        base.launchFragment(R.string.fragment_tag_show_detail, args, false);
                    }

                    if (items[item].equals(getString(R.string.dialog_unsubscribe))) {
                        UnSubscribe s = new UnSubscribe(getActivity(), subscription.getShow().getShowId());
                        s.asyncTaskListener = MyShowsFragment.this;
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
        lv.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<GridView>() {
            @Override
            public void onRefresh(PullToRefreshBase<GridView> refreshView) {
                lv.setRefreshing();
                onActivityDrawerClosed();
            }
        });
        lv.getRefreshableView().setOnItemClickListener(itemClick);


        if (adapter == null) {
            adapter = new MyShowAdapter(getActivity());
            adapter.setSubscriptions(new ArrayList<Subscription>(0));
        }

        lv.getRefreshableView().setAdapter(adapter);

        base.invalidateOptionsMenu();

        return rootView;
    }


    @Override
    public void onResume() {
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
    public void onTaskCompleted(Object data, String ASYNC_ID) {
        if (data != null) {
            if (ASYNC_ID.equalsIgnoreCase(GetMyShows.ASYNC_ID)) {
                lv.onRefreshComplete();
                List<Subscription> d = (List<Subscription>) data;
                if (d.size() <= 0) {
                    String title = getResources().getString(R.string.loader_title_request_result);
                    String btnPosTitle = getResources().getString(R.string.dialog_button_ok);
                    Utility.showDialog(getActivity(), title, "You are not following any Shows yet. Go to Shows and subscribe one.", btnPosTitle, null, false, null);
                } else {
                    adapter.setSubscriptions((ArrayList<Subscription>) d);
                    adapter.notifyDataSetChanged();
                }
            }

            if (ASYNC_ID.equalsIgnoreCase(UnSubscribe.ASYNC_ID)) {
                Boolean success = (Boolean) data;
                base.showToast(success ? getString(R.string.message_unsubscribe_successful) : getString(R.string.message_unsubscribe_failure)
                        , Toast.LENGTH_LONG);
                GetMyShows async = new GetMyShows(getActivity());
                async.asyncTaskListener = this;
                async.execute();
            }


        }

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
        onActivityDrawerClosed();
        force = false;
    }

    public void onActivityDrawerClosed() {
        GetMyShows async = new GetMyShows(getActivity());
        async.asyncTaskListener = this;
        async.execute();
    }


    @Override
    public void onTaskWorking(String ASYNC_ID) {

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
        if (ASYNC_ID.equalsIgnoreCase(GetMyShows.ASYNC_ID)) {
            base.runOnUiThread(new Runnable() {
                public void run() {
                    APIRequestException ex = (APIRequestException) e;
                    Utility.showDialog(getActivity(), null, ex.getStatus().getDescription(), "Okay", null, false, null);
                    lv.onRefreshComplete();
                }
            });
        }

    }

}
