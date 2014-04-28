package com.hamaksoftware.eztvdroid.models;

import java.util.ArrayList;

public class RSSItem {
	public String title = null;
	public String description = null;
	public String itemlink = null;
	public String showLink = null;
	public String category = null;
	public String pubdate = null;
	public String enclosure = null;
	public String comments = null;
	public String guid = null;
	public String altLInk = null;
	public double filesize = 0;
	public boolean selected;
	public ArrayList<String> otherlinks;

	public RSSItem() {
        otherlinks = new ArrayList<String>(0);
	}

}
