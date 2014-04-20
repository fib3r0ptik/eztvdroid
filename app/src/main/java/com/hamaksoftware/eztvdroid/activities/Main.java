package com.hamaksoftware.eztvdroid.activities;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;


import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Toast;

import com.hamaksoftware.eztvdroid.R;
import com.hamaksoftware.eztvdroid.fragments.*;
import com.hamaksoftware.eztvdroid.utils.*;
import com.hamaksoftware.eztvdroid.adapters.*;

public class Main extends Activity{
    /* private members */
    private DrawerLayout mDrawerLayout;
    private ExpandableListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    
    
    private DrawerGroupAdapter listDrawerAdapter;
    private List<String> listDataHeader;
    private HashMap<String, List<String>> listDataChild;

    private DrawerChildClickListener drawerChildClickListener;
    public boolean forceRefresh;

    //private Fragment gridFragment;
    
    public AppPref pref;
    public ShowHandler sh;
    public ProfileHandler ph;
    public ArrayList<ClientProfile> profiles;
    private FragmentManager fragmentManager;
    
    /* public members */
    
    public int currentPage;
    //public IActivityListener activityListener;

    public int currentFragmentTag;
    //public List<WeakReference<Fragment>> visibleFragments = new ArrayList<WeakReference<Fragment>>(0);
      
    private void prepareListData() {
    	
    	//find me in res/values
    	
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();
        
        String[] categories  = getResources().getStringArray(R.array.categories);
        for(int i = 0; i < categories.length;i++){
        	try {
				JSONArray jsonarr = new JSONArray(categories[i]);
				listDataHeader.add(jsonarr.getString(0));
				List<String> obj = new ArrayList<String>(0);
				for(int j = 1; j < jsonarr.length();j++){
					obj.add(jsonarr.getString(j));
					listDataChild.put(listDataHeader.get(i), obj);
				}

			} catch (JSONException e) {
				Log.e("prepareData",e.getMessage());
			}
        }
       
    }
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pref = new AppPref(getApplicationContext());
        ph = new ProfileHandler(getApplicationContext());
        profiles = ph.getAllProfiles();
        sh = new ShowHandler(getApplicationContext());

        fragmentManager = getFragmentManager();

        mTitle = mDrawerTitle = getResources().getString(R.string.app_name);
        //mPlanetTitles = getResources().getStringArray(R.array.planets_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ExpandableListView) findViewById(R.id.left_drawer);

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        prepareListData();
        listDrawerAdapter = new DrawerGroupAdapter(this, listDataHeader, listDataChild);
        mDrawerList.setAdapter(listDrawerAdapter);
        

        drawerChildClickListener =  new DrawerChildClickListener();
        
        mDrawerList.setOnGroupClickListener(new DrawerGroupItemClickListener());
        mDrawerList.setOnChildClickListener(drawerChildClickListener);
        mDrawerList.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int i) {
                LinearLayout parent = (LinearLayout)mDrawerList.getChildAt(i);
                ImageView expand = (ImageView)parent.findViewById(R.id.drawer_collapse);
                expand.setImageResource(R.drawable.ic_action_expand);
            }
        });

        mDrawerList.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int i) {
                LinearLayout parent = (LinearLayout)mDrawerList.getChildAt(i);
                ImageView collapse = (ImageView)parent.findViewById(R.id.drawer_collapse);
                collapse.setImageResource(R.drawable.ic_action_collapse);
            }
        });

        
        //drawerChildClickListener.onChildClick(null, null, 1, 1, 100000L);
        
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);



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
                if(currentFragmentTag == R.string.fragment_tag_latest){
                    Bundle args = new Bundle();
                    args.putBoolean("force",forceRefresh);
                    launchFragment(R.string.fragment_tag_latest, args,forceRefresh);
                }

                if(currentFragmentTag == R.string.fragment_tag_shows){
                    launchFragment(R.string.fragment_tag_shows, null,false);
                }

                if(currentFragmentTag == R.string.fragment_tag_pref){
                    launchFragment(R.string.fragment_tag_pref,null, false);
                }

            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                //invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }


        };
        
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            //selectItem(0);
        }

        launchFragment(R.string.fragment_tag_latest,null,false);

    }


    public void launchFragment(int fragmentTag, Bundle params,boolean force){
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        switch(fragmentTag){
            case R.string.fragment_tag_latest:
                LatestFragment listFragment = (LatestFragment)fragmentManager.findFragmentByTag(getString(R.string.fragment_tag_latest));
                if(listFragment == null) {
                    listFragment = new LatestFragment();
                    listFragment.setArguments(params);
                }


                transaction.replace(R.id.content_frame, listFragment,getString(R.string.fragment_tag_latest));

                if(force){
                    listFragment.force = force;
                    listFragment.onActivityDrawerClosed();
                }

                break;

            case R.string.fragment_tag_shows:
                ShowsFragment showFragment = (ShowsFragment)fragmentManager.findFragmentByTag(getString(R.string.fragment_tag_shows));
                if(showFragment == null) {
                    showFragment = new ShowsFragment();
                    showFragment.setArguments(params);
                }
                transaction.replace(R.id.content_frame, showFragment,getString(R.string.fragment_tag_shows));
                break;
            case R.string.fragment_tag_search:
                SearchFragment searchFragment = (SearchFragment)fragmentManager.findFragmentByTag(getString(R.string.fragment_tag_search));
                if(searchFragment == null) {
                    searchFragment = new SearchFragment();
                    searchFragment.setArguments(params);
                }else{
                    searchFragment.search(params.getString("query"));
                }
                transaction.replace(R.id.content_frame, searchFragment,getString(R.string.fragment_tag_search));
                break;
            case R.string.fragment_tag_show_detail:
                ShowDetailsFragment detail = (ShowDetailsFragment)fragmentManager.findFragmentByTag(getString(R.string.fragment_tag_show_detail));
                if(detail == null) {
                    detail = new ShowDetailsFragment();
                    detail.setArguments(params);
                }else{
                    detail.setShowDetails(params.getInt("show_id"));
                }
                transaction.replace(R.id.content_frame, detail,getString(R.string.fragment_tag_show_detail));
                break;
            case R.string.fragment_tag_pref:
                PrefFragment pref = (PrefFragment)fragmentManager.findFragmentByTag(getString(R.string.fragment_tag_pref));
                if(pref == null){
                    pref = new PrefFragment();
                }
                transaction.replace(R.id.content_frame, pref,getString(R.string.fragment_tag_pref));
                break;
        }

        transaction.addToBackStack(null);
        transaction.commit();
        invalidateOptionsMenu();
        currentFragmentTag = fragmentTag; //just in case
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        switch(currentFragmentTag){
            case R.string.fragment_tag_search:
            case R.string.fragment_tag_latest:
                inflater.inflate(R.menu.menu_latest, menu);
                final SearchView searchView = (SearchView)menu.findItem(R.id.action_search).getActionView();
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String s) {
                        Bundle args = new Bundle();
                        args.putString("query", s);
                        args.putBoolean("byId",false);
                        launchFragment(R.string.fragment_tag_search, args,true);

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
                inflater.inflate(R.menu.menu_latest, menu); //reuse
                break;
        }

        return super.onCreateOptionsMenu(menu);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        /*If the nav drawer is open, hide action items related to the content view
        final boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.action_server_status_on).setVisible(false);
        menu.findItem(R.id.action_server_status_off).setVisible(true);
        */
        if(currentFragmentTag == R.string.fragment_tag_latest || currentFragmentTag == R.string.fragment_tag_shows){
            menu.findItem(R.id.action_refresh).setVisible(currentFragmentTag == R.string.fragment_tag_latest ||
                    currentFragmentTag == R.string.fragment_tag_shows);
            menu.findItem(R.id.action_search).setVisible(currentFragmentTag == R.string.fragment_tag_latest ||
                    currentFragmentTag == R.string.fragment_tag_shows || currentFragmentTag == R.string.fragment_tag_search);
        }

        if(currentFragmentTag == R.string.fragment_tag_search){
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

        switch(item.getItemId()) {
            case R.id.action_refresh:
                currentPage = 0;
                if(currentFragmentTag == R.string.fragment_tag_latest || currentFragmentTag == R.string.fragment_tag_search){
                    Bundle args = new Bundle();
                    args.putBoolean("force",true);
                    launchFragment(R.string.fragment_tag_latest,args,true);
                }
        }

        return super.onOptionsItemSelected(item);
    }

    public void showToast(String msg, int duration){
        Toast.makeText(getApplicationContext(),msg,duration).show();
    }

    private class DrawerGroupItemClickListener implements ExpandableListView.OnGroupClickListener {
		@Override
		public boolean onGroupClick(ExpandableListView parent, View v,
				int groupPosition, long id) {
            if(listDrawerAdapter.getChildrenCount(groupPosition) <= 0 ){
                String category = listDataHeader.get(groupPosition);
                if(category.equals(getString(R.string.cat_latest))) {
                    //forceRefresh = true;
                    currentFragmentTag = R.string.fragment_tag_latest;
                }
                if(category.equals(getString(R.string.cat_shows))) {
                    currentFragmentTag = R.string.fragment_tag_shows;
                }

                if(category.equals(getString(R.string.cat_settings))){
                    currentFragmentTag = R.string.fragment_tag_pref;
                }

                mDrawerLayout.closeDrawer(mDrawerList);
            }

			return false;
		}
    }
    
    public void showMessage(String title, String message, String btnPosTitle){
   	 	Utility.showDialog(Main.this,title, message, btnPosTitle, null, false,null);
    }

	
    /* The click listner for ListView in the navigation drawer */
    private class DrawerChildClickListener implements ExpandableListView.OnChildClickListener {
        @Override
        public boolean onChildClick(ExpandableListView parent, View v,
                int groupPosition, int childPosition, long id) {

        	String cat = listDataHeader.get(groupPosition);
        	String subcat = listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition);
        	
            FragmentManager fragmentManager = getFragmentManager();
            if(cat.equals(getString(R.string.cat_latest)) && currentFragmentTag != R.string.fragment_tag_latest){
                currentFragmentTag = R.string.fragment_tag_latest;
            }


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