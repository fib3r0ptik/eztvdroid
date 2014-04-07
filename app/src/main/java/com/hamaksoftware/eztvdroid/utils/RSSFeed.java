package com.hamaksoftware.eztvdroid.utils;

import com.hamaksoftware.eztvdroid.models.RSSItem;

import java.util.ArrayList;

public class RSSFeed {
	public String title = null;
	public String pubdate = null;

	public int itemcount = 0;
	public ArrayList<RSSItem> itemlist;

	public RSSFeed() {
		itemlist = new ArrayList<RSSItem>(0);
	}

	public int addItem(RSSItem item) {
		itemlist.add(item);
		itemcount++;
		return itemcount;
	}
	
}
