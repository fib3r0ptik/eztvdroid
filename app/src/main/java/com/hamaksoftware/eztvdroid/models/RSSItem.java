package com.hamaksoftware.eztvdroid.models;

import java.util.ArrayList;

public class RSSItem {
	public String _title = null;
	public String _description = null;
	public String _link = null;
	public String _showLink = null;
	public String _category = null;
	public String _pubdate = null;
	public String _enclosure = null;
	public String _comments = null;
	public String _guid = null;
	public String _altLInk=null;
	public double _filesize = 0;
	public boolean _selected;
	public ArrayList<String> otherlinks;

	public RSSItem() {
	}

}
