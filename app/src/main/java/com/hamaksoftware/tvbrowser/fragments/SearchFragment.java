package com.hamaksoftware.tvbrowser.fragments;

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
import android.widget.ListView;
import android.widget.Toast;

import com.hamaksoftware.tvbrowser.R;
import com.hamaksoftware.tvbrowser.activities.Main;
import com.hamaksoftware.tvbrowser.adapters.EpisodeAdapter;
import com.hamaksoftware.tvbrowser.asynctasks.Search;
import com.hamaksoftware.tvbrowser.asynctasks.SendTorrent;
import com.hamaksoftware.tvbrowser.models.Episode;
import com.hamaksoftware.tvbrowser.utils.ShowHandler;
import com.hamaksoftware.tvbrowser.utils.Utility;

import java.util.ArrayList;

public class SearchFragment extends Fragment implements IAsyncTaskListener {


    protected ListView lv;
    protected EpisodeAdapter adapter;
    protected View footer;
    protected Main base;

    private ProgressDialog dialog;
    private String query;
    private boolean byId;
    private boolean force;

    AdapterView.OnItemClickListener itemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final Episode row = adapter.listings.get(position);
            final CharSequence[] items = {getString(R.string.dialog_open), getString(R.string.dialog_send), getString(R.string.dialog_view)};

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(row.title);
            builder.setItems(items, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    if (items[item].equals(getString(R.string.dialog_open))) {

                        final ArrayList<String> links = (ArrayList<String>) row.links;
                        AlertDialog.Builder linkbuilder = new AlertDialog.Builder(getActivity());
                        final String[] slinks = new String[links.size()];
                        int ctr = 0;
                        for (String link : links) {
                            if (link.toLowerCase().contains("magnet")) {
                                slinks[ctr] = "Magnet Link";
                            } else {
                                slinks[ctr] = "Link # " + (ctr + 1);
                            }

                            ctr++;
                        }

                        linkbuilder.setItems(slinks, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int pos) {
                                try {
                                    Utility.getInstance(getActivity()).markDownload(row.title, row.showId);
                                    Intent i = new Intent(Intent.ACTION_VIEW);
                                    i.setData(Uri.parse(links.get(pos)));
                                    startActivity(i);
                                } catch (ActivityNotFoundException e) {
                                    Utility.showDialog(getActivity(), getString(R.string.dialog_title_info),
                                            getString(R.string.unknown_handler), getString(R.string.dialog_button_ok),
                                            getString(R.string.dialog_button_close), true, null);
                                }
                            }
                        });

                        AlertDialog linkalert = linkbuilder.create();
                        linkalert.show();

                    }

                    if (items[item].equals(getString(R.string.dialog_send))) {
                        if (base.pref.getClientName().length() < 2) {
                            base.showToast("Set up a profile for a torrent client in the settings first.", Toast.LENGTH_LONG);
                        } else {
                            Utility.getInstance(getActivity()).markDownload(row.title, row.showId);
                            SendTorrent send = new SendTorrent(getActivity(), row);
                            send.asyncTaskListener = SearchFragment.this;
                            send.execute();
                        }
                    }

                    if (items[item].equals(getString(R.string.dialog_view))) {
                        base = (Main) getActivity();
                        ShowHandler showHandler = new ShowHandler(getActivity());
                        if (showHandler.getCount() > 0) {
                            Bundle args = new Bundle();
                            args.putInt("show_id", row.showId);
                            base.launchFragment(R.string.fragment_tag_show_detail, args, false);
                        } else {
                            base.showToast("Please refresh shows section first.", Toast.LENGTH_LONG);
                        }
                    }

                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    };


    @Override
    public void onResume() {
        super.onResume();
        base.currentFragmentTag = R.string.fragment_tag_search;
        base.invalidateOptionsMenu();
    }

    public void search(String query) {
        if (query == null || query.equals("") || !query.equalsIgnoreCase(this.query)) {
            this.force = true;
            this.query = query;
            this.byId = false;
            onActivityDrawerClosed();
            this.force = false;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.home, container, false);
        base = (Main) getActivity();
        base.toggleHintLayout(false);
        //footer = inflater.inflate(R.layout.footer, null,false);


        if (dialog == null) dialog = new ProgressDialog(getActivity());
        dialog.setIndeterminate(true);
        dialog.setMessage(getString(R.string.loader_searching));

        lv = (ListView) rootView.findViewById(R.id.latest_list_feed);
        lv.setOnItemClickListener(itemClick);
        if (adapter == null) {
            adapter = new EpisodeAdapter(getActivity(), new ArrayList<Episode>(0));
        }

        lv.setAdapter(adapter);


        query = getArguments().getString("query");
        byId = getArguments().getBoolean("byId");

        base.invalidateOptionsMenu();

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if (force || adapter.listings.size() <= 0) {
            adapter.listings.clear();
            onActivityDrawerClosed();
        }
    }

    @Override
    public void onTaskCompleted(Object data, String ASYNC_ID) {
        if (data != null) {
            if (ASYNC_ID.equalsIgnoreCase(Search.ASYNC_ID)) {
                ArrayList<Episode> d = (ArrayList<Episode>) data;
                if (d.size() > 0) {
                    adapter.listings = d;
                    adapter.notifyDataSetChanged();
                } else {
                    base.showToast("No episodes found for " + query + ". Try again.", Toast.LENGTH_LONG);
                }
            }

            if (ASYNC_ID.equalsIgnoreCase(SendTorrent.ASYNC_ID)) {
                boolean success = (Boolean) data;
                base.showToast(success ? "Torrent sent successfully." : "Warning: Failed to send torrent.", Toast.LENGTH_LONG);
            }
        } else {
            base.showToast("Sorry, an unexpected error has occured. Try again.", Toast.LENGTH_LONG);
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

    public void onActivityDrawerClosed() {
        Search async = new Search(getActivity(), query, byId);
        async.asyncTaskListener = this; //set this class as observer to listen to asynctask events
        async.execute();
    }


    @Override
    public void onTaskWorking(String ASYNC_ID) {
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
        base.showToast("An error has occured while doing the search.", Toast.LENGTH_SHORT);
        dialog.dismiss();

    }

}
