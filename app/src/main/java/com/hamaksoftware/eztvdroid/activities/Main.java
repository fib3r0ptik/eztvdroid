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
import android.content.res.Configuration;
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

import com.hamaksoftware.eztvdroid.R;
import com.hamaksoftware.eztvdroid.fragments.*;
import com.hamaksoftware.eztvdroid.utils.*;
import com.hamaksoftware.eztvdroid.adapters.*;

public class Main extends Activity implements IFragmentListener {
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

    //private Fragment gridFragment;
    
    private AppPref pref;
    private FragmentManager fragmentManager;
    
    /* public members */
    
    public int currentPage;
    public IActivityListener activityListener;

    public int currentFragmentTag;
    public List<WeakReference<Fragment>> visibleFragments = new ArrayList<WeakReference<Fragment>>(0);
      
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
        
        //mDrawerList.setOnGroupClickListener(new DrawerGroupItemClickListener());
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
                //check for vsible fragments
                String visibleTag = null;
                for(WeakReference<Fragment> ref : visibleFragments) {
                    Fragment f = ref.get();
                    if(f != null){
                        if(f.isVisible()){
                            visibleTag = f.getTag();
                            break;
                        }
                    }
                }

                if(activityListener != null && !visibleTag.equals(getString(currentFragmentTag))) {
                    activityListener.onActivityDrawerClosed();
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

        LatestFragment listFragment = (LatestFragment)fragmentManager.findFragmentByTag("LIST_FRAGMENT");
        if(listFragment == null){
            listFragment = new LatestFragment();
            fragmentManager.beginTransaction().replace(R.id.content_frame, listFragment,getString(R.string.fragment_tag_latest)).commit();
        }

        activityListener = listFragment;
        currentFragmentTag = R.string.fragment_tag_latest;
    }


    public List<Fragment> getActiveFragments() {
        ArrayList<Fragment> activeFragments = new ArrayList<Fragment>();
        for(WeakReference<Fragment> ref : visibleFragments) {
            Fragment f = ref.get();
            if(f != null) {
                if(f.isVisible()) {
                    activeFragments.add(f);
                }
            }
        }
        return activeFragments;
    }

    @Override
    public void onAttachFragment (Fragment fragment) {
        visibleFragments.add(new WeakReference<Fragment>(fragment));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //MenuInflater inflater = getMenuInflater();
        //inflater.inflate(R.menu.main, menu);

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
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private class DrawerGroupItemClickListener implements ExpandableListView.OnGroupClickListener {
		@Override
		public boolean onGroupClick(ExpandableListView parent, View v,
				int groupPosition, long id) {
			//Toast.makeText(getApplicationContext(), listDataHeader.get(groupPosition), Toast.LENGTH_SHORT).show();

			return false;
		}
    }
    
    public void showMessage(String title, String message, String btnPosTitle){
   	 	Utility.showDialog(Main.this,title, message, btnPosTitle, null, false,null);
    }

	@Override
	public void onFragmentViewCreated() {
		activityListener.onFragmentLaunched();
	}
    
	@Override
	public void onViewClicked(View v) {
        /*
		if(v.getId() == R.id.btnSearch){
			activityListener = null;
			String tag = v.getTag().toString();
			String[] tags = tag.split("|");

			ListItemFragment listfrag = (ListItemFragment)getFragmentManager().findFragmentByTag("LIST_FRAGMENT");
			if(listfrag == null){
				listfrag = new ListItemFragment();
			}
            
            Bundle args = new Bundle();
            args.putString("cat", tags[0]);
            args.putString("query", tags[1]);
            listfrag.setArguments(args);
			
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, listfrag,"LIST_FRAGMENT").commit();
            
            activityListener = listfrag;
    		
		}
		*/

	}
	
	
	
    /* The click listner for ListView in the navigation drawer */
    private class DrawerChildClickListener implements ExpandableListView.OnChildClickListener {
        @Override
        public boolean onChildClick(ExpandableListView parent, View v,
                int groupPosition, int childPosition, long id) {
        	
            activityListener = null; //reset drawerChildClickListener to avoid "unnecessary" triggering of async on onDrawerClosed
            
        	String cat = listDataHeader.get(groupPosition);
        	String subcat = listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition);
        	
            FragmentManager fragmentManager = getFragmentManager();
            if(cat.equals(getString(R.string.cat_latest)) && currentFragmentTag != R.string.fragment_tag_latest){
                LatestFragment listFragment = (LatestFragment)fragmentManager.findFragmentByTag(getString(R.string.fragment_tag_latest));
                if(listFragment == null) listFragment = new LatestFragment();
                fragmentManager.beginTransaction().replace(R.id.content_frame, listFragment,getString(R.string.fragment_tag_latest)).commit();
                activityListener = listFragment;
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