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
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.hamaksoftware.tvbrowser.R;
import com.hamaksoftware.tvbrowser.activities.Main;
import com.hamaksoftware.tvbrowser.adapters.EpisodeAdapter;
import com.hamaksoftware.tvbrowser.asynctasks.Search;
import com.hamaksoftware.tvbrowser.asynctasks.SendTorrent;
import com.hamaksoftware.tvbrowser.asynctasks.Subscription;
import com.hamaksoftware.tvbrowser.models.Episode;
import com.hamaksoftware.tvbrowser.models.Show;
import com.hamaksoftware.tvbrowser.utils.Utility;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class ShowDetailsFragment extends Fragment implements IAsyncTaskListener {

    public Show show;
    public boolean force;
    protected PullToRefreshListView lv;
    protected View footer;
    protected Main base;


    AdapterView.OnItemClickListener itemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            int headerCount = 0;

            if(lv != null){
                headerCount = lv.getRefreshableView().getHeaderViewsCount();
            }

            final Episode row = adapter.listings.get(position - headerCount);
            final CharSequence[] items = {getString(R.string.dialog_open), getString(R.string.dialog_send)};

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
    private TextView downloadCount;
    private TextView subscriberCount;
    private TextView playCount;
    private TextView watcherCount;
    private TextView rating;
    private TextView showDescription;
    private Button status;
    private EpisodeAdapter adapter;
    private ProgressDialog dialog;

    public static String convertToFancyString(int val) {
        final double KILO = 1000D;
        final double MEGA = KILO * KILO;
        DecimalFormat df = new DecimalFormat("#.##");
        if (val >= KILO && val < MEGA) {
            return df.format(val / KILO) + "K";
        } else if (val >= MEGA) {
            return df.format(val / MEGA) + "M";
        } else {
            return val + "";
        }

    }

    public void setShowDetails(int showId) {
        show = new Select().from(Show.class).where("showId=?", showId)
                .executeSingle();
        base.setTitle(show.title);
        status.setText(show.isSubscribed ? "UNSUBSCRIBE" : "SUBSCRIBE");
        if (show.isSubscribed) {
            status.setBackgroundColor(getResources().getColor(R.color.torrent_completed));
            status.setTextColor(Color.WHITE);
        } else {
            status.setBackgroundColor(getResources().getColor(R.color.torrent_progress));
            status.setTextColor(Color.WHITE);
        }
    }

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

        dialog = new ProgressDialog(getActivity());
        dialog.setIndeterminate(true);

        Bundle payload = getArguments();
        show = new Select().from(Show.class).where("showId=?", payload.getInt("show_id"))
                .executeSingle();

        ImageView img = (ImageView) headerView.findViewById(R.id.poster);
        ImageLoader.getInstance().displayImage("http://hamaksoftware.com/myeztv/tvimg/" + show.showId + ".jpg", img);


        if (show == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle("Update Needed").setMessage("TV Shows need to update. Click OK to proceed.")
                    .setCancelable(false)
                    .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Bundle param = new Bundle();
                            param.putBoolean("force", true);
                            base.launchFragment(R.string.fragment_tag_shows, param, true);
                        }
                    });

            AlertDialog alert = builder.create();
            alert.show();
        } else {

            base.setTitle(show.title);
            /*
            downloadCount = (TextView) rootView.findViewById(R.id.show_detail_downcount);
            subscriberCount = (TextView) rootView.findViewById(R.id.show_detail_subcount);
            playCount = (TextView) rootView.findViewById(R.id.show_play_count);
            watcherCount = (TextView) rootView.findViewById(R.id.show_play_watchers);
            rating = (TextView) rootView.findViewById(R.id.show_rating);
            showDescription = (TextView) rootView.findViewById(R.id.show_description);
            */

            status = (Button) rootView.findViewById(R.id.show_detail_status);

            status.setText(show.isSubscribed ? "UNSUBSCRIBE" : "SUBSCRIBE");
            if (show.isSubscribed) {
                status.setBackgroundColor(getResources().getColor(R.color.torrent_completed));
                status.setTextColor(Color.WHITE);
            } else {
                status.setBackgroundColor(getResources().getColor(R.color.torrent_progress));
                status.setTextColor(Color.WHITE);
            }

            status.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Button btn = (Button) view;
                    Subscription subscription = new Subscription(getActivity(), show);
                    subscription.asyncTaskListener = ShowDetailsFragment.this;
                    subscription.isSubscribe = !show.isSubscribed;
                    subscription.execute();
                }
            });

            base.invalidateOptionsMenu();
        }


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
        if (show == null) return;
        /*
        if (show.showId != 187) {
            GetShowDetails details = new GetShowDetails(getActivity(), show);
            details.asyncTaskListener = this;
            details.execute();
        }
        */

        /*
        GetSubscriberCount info = new GetSubscriberCount(getActivity(),show);
        info.asyncTaskListener = this;
        info.execute();
        */

        Search search = new Search(getActivity(), show.showId + "", true);
        search.asyncTaskListener = this;
        search.execute();
    }


    @Override
    public void onTaskCompleted(Object data, String ASYNC_ID) {
        if (ASYNC_ID.equalsIgnoreCase(Search.ASYNC_ID)) {
            if (data != null) {
                ArrayList<Episode> items = (ArrayList<Episode>) data;
                adapter.listings = items;
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getActivity(), "No Episodes for " + show.title + " found.", Toast.LENGTH_LONG).show();
            }

            dialog.dismiss();
        }

        /*
        if (ASYNC_ID.equalsIgnoreCase(GetShowDetails.ASYNC_ID)) {
            if (data != null) {
                String response = (String) data;
                try {
                    JSONObject root = new JSONObject(response);
                    showDescription.setText(root.getString("overview"));

                    JSONObject ratings = root.getJSONObject("ratings");
                    rating.setText(ratings.getInt("percentage") + "%");

                    JSONObject stats = root.getJSONObject("stats");
                    int plays = stats.getInt("plays");
                    int watchers = stats.getInt("watchers");
                    playCount.setText(convertToFancyString(plays) + "\nplays");
                    watcherCount.setText(convertToFancyString(watchers) + "\nwatchers");

                    Random r = new Random();
                    int i1 = r.nextInt(20 - 1) + 1;
                    downloadCount.setText(convertToFancyString(plays + i1) + "\ndownloads");

                    int i2 = r.nextInt(20 - 1) + 1;
                    subscriberCount.setText(convertToFancyString(watchers + i2) + "\nsubscribers");


                } catch (Exception e) {
                    Log.e("async-info", e.getMessage());
                }
            }

        }
        */

        if (ASYNC_ID.equalsIgnoreCase(SendTorrent.ASYNC_ID)) {
            boolean success = (Boolean) data;
            base.showToast(success ? "Torrent sent successfully." : "Warning: Failed to send torrent.", Toast.LENGTH_LONG);
        }

        if (ASYNC_ID.equalsIgnoreCase(Subscription.ASYNC_ID)) {
            if (data != null) {
                Show s = (Show) data;
                if (s != null) {
                    /*
                    if (show.isSubscribed) {
                        show.isSubscribed = false;
                        status.setBackgroundColor(getResources().getColor(R.color.torrent_progress));
                        status.setTextColor(Color.WHITE);
                        status.setText("SUBSCRIBE");
                    } else {
                        status.setBackgroundColor(getResources().getColor(R.color.torrent_completed));
                        status.setTextColor(Color.WHITE);
                        show.isSubscribed = true;
                        status.setText("UNSUBSCRIBE");
                    }
                    base.sh.updateShow(show);
                    */
                    setShowDetails(s.showId);
                }
            }
        }

    }

    @Override
    public void onTaskWorking(String ASYNC_ID) {
        if (ASYNC_ID.equalsIgnoreCase(Search.ASYNC_ID)) {
            dialog.setMessage("Searching episodes for " + show.title);
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
        // TODO Auto-generated method stub

    }

}
