package com.hamaksoftware.tvbrowser.models;

import java.util.ArrayList;
import java.util.List;

public class Episode implements Cloneable{
	public List<String> links;
	public String title;
	public String elapsed;
	public boolean isSelected;
	public boolean isWatched;
	public boolean isFavorite;
	public String filesize;
	public String markedWatchedLink;
	public int showId;
	
	
	public Episode(){
		links = new ArrayList<String>(0);
	}

}
