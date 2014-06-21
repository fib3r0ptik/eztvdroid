package com.hamaksoftware.tvbrowser.torrentcontroller;

public class Torrent {
    public String name = null;
    public String hash = null;
    public int status;
    public int size = 0;
    public int percent = 0;
    public int downloaded = 0;
    public int uploaded = 0;
    public int downloadspeed = 0;
    public int uploadspeed = 0;
    public int seederscon = 0;
    public int seedersall = 0;
    public int peerscon = 0;
    public int peersall = 0;
    public int order = 0;
    public int remaining = 0;
    public int eta = 0;
    public boolean isSelected;
    public boolean sortByQueue;
}
