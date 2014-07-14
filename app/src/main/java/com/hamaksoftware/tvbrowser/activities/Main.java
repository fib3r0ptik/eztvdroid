package com.hamaksoftware.tvbrowser.activities;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.hamaksoftware.tvbrowser.R;
import com.hamaksoftware.tvbrowser.adapters.DrawerGroupAdapter;
import com.hamaksoftware.tvbrowser.fragments.DownloadsFragment;
import com.hamaksoftware.tvbrowser.fragments.LatestFragment;
import com.hamaksoftware.tvbrowser.fragments.MyShowsFragment;
import com.hamaksoftware.tvbrowser.fragments.OtherSourceFragment;
import com.hamaksoftware.tvbrowser.fragments.PrefFragment;
import com.hamaksoftware.tvbrowser.fragments.SearchFragment;
import com.hamaksoftware.tvbrowser.fragments.ShowDetailsFragment;
import com.hamaksoftware.tvbrowser.fragments.ShowsFragment;
import com.hamaksoftware.tvbrowser.models.Show;
import com.hamaksoftware.tvbrowser.utils.AppPref;
import com.hamaksoftware.tvbrowser.utils.ClientProfile;
import com.hamaksoftware.tvbrowser.utils.DBHandler;
import com.hamaksoftware.tvbrowser.utils.Utility;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Main extends Activity {
    /* private members */
    private DrawerLayout mDrawerLayout;
    private ExpandableListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private View hintHolder;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;


    private DrawerGroupAdapter listDrawerAdapter;
    private List<String> listDataHeader;
    private HashMap<String, List<String>> listDataChild;

    private DrawerChildClickListener drawerChildClickListener;
    public boolean forceRefresh;

    //private Fragment gridFragment;

    public AppPref pref;
    public ArrayList<ClientProfile> profiles;
    private FragmentManager fragmentManager;
    private int currentSelectedChildPos;
    private int currentSelectedParentPos;
    
    /* public members */

    public int currentPage;
    public int currentFragmentTag;

    public boolean hasRun = false;

    private void prepareListData() {

        //find me in res/values

        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        String[] categories = getResources().getStringArray(R.array.categories);
        for (int i = 0; i < categories.length; i++) {
            try {
                JSONArray jsonarr = new JSONArray(categories[i]);
                listDataHeader.add(jsonarr.getString(0));
                List<String> obj = new ArrayList<String>(0);
                for (int j = 1; j < jsonarr.length(); j++) {
                    obj.add(jsonarr.getString(j));
                    listDataChild.put(listDataHeader.get(i), obj);
                }
            } catch (JSONException e) {
                Log.e("prepareData", e.getMessage());
            }
        }

    }

    public void toggleHintLayout(boolean show) {
        if (hintHolder != null) {
            hintHolder.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DBHandler.getInstance(getApplicationContext()).close();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pref = new AppPref(getApplicationContext());

        Utility.getInstance(getApplicationContext()).registerInBackground();

        fragmentManager = getFragmentManager();

        mTitle = mDrawerTitle = getResources().getString(R.string.app_name);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ExpandableListView) findViewById(R.id.left_drawer);


        View v = getLayoutInflater().inflate(R.layout.sidebar_header, null);
        if (v != null) {
            mDrawerList.addHeaderView(v);
        }

        hintHolder = findViewById(R.id.hints);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        prepareListData();
        listDrawerAdapter = new DrawerGroupAdapter(this, listDataHeader, listDataChild);
        mDrawerList.setAdapter(listDrawerAdapter);


        drawerChildClickListener = new DrawerChildClickListener();

        mDrawerList.setOnGroupClickListener(new DrawerGroupItemClickListener());
        mDrawerList.setOnChildClickListener(drawerChildClickListener);
        mDrawerList.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int i) {
                LinearLayout parent = (LinearLayout) mDrawerList.getChildAt(i + 1);
                ImageView expand = (ImageView) parent.findViewById(R.id.drawer_collapse);
                expand.setImageResource(R.drawable.ic_action_expand);
            }
        });

        mDrawerList.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int i) {
                LinearLayout parent = (LinearLayout) mDrawerList.getChildAt(i + 1);
                ImageView collapse = (ImageView) parent.findViewById(R.id.drawer_collapse);
                collapse.setImageResource(R.drawable.ic_action_collapse);
            }
        });


        //drawerChildClickListener.onChildClick(null, null, 1, 1, 100000L);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .delayBeforeLoading(0)
                .cacheInMemory(false)
                .cacheOnDisk(true)
                .build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .defaultDisplayImageOptions(options)
                .build();
        ImageLoader.getInstance().init(config);


        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {

            public void onDrawerClosed(View view) {
                if (currentFragmentTag == R.string.fragment_tag_latest) {
                    Bundle args = new Bundle();
                    args.putBoolean("force", forceRefresh);
                    launchFragment(R.string.fragment_tag_latest, args, forceRefresh);
                }

                if (currentFragmentTag == R.string.fragment_tag_shows) {
                    Bundle args = new Bundle();
                    args.putBoolean("force", false);
                    launchFragment(R.string.fragment_tag_shows, args, false);
                }

                if (currentFragmentTag == R.string.fragment_tag_pref) {
                    launchFragment(R.string.fragment_tag_pref, null, false);
                }

                if (currentFragmentTag == R.string.fragment_tag_myshows) {

                    int count = new Select().from(Show.class).count();
                    int subCount = new Select().from(Show.class).where("isSubscribed=?", true).count();
                    if (count > 0) {
                        if (subCount > 0) {
                            launchFragment(R.string.fragment_tag_myshows, null, false);
                        } else {
                            showToast(getString(R.string.message_no_subscription), Toast.LENGTH_LONG);
                        }

                    } else {
                        launchFragment(R.string.fragment_tag_shows, null, false);
                    }
                }

                if (currentFragmentTag == R.string.fragment_tag_rss) {
                    String uri = Utility.getURL(listDataChild.get(listDataHeader.get(currentSelectedParentPos)).get(currentSelectedChildPos));
                    Bundle args = new Bundle();
                    args.putString("uri", uri);
                    launchFragment(R.string.fragment_tag_rss, args, true);
                }

                if (currentFragmentTag == R.string.fragment_tag_downloads) {
                    if (pref.getClientType().equals("")) {
                        showToast("You need to setup a client profile first in the Settings.", Toast.LENGTH_LONG);
                    } else {
                        Bundle args = new Bundle();
                        launchFragment(R.string.fragment_tag_downloads, args, true);
                    }

                }

            }

            /*
            public void onDrawerOpened(View drawerView) {
                //getActionBar().setTitle(mDrawerTitle);
                //invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }*/


        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            //selectItem(0);
        }

        launchFragment(R.string.fragment_tag_latest, null, false);

    }


    public void launchFragment(int fragmentTag, Bundle params, boolean force) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        switch (fragmentTag) {
            case R.string.fragment_tag_latest:
                LatestFragment listFragment = (LatestFragment) fragmentManager.findFragmentByTag(getString(R.string.fragment_tag_latest));
                if (listFragment == null) {
                    listFragment = new LatestFragment();
                    listFragment.setArguments(params);
                }


                transaction.replace(R.id.content_frame, listFragment, getString(R.string.fragment_tag_latest));

                if (force) {
                    listFragment.force = force;
                    listFragment.onActivityDrawerClosed();
                }

                setTitle("Latest Episodes");
                break;

            case R.string.fragment_tag_shows:
                ShowsFragment showFragment = (ShowsFragment) fragmentManager.findFragmentByTag(getString(R.string.fragment_tag_shows));
                if (showFragment == null || force) {
                    showFragment = new ShowsFragment();
                    showFragment.setArguments(params);
                } else {
                    showFragment.getArguments().putBoolean("force",true);
                }
                transaction.replace(R.id.content_frame, showFragment, getString(R.string.fragment_tag_shows));
                setTitle("TV Shows");
                break;
            case R.string.fragment_tag_search:
                SearchFragment searchFragment = (SearchFragment) fragmentManager.findFragmentByTag(getString(R.string.fragment_tag_search));
                if (searchFragment == null) {
                    searchFragment = new SearchFragment();
                    searchFragment.setArguments(params);
                } else {
                    searchFragment.search(params.getString("query"));
                }
                transaction.replace(R.id.content_frame, searchFragment, getString(R.string.fragment_tag_search));
                setTitle("Search");
                break;
            case R.string.fragment_tag_show_detail:
                ShowDetailsFragment detail = (ShowDetailsFragment) fragmentManager.findFragmentByTag(getString(R.string.fragment_tag_show_detail));
                if (detail == null) {
                    detail = new ShowDetailsFragment();
                    detail.setArguments(params);
                } else {
                    detail.getArguments().putInt("show_id",params.getInt("show_id"));
                }

                transaction.replace(R.id.content_frame, detail, getString(R.string.fragment_tag_show_detail));
                break;
            case R.string.fragment_tag_pref:
                PrefFragment pref = (PrefFragment) fragmentManager.findFragmentByTag(getString(R.string.fragment_tag_pref));
                if (pref == null) {
                    pref = new PrefFragment();
                }
                transaction.replace(R.id.content_frame, pref, getString(R.string.fragment_tag_pref));
                break;
            case R.string.fragment_tag_myshows:
                MyShowsFragment myShowsFragment = (MyShowsFragment) fragmentManager.findFragmentByTag(getString(R.string.fragment_tag_myshows));
                if (myShowsFragment == null) {
                    myShowsFragment = new MyShowsFragment();
                }
                transaction.replace(R.id.content_frame, myShowsFragment, getString(R.string.fragment_tag_myshows));
                setTitle("My Shows");
                break;
            case R.string.fragment_tag_rss:
                OtherSourceFragment sourceFragment = (OtherSourceFragment) fragmentManager.findFragmentByTag(getString(R.string.fragment_tag_rss));
                if (sourceFragment == null) {
                    sourceFragment = new OtherSourceFragment();
                    sourceFragment.setArguments(params);
                } else {
                    sourceFragment.getArguments().putString("uri", params.getString("uri"));
                }
                transaction.replace(R.id.content_frame, sourceFragment, getString(R.string.fragment_tag_rss));
                setTitle("Other RSS");
                break;
            case R.string.fragment_tag_downloads:
                DownloadsFragment downloadsFragment = (DownloadsFragment) fragmentManager.findFragmentByTag(getString(R.string.fragment_tag_downloads));
                if (downloadsFragment == null) {
                    downloadsFragment = new DownloadsFragment();
                    downloadsFragment.setArguments(params);
                }
                transaction.replace(R.id.content_frame, downloadsFragment, getString(R.string.fragment_tag_downloads));
                break;

        }

        if (currentFragmentTag != R.string.fragment_tag_show_detail) {
            transaction.addToBackStack(null);
        }

        transaction.commit();
        invalidateOptionsMenu();
        currentFragmentTag = fragmentTag; //just in case
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        switch (currentFragmentTag) {
            case R.string.fragment_tag_search:
            case R.string.fragment_tag_latest:
                inflater.inflate(R.menu.menu_latest, menu);
                final SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String s) {
                        Bundle args = new Bundle();
                        args.putString("query", s);
                        args.putBoolean("byId", false);
                        launchFragment(R.string.fragment_tag_search, args, true);

                        searchView.clearFocus();
                        menu.findItem(R.id.action_search).collapseActionView();

                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String s) {
                        return false;
                    }
                });

                break;
            case R.string.fragment_tag_shows:
                inflater.inflate(R.menu.menu_latest, menu);
                final SearchView showSearch = (SearchView) menu.findItem(R.id.action_search).getActionView();
                showSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String s) {
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String s) {
                        ShowsFragment shows = (ShowsFragment) fragmentManager.findFragmentByTag(getString(R.string.fragment_tag_shows));
                        if (shows != null) {
                            shows.adapter.getFilter().filter(s);
                            //shows.adapter.notifyDataSetChanged();
                            return true;
                        }
                        return false;
                    }
                });

                break;
        }

        return super.onCreateOptionsMenu(menu);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        if (currentFragmentTag == R.string.fragment_tag_latest || currentFragmentTag == R.string.fragment_tag_shows) {
            menu.findItem(R.id.action_refresh).setVisible(false);
            menu.findItem(R.id.action_search).setVisible(currentFragmentTag == R.string.fragment_tag_latest ||
                    currentFragmentTag == R.string.fragment_tag_shows || currentFragmentTag == R.string.fragment_tag_search);
        }

        if (currentFragmentTag == R.string.fragment_tag_search) {
            menu.findItem(R.id.action_refresh).setVisible(false);
        }

        return super.onPrepareOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        // Handle action buttons

        switch (item.getItemId()) {
            case R.id.action_refresh:
                currentPage = 0;
                if (currentFragmentTag == R.string.fragment_tag_latest) {
                    Bundle args = new Bundle();
                    args.putBoolean("force", true);
                    launchFragment(R.string.fragment_tag_latest, args, true);
                }

                if (currentFragmentTag == R.string.fragment_tag_shows) {
                    Bundle args = new Bundle();
                    args.putBoolean("force", true);
                    launchFragment(R.string.fragment_tag_shows, args, true);
                }
        }

        return super.onOptionsItemSelected(item);
    }

    public void showToast(String msg, int duration) {
        Toast.makeText(getApplicationContext(), msg, duration).show();
    }

    private class DrawerGroupItemClickListener implements ExpandableListView.OnGroupClickListener {
        @Override
        public boolean onGroupClick(ExpandableListView parent, View v,
                                    int groupPosition, long id) {
            if (listDrawerAdapter.getChildrenCount(groupPosition) <= 0) {
                String category = listDataHeader.get(groupPosition);
                if (category.equals(getString(R.string.cat_latest))) {
                    //forceRefresh = true;
                    currentFragmentTag = R.string.fragment_tag_latest;
                }
                if (category.equals(getString(R.string.cat_shows))) {
                    currentFragmentTag = R.string.fragment_tag_shows;
                }

                if (category.equals(getString(R.string.cat_settings))) {
                    currentFragmentTag = R.string.fragment_tag_pref;
                }

                if (category.equals(getString(R.string.cat_myshows))) {
                    currentFragmentTag = R.string.fragment_tag_myshows;
                }

                if (category.equals(getString(R.string.cat_downloads))) {
                    currentFragmentTag = R.string.fragment_tag_downloads;
                }

                if (category.equals(getString(R.string.cat_support))) {
                    String url = "http://eztvdroid.org";
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                }

                if (category.equals(getString(R.string.cat_like))) {
                    Uri uri = Uri.parse("market://details?id=" + getPackageName());
                    Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
                    try {
                        startActivity(myAppLinkToMarket);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(getApplicationContext(), "Unable to find App", Toast.LENGTH_LONG).show();
                    }
                }

                mDrawerLayout.closeDrawer(mDrawerList);
            }

            return false;
        }
    }

    public void showMessage(String title, String message, String btnPosTitle) {
        Utility.showDialog(Main.this, title, message, btnPosTitle, null, false, null);
    }


    /* The click listner for ListView in the navigation drawer */
    private class DrawerChildClickListener implements ExpandableListView.OnChildClickListener {
        @Override
        public boolean onChildClick(ExpandableListView parent, View v,
                                    int groupPosition, int childPosition, long id) {

            String cat = listDataHeader.get(groupPosition);
            String subcat = listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition);

            FragmentManager fragmentManager = getFragmentManager();
            if (cat.equals(getString(R.string.cat_latest)) && currentFragmentTag != R.string.fragment_tag_latest) {
                currentFragmentTag = R.string.fragment_tag_latest;
            }

            if (cat.equals(getString(R.string.cat_sources))) {
                currentFragmentTag = R.string.fragment_tag_rss;
            }

            currentSelectedChildPos = childPosition;
            currentSelectedParentPos = groupPosition;

            mDrawerList.setItemChecked(childPosition, true);
            mDrawerLayout.closeDrawer(mDrawerList);

            return false;
        }

    }


    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }


}