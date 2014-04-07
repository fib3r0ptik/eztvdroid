package com.hamaksoftware.eztvdroid.adapters;

import java.util.HashMap;
import java.util.List;


import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hamaksoftware.eztvdroid.R;


public class DrawerGroupAdapter extends BaseExpandableListAdapter {
	public static final int TYPE_ITEM = 0;
	public static final int TYPE_SEPARATOR = 1;
    public static final int TYPE_MAX_COUNT = TYPE_SEPARATOR + 1;
    
	private Context _context;
    private List<String> _listDataHeader; // header titles
    // child data in format of header title, child title
    private HashMap<String, List<String>> _listDataChild;
 
    public DrawerGroupAdapter(Context context, List<String> listDataHeader,
            HashMap<String, List<String>> listChildData) {
        this._context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
    }
 
    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .get(childPosititon);
    }
 
    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }
 
    @Override
    public View getChildView(int groupPosition, final int childPosition,
            boolean isLastChild, View convertView, ViewGroup parent) {
 
        final String childText = (String) getChild(groupPosition, childPosition);
 
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.drawer_main_item_list, null);
        }
 
        TextView txtListChild = (TextView) convertView
                .findViewById(R.id.drawer_list_item_sub_text);
 
        txtListChild.setText(childText);
                
        return convertView;
    }
 
    @Override
    public int getChildrenCount(int groupPosition) {
    	List<String> header = this._listDataChild.get(this._listDataHeader.get(groupPosition));
        if(header !=null){
        	return header.size();
        }else{
        	return 0;
        }
    }
 
    @Override
    public Object getGroup(int groupPosition) {
        return this._listDataHeader.get(groupPosition);
    }
 
    @Override
    public int getGroupCount() {
        return this._listDataHeader.size();
    }
 
    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }
 
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
            View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.drawer_main_group_list, null);
        }
 
        TextView lblListHeader = (TextView) convertView
                .findViewById(R.id.drawer_list_item_text);
        
        ImageView imgCatIcon = (ImageView)convertView.findViewById(R.id.drawer_list_item_icon);

        ImageView imgCollapse = (ImageView)convertView.findViewById(R.id.drawer_collapse);

        if(this._listDataHeader.size() > 0){
            	String category = this._listDataHeader.get(groupPosition);

                if(category.equals(_context.getResources().getString(R.string.cat_search))){
                    imgCatIcon.setBackgroundResource(R.drawable.ic_action_search);
                }

                if(category.equals(_context.getResources().getString(R.string.cat_latest))){
                    imgCatIcon.setBackgroundResource(R.drawable.ic_action_view_as_grid);
                }

                if(category.equals(_context.getResources().getString(R.string.cat_like))){
                    imgCatIcon.setBackgroundResource(R.drawable.ic_action_share);
                }


                if(category.equals(_context.getResources().getString(R.string.cat_shows))){
                    imgCatIcon.setBackgroundResource(R.drawable.ic_action_computer);
                }

                if(category.equals(_context.getResources().getString(R.string.cat_downloads))){
                    imgCatIcon.setBackgroundResource(R.drawable.ic_action_download);
                }

                if(category.equals(_context.getResources().getString(R.string.cat_sources))){
                    imgCatIcon.setBackgroundResource(R.drawable.ic_sources);
                }

                if(category.equals(_context.getResources().getString(R.string.cat_settings))){
                    imgCatIcon.setBackgroundResource(R.drawable.ic_action_settings);
                }

                if(category.equals(_context.getResources().getString(R.string.cat_support))){
                    imgCatIcon.setBackgroundResource(R.drawable.ic_action_help);
                }

                if(category.equals(_context.getResources().getString(R.string.cat_like))){
                    imgCatIcon.setBackgroundResource(R.drawable.ic_action_share);
                }

        		LinearLayout l = (LinearLayout)convertView.findViewById(R.id.group_container);

                imgCollapse.setVisibility(getChildrenCount(groupPosition)>0?View.VISIBLE:View.GONE);

            	if(category.equals(_context.getResources().getString(R.string.cat_app_settings))){
            		//l.setPadding(2, 2, 2, 2);
            		lblListHeader.setTextColor(Color.WHITE);
            		l.setBackgroundColor(_context.getResources() .getColor(R.color.light_blue));
            		imgCatIcon.setVisibility(View.GONE);
            	}else{
            		//l.setPadding(3, 10, 3, 10);
            		l.setBackgroundColor(Color.WHITE);
            		lblListHeader.setTextAppearance(_context, android.R.attr.textAppearanceSmall);
            		lblListHeader.setTextColor(_context.getResources().getColor(R.color.light_black));
            		imgCatIcon.setVisibility(View.VISIBLE);
            	}
        }
        
        
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);
 
        return convertView;
    }
 
    @Override
    public boolean hasStableIds() {
        return false;
    }
 
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
