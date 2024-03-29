package com.hamaksoftware.tvbrowser.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.hamaksoftware.tvbrowser.R;
import com.hamaksoftware.tvbrowser.activities.Main;
import com.hamaksoftware.tvbrowser.adapters.EpisodeAdapter;
import com.hamaksoftware.tvbrowser.asynctasks.SearchByKeyword;
import com.hamaksoftware.tvbrowser.asynctasks.SendTorrent;
import com.hamaksoftware.tvbrowser.utils.Utility;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import java.util.ArrayList;
import java.util.List;

import fr.castorflex.android.circularprogressbar.CircularProgressDrawable;
import info.besiera.api.APIRequestException;
import info.besiera.api.models.Episode;

public class SearchFragment extends Fragment implements IAsyncTaskListener {


    protected PullToRefreshListView lv;
    protected EpisodeAdapter adapter;
    protected Main base;

    private ProgressDialog dialog;
    private String query;

    AdapterView.OnItemClickListener itemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            int headerCount = 0;

            if (lv != null) {
                headerCount = lv.getRefreshableView().getHeaderViewsCount();
            }

            final Episode row = adapter.listings.get(position - headerCount);
            final CharSequence[] items = {getString(R.string.dialog_open), getString(R.string.dialog_send), getString(R.string.dialog_view)};

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(row.getTitle());
            builder.setItems(items, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    if (items[item].equals(getString(R.string.dialog_open))) {

                        final List<String> links = row.getLinks();
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
                                    try {
                                        Utility.getInstance(getActivity()).markDownload(row.getTitle(), Integer.parseInt(row.getShow_id()));
                                    } catch (NumberFormatException e) {
                                        Log.e("markdownload", e.getMessage());
                                    }
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
                            try {
                                Utility.getInstance(getActivity()).markDownload(row.getTitle(), Integer.parseInt(row.getShow_id()));
                            } catch (NumberFormatException e) {
                                Log.e("markdownload", e.getMessage());
                            }
                            SendTorrent send = new SendTorrent(getActivity(), row);
                            send.asyncTaskListener = SearchFragment.this;
                            send.execute();
                        }
                    }

                    if (items[item].equals(getString(R.string.dialog_view))) {
                        Bundle args = new Bundle();
                        args.putInt("show_id", Integer.parseInt(row.getShow_id()));
                        base.launchFragment(R.string.fragment_tag_show_detail, args, false);
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
            this.query = query;
            onActivityDrawerClosed();
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
        dialog.setIndeterminateDrawable(new CircularProgressDrawable
                .Builder(getActivity())
                .colors(getResources().getIntArray(R.array.gplus_colors))
                .sweepSpeed(1f)
                .style(CircularProgressDrawable.Style.NORMAL).build());

        lv = (PullToRefreshListView) rootView.findViewById(R.id.latest_list_feed);
        lv.getRefreshableView().setOnItemClickListener(itemClick);
        if (adapter == null) {
            adapter = new EpisodeAdapter(getActivity(), new ArrayList<Episode>(0));
        }

        lv.getRefreshableView().setAdapter(adapter);


        query = getArguments().getString("query");

        base.invalidateOptionsMenu();

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if (adapter.listings.size() <= 0) {
            adapter.listings.clear();
            onActivityDrawerClosed();
        }
    }

    @Override
    public void onTaskCompleted(Object data, String ASYNC_ID) {
        if (data != null) {
            if (ASYNC_ID.equalsIgnoreCase(SearchByKeyword.ASYNC_ID)) {
                List<Episode> d = (List<Episode>) data;
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
        SearchByKeyword async = new SearchByKeyword(query);
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
    public void onTaskError(final Exception e, String ASYNC_ID) {
        if (e instanceof APIRequestException) {
            final APIRequestException apiRequestException = (APIRequestException) e;
            if (ASYNC_ID.equalsIgnoreCase(SearchByKeyword.ASYNC_ID)) {
                base.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        base.showToast("Error: " + apiRequestException.getStatus().toString(), Toast.LENGTH_SHORT);
                        dialog.dismiss();
                    }
                });
            }
        } else {
            if (ASYNC_ID.equalsIgnoreCase(SearchByKeyword.ASYNC_ID)) {
                base.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        base.showToast("Error: " + e.getMessage(), Toast.LENGTH_SHORT);
                        dialog.dismiss();
                    }
                });
            }
        }

    }

}
