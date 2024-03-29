package com.hamaksoftware.tvbrowser.fragments;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import com.hamaksoftware.tvbrowser.R;
import com.hamaksoftware.tvbrowser.activities.Main;
import com.hamaksoftware.tvbrowser.adapters.DownloadAdapter;
import com.hamaksoftware.tvbrowser.asynctasks.GetTorrents;
import com.hamaksoftware.tvbrowser.asynctasks.SendTorrentAction;
import com.hamaksoftware.tvbrowser.torrentcontroller.TorrentAction;
import com.hamaksoftware.tvbrowser.torrentcontroller.TorrentItem;
import com.hamaksoftware.tvbrowser.torrentcontroller.ViewFilter;
import com.hamaksoftware.tvbrowser.utils.AppPref;
import com.hamaksoftware.tvbrowser.utils.Utility;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

public class DownloadsFragment extends Fragment implements IAsyncTaskListener {


    final Handler handler = new Handler();
    public DownloadAdapter adapter;
    protected ListView lv;
    protected Main base;
    private AppPref pref;
    private ViewFilter viewFilter = ViewFilter.ALL;
    View.OnClickListener chkboxOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            resetCheckboxes();
            CheckBox chk = (CheckBox) view;
            chk.setChecked(true);
            if (chk.getText().toString().equalsIgnoreCase(getString(R.string.chk_download_running))) {
                viewFilter = ViewFilter.RUNNING;
            } else if (chk.getText().toString().equalsIgnoreCase(getString(R.string.chk_download_completed))) {
                viewFilter = ViewFilter.COMPLETED;
            } else {
                viewFilter = ViewFilter.ALL;
            }
        }
    };
    private CheckBox downloadRunning;
    private CheckBox downloadCompleted;
    private CheckBox downloadAll;
    private Runnable autoRun = new Runnable() {
        public void run() {
            GetTorrents async = new GetTorrents(getActivity(), viewFilter);
            async.asyncTaskListener = DownloadsFragment.this;
            async.execute();
            //int ref = pref.getRefreshInterval();
            handler.postDelayed(this, 1 * 1000);
        }
    };
    AdapterView.OnItemClickListener itemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            handler.removeCallbacks(autoRun);
            final TorrentItem row = adapter.items.get(position);
            final CharSequence[] items = {
                    getString(R.string.download_pause),
                    getString(R.string.download_resume),
                    getString(R.string.download_remove),
                    getString(R.string.download_remove_data)
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(row.getName());
            builder.setItems(items, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    SendTorrentAction action = new SendTorrentAction(getActivity());
                    action.hashes = adapter.getHash(row);

                    if (items[item].equals(getString(R.string.download_pause))) {
                        action.action = TorrentAction.PAUSE;
                    }

                    if (items[item].equals(getString(R.string.download_resume))) {
                        action.action = TorrentAction.START;
                    }

                    if (items[item].equals(getString(R.string.download_remove))) {
                        action.action = TorrentAction.REMOVE;
                    }

                    if (items[item].equals(getString(R.string.download_remove_data))) {
                        action.action = TorrentAction.REMOVEDATA;
                    }

                    action.asyncTaskListener = DownloadsFragment.this;
                    action.execute();


                }

            });
            AlertDialog alert = builder.create();
            alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    autoRun.run();
                }
            });

            alert.show();

        }
    };

    private void resetCheckboxes() {
        if (downloadAll != null && downloadCompleted != null && downloadRunning != null) {
            downloadAll.setChecked(false);
            downloadRunning.setChecked(false);
            downloadCompleted.setChecked(false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.downloads, container, false);
        base = (Main) getActivity();
        base.toggleHintLayout(false);
        if (pref == null) {
            pref = new AppPref(getActivity());
        }


        downloadRunning = (CheckBox) rootView.findViewById(R.id.download_running);
        downloadCompleted = (CheckBox) rootView.findViewById(R.id.download_completed);
        downloadAll = (CheckBox) rootView.findViewById(R.id.download_all);

        downloadRunning.setOnClickListener(chkboxOnClick);
        downloadCompleted.setOnClickListener(chkboxOnClick);
        downloadAll.setOnClickListener(chkboxOnClick);


        final ActionBar actionBar = base.getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);


        final String[] actions = {getString(R.string.download_select_action), getString(R.string.download_pause_all), getString(R.string.download_resume_all), getString(R.string.download_remove_all_completed)};
        final ArrayAdapter<String> dropdownAdapter = new ArrayAdapter<String>(actionBar.getThemedContext(),
                android.R.layout.simple_spinner_item, android.R.id.text1,
                actions);
        dropdownAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        actionBar.setListNavigationCallbacks(dropdownAdapter, new ActionBar.OnNavigationListener() {
            @Override
            public boolean onNavigationItemSelected(int itemPosition, long itemId) {
                if (adapter.getCount() > 0) {
                    String selected = dropdownAdapter.getItem(itemPosition);
                    SendTorrentAction action = new SendTorrentAction(getActivity());
                    action.hashes = adapter.getHashes();
                    if (selected.equalsIgnoreCase(getString(R.string.download_pause_all))) {
                        action.action = TorrentAction.PAUSE;
                    }

                    if (selected.equalsIgnoreCase(getString(R.string.download_resume_all))) {
                        action.action = TorrentAction.START;
                    }

                    if (selected.equalsIgnoreCase(getString(R.string.download_remove_all_completed))) {
                        action.hashes = adapter.getCompletedHashes();
                        action.action = TorrentAction.REMOVE;
                    }

                    action.asyncTaskListener = DownloadsFragment.this;
                    action.execute();

                }

                return true;
            }
        });


        lv = (ListView) rootView.findViewById(R.id.ldownloads_list);

        lv.setOnItemClickListener(itemClick);

        if (adapter == null) {
            adapter = new DownloadAdapter(getActivity());
        }

        lv.setAdapter(adapter);
        base.invalidateOptionsMenu();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        autoRun.run();
        base.currentFragmentTag = R.string.fragment_tag_downloads;
        base.invalidateOptionsMenu();
        base.getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(autoRun);
        base.getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        //onActivityDrawerClosed();
    }

    @Override
    public void onTaskCompleted(Object data, String ASYNC_ID) {
        if (data != null) {
            if (ASYNC_ID.equalsIgnoreCase(GetTorrents.ASYNC_ID)) {
                ArrayList<TorrentItem> d = (ArrayList<TorrentItem>) data;
                base.setTitle(getString(R.string.arrow_down) + Utility.getFancySize(adapter.getTotalDownloadSpeed()) + " " +
                        getString(R.string.arrow_up) + Utility.getFancySize(adapter.getTotalUploadSpeed()));
                adapter.items = (ArrayList<TorrentItem>) d;
                adapter.notifyDataSetChanged();
            }

            if (ASYNC_ID.equalsIgnoreCase(SendTorrentAction.ASYNC_ID)) {
                Boolean success = (Boolean) data;
                base.showToast(success ? getString(R.string.message_request_success) : getString(R.string.message_request_failure)
                        , Toast.LENGTH_LONG);
            }

        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        base.currentFragmentTag = 0;
        base.invalidateOptionsMenu();
        base.setTitle(getString(R.string.app_name));
        base.toggleHintLayout(true);
    }

    public void onActivityDrawerClosed() {
        if (adapter.items.size() <= 0) {
            GetTorrents async = new GetTorrents(getActivity(), ViewFilter.ALL);
            async.asyncTaskListener = this; //set this class as observer to listen to asynctask events
            async.execute();
        }
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
    public void onTaskError(Exception e, String ASYNC_ID) {
        if (ASYNC_ID.equalsIgnoreCase(GetTorrents.ASYNC_ID)) {
            String msg = "";
            if (e instanceof IOException) {
                msg = "Warning: Unable to Connect to " + pref.getClientIPAddress();
            }

            if (e instanceof JSONException) {
                msg = "Warning: Unexpected response format from the client";
            }
            final String finalMsg = msg;
            base.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    base.showToast(finalMsg, Toast.LENGTH_SHORT);
                }
            });

        }
    }

}
