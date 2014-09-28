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
import com.hamaksoftware.tvbrowser.adapters.RSSAdapter;
import com.hamaksoftware.tvbrowser.asynctasks.GetOtherSource;
import com.hamaksoftware.tvbrowser.asynctasks.SendTorrent;
import com.hamaksoftware.tvbrowser.models.RSSItem;
import com.hamaksoftware.tvbrowser.utils.Utility;

import java.util.ArrayList;

import fr.castorflex.android.circularprogressbar.CircularProgressDrawable;
import info.besiera.api.models.Episode;

public class OtherSourceFragment extends Fragment implements IAsyncTaskListener {

    protected ListView lv;
    protected RSSAdapter adapter;
    protected Main base;


    private ProgressDialog dialog;
    public boolean force;
    public String uri;

    AdapterView.OnItemClickListener itemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final RSSItem row = adapter.items.get(position);
            final CharSequence[] items = {getString(R.string.dialog_open), getString(R.string.dialog_send)};

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(row.title);
            builder.setItems(items, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    if (items[item].equals(getString(R.string.dialog_open))) {
                        final ArrayList<String> links = new ArrayList<String>(0);
                        links.add(row.itemlink == null || row.itemlink.equals("") ? row.altLInk : row.itemlink);
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

                            Episode ep = new Episode();
                            ArrayList<String> links = new ArrayList<String>(0);
                            links.add(row.altLInk == null || row.altLInk.equals("") ? row.itemlink : row.altLInk);
                            links.add(row.altLInk);
                            ep.setLinks(links);
                            SendTorrent send = new SendTorrent(getActivity(), ep);
                            send.asyncTaskListener = OtherSourceFragment.this;
                            send.execute();

                        }
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
        final View rootView = inflater.inflate(R.layout.feeds, container, false);
        base = (Main) getActivity();
        base.toggleHintLayout(false);
        lv = (ListView) rootView.findViewById(R.id.rssfeed);


        dialog = new ProgressDialog(getActivity());
        dialog.setIndeterminate(true);
        dialog.setIndeterminateDrawable(new CircularProgressDrawable
                .Builder(getActivity())
                .colors(getResources().getIntArray(R.array.gplus_colors))
                .sweepSpeed(1f)
                .style(CircularProgressDrawable.Style.NORMAL).build());

        if (adapter == null) {
            adapter = new RSSAdapter(getActivity());
        }

        uri = getArguments().getString("uri");

        lv.setAdapter(adapter);
        lv.setOnItemClickListener(itemClick);

        base.invalidateOptionsMenu();

        return rootView;
    }

    public void refreshView(){

    }

    @Override
    public void onResume() {
        super.onResume();
        base.currentFragmentTag = R.string.fragment_tag_rss;
        base.invalidateOptionsMenu();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if (force || adapter.items.size() <= 0) {
            adapter.items.clear();
            onActivityDrawerClosed();
        }
    }

    @Override
    public void onTaskCompleted(Object data, String ASYNC_ID) {
        if (data != null) {
            if (ASYNC_ID.equalsIgnoreCase(GetOtherSource.ASYNC_ID)) {
                ArrayList<RSSItem> d = (ArrayList<RSSItem>) data;
                if (d.size() <= 0) {
                    String title = getResources().getString(R.string.loader_title_request_result);
                    String msg = getResources().getString(R.string.result_listing_error);
                    String btnPosTitle = getResources().getString(R.string.dialog_button_ok);
                    Utility.showDialog(getActivity(), title, msg, btnPosTitle, null, false, null);
                } else {
                    adapter.items = d;
                    adapter.notifyDataSetChanged();
                }
            }

            if (ASYNC_ID.equalsIgnoreCase(SendTorrent.ASYNC_ID)) {
                boolean success = (Boolean) data;
                base.showToast(success ? "Torrent sent successfully." : "Warning: Failed to send torrent.", Toast.LENGTH_LONG);
            }
        }


        dialog.dismiss();
        force = false;
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
        if (force) adapter.items.clear();
        GetOtherSource async = new GetOtherSource(getActivity(), uri);
        async.asyncTaskListener = this; //set this class as observer to listen to asynctask events
        async.execute();

    }


    @Override
    public void onTaskWorking(String ASYNC_ID) {
        dialog.setMessage(getString(R.string.loader_working));
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

    }

}
