package com.hamaksoftware.tvbrowser.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.hamaksoftware.tvbrowser.R;
import com.hamaksoftware.tvbrowser.activities.Main;
import com.hamaksoftware.tvbrowser.adapters.EpisodeAdapter;
import com.hamaksoftware.tvbrowser.asynctasks.GetShowDetails;
import com.hamaksoftware.tvbrowser.asynctasks.GetShowSubscription;
import com.hamaksoftware.tvbrowser.asynctasks.SearchById;
import com.hamaksoftware.tvbrowser.asynctasks.SendTorrent;
import com.hamaksoftware.tvbrowser.asynctasks.Subscribe;
import com.hamaksoftware.tvbrowser.asynctasks.UnSubscribe;
import com.hamaksoftware.tvbrowser.utils.Utility;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.pollexor.Thumbor;

import java.util.ArrayList;
import java.util.List;

import info.besiera.api.APIRequestException;
import info.besiera.api.models.Episode;
import info.besiera.api.models.Show;

public class ShowDetailsFragment extends Fragment implements IAsyncTaskListener {
    public boolean force;
    public Button status;
    protected PullToRefreshListView lv;
    AdapterView.OnItemClickListener itemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            int headerCount = 0;

            if (lv != null) {
                headerCount = lv.getRefreshableView().getHeaderViewsCount();
            }

            final Episode row = adapter.listings.get(position - headerCount);
            final CharSequence[] items = {getString(R.string.dialog_open), getString(R.string.dialog_send)};

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
                                    Utility.getInstance(getActivity()).markDownload(row.getTitle(), Integer.parseInt(row.getShow_id()));
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
                            Utility.getInstance(getActivity()).markDownload(row.getTitle(), Integer.parseInt(row.getShow_id()));
                            SendTorrent send = new SendTorrent(getActivity(), row);
                            send.asyncTaskListener = ShowDetailsFragment.this;
                            send.execute();
                        }
                    }

                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    };
    protected View footer;
    protected Main base;
    private int showId;
    private EpisodeAdapter adapter;
    private ProgressDialog dialog;
    private ImageView poster;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.home, container, false);
        base = (Main) getActivity();
        base.toggleHintLayout(false);
        final View headerView = inflater.inflate(R.layout.show_detail_header, null, false);


        adapter = new EpisodeAdapter(getActivity(), new ArrayList<Episode>(0));

        lv = (PullToRefreshListView) rootView.findViewById(R.id.latest_list_feed);
        lv.getRefreshableView().setVerticalScrollBarEnabled(false);
        lv.getRefreshableView().setScrollBarStyle(ListView.SCROLLBARS_OUTSIDE_OVERLAY);
        lv.getRefreshableView().setOnItemClickListener(itemClick);
        lv.getRefreshableView().addHeaderView(headerView);

        lv.getRefreshableView().setAdapter(adapter);

        lv.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                lv.setRefreshing();
                onActivityDrawerClosed();
            }
        });


        dialog = new ProgressDialog(getActivity());
        dialog.setIndeterminate(true);

        Bundle payload = getArguments();
        showId = payload.getInt("show_id");

        poster = (ImageView) headerView.findViewById(R.id.poster);


        status = (Button) rootView.findViewById(R.id.show_detail_status);

        status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button btn = (Button) view;
                if (status.getText().toString().equalsIgnoreCase(getString(R.string.dialog_subscribe))) {
                    Subscribe subscribe = new Subscribe(getActivity(), showId);
                    subscribe.asyncTaskListener = ShowDetailsFragment.this;
                    subscribe.execute();
                } else {
                    UnSubscribe unSubscribe = new UnSubscribe(getActivity(), showId);
                    unSubscribe.asyncTaskListener = ShowDetailsFragment.this;
                    unSubscribe.execute();
                }
            }
        });
        base.invalidateOptionsMenu();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        base.currentFragmentTag = R.string.fragment_tag_show_detail;
        base.invalidateOptionsMenu();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if (force || adapter.listings.size() <= 0) {
            adapter.listings.clear();
            onActivityDrawerClosed();
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

        GetShowDetails details = new GetShowDetails(showId);
        details.asyncTaskListener = this;
        details.execute();

        SearchById searchById = new SearchById(showId);
        searchById.asyncTaskListener = this;
        searchById.execute();
    }


    @Override
    public void onTaskCompleted(Object data, String ASYNC_ID) {
        if (lv.isRefreshing()) lv.onRefreshComplete();
        if (ASYNC_ID.equalsIgnoreCase(GetShowDetails.ASYNC_ID)) {
            Show show = (Show) data;
            base.setTitle(show.getTitle());
            Thumbor thumbor = Thumbor.create("http://besiera.info:8888/");
            String url = thumbor.buildImage("http://besiera.info/apibackend/tvimg/" + show.getShowId() + ".jpg")
                    .resize(500, 500)
                    .smart()
                    .toUrl();

            System.out.println(url);
            ImageLoader.getInstance().displayImage(url, poster);
            GetShowSubscription getShowSubscription = new GetShowSubscription(getActivity(), show.getShowId());
            getShowSubscription.asyncTaskListener = this;
            getShowSubscription.execute();

        }

        if (ASYNC_ID.equalsIgnoreCase(GetShowSubscription.ASYNC_ID)) {
            Show show = (Show) data;
            if (show == null) {
                status.setText(getString(R.string.dialog_subscribe));
                status.setBackgroundColor(getResources().getColor(R.color.torrent_progress));
                status.setTextColor(Color.WHITE);
            } else {
                status.setText(getString(R.string.dialog_unsubscribe));
                status.setBackgroundColor(getResources().getColor(R.color.torrent_completed));
                status.setTextColor(Color.WHITE);
            }

        }

        if (ASYNC_ID.equalsIgnoreCase(SearchById.ASYNC_ID)) {
            if (data != null) {
                ArrayList<Episode> items = (ArrayList<Episode>) data;
                adapter.listings = items;
                adapter.notifyDataSetChanged();
            }
            dialog.dismiss();
        }


        if (ASYNC_ID.equalsIgnoreCase(SendTorrent.ASYNC_ID)) {
            boolean success = (Boolean) data;
            base.showToast(success ? "Torrent sent successfully." : "Warning: Failed to send torrent.", Toast.LENGTH_LONG);
        }

        if (ASYNC_ID.equalsIgnoreCase(Subscribe.ASYNC_ID)) {
            if (data != null) {
                Boolean success = (Boolean) data;
                if (success) {
                    status.setBackgroundColor(getResources().getColor(R.color.torrent_completed));
                    status.setTextColor(Color.WHITE);
                    status.setText(getString(R.string.dialog_unsubscribe));
                }
            }
        }

        if (ASYNC_ID.equalsIgnoreCase(UnSubscribe.ASYNC_ID)) {
            if (data != null) {
                Boolean success = (Boolean) data;
                if (success != null) {
                    status.setBackgroundColor(getResources().getColor(R.color.torrent_completed));
                    status.setTextColor(Color.WHITE);
                    status.setText(getString(R.string.dialog_subscribe));
                }
            }
        }


    }

    @Override
    public void onTaskWorking(String ASYNC_ID) {
        if (ASYNC_ID.equalsIgnoreCase(SearchById.ASYNC_ID)) {
            dialog.setMessage("Searching episodes...");
            dialog.show();
        }
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
        if (ASYNC_ID.equalsIgnoreCase(SearchById.ASYNC_ID)) {
            final APIRequestException apiRequestException = (APIRequestException) e;
            base.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    base.showMessage("Search Failed", apiRequestException.getStatus().getDescription() + ", Try again later.", "Okay");
                }
            });
        }

    }

}
