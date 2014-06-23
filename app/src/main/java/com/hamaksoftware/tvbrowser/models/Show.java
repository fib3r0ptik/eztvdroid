package com.hamaksoftware.tvbrowser.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

@Table(name = "Shows")
public class Show extends Model {

    @Column
    public int showId;
    @Column
    public String title;
    @Column
    public String showLink;
    @Column
    public String status;
    @Column
    public boolean isSubscribed;
    @Column
    public boolean hasNewEpisode;

}
