package com.hamaksoftware.eztvdroid.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hamaksoftware.eztvdroid.models.*;

import java.util.ArrayList;

public class ShowHandler extends DBHandler{

	ArrayList<EZTVShowItem> shows;

    public ShowHandler(Context context) {
        super(context);
        shows = new ArrayList<EZTVShowItem>(0);
        super.onCreate(this.getWritableDatabase());
    }
 

    
    public void addShow(EZTVShowItem show) {
    	   SQLiteDatabase db = this.getWritableDatabase();
    	   
    	    ContentValues values = new ContentValues();
    	    values.put(KEY_SHOW_ID,show.showId);
    	    values.put(KEY_TITLE, show.title);
    	    values.put(KEY_SHOWLINK, show.showLink);
    	    values.put(KEY_STATUS, show.status);
    	    values.put(KEY_ISSELECTED, show.isSeledted);
    	    values.put(KEY_ISSUBSCRIBE, show.isSubscribed);

    	    db.insert(TABLE_SHOWS, null, values);
    	    db.close();
    }
    

	 public EZTVShowItem getShow(int id) {
		   SQLiteDatabase db = this.getReadableDatabase();
		   
		    Cursor cursor = db.query(TABLE_SHOWS, new String[] { KEY_SHOW_ID,KEY_TITLE, KEY_SHOWLINK, KEY_STATUS, KEY_ISSELECTED, KEY_ISSUBSCRIBE}, KEY_SHOW_ID + "=?",
		            new String[] { String.valueOf(id) }, null, null, null, null);
		    //if (cursor != null) cursor.moveToFirst();
		    EZTVShowItem show = null;
		    while(cursor.moveToNext()){
		    	show = new EZTVShowItem();
			    show.showId = Integer.parseInt(cursor.getString(0));
			    show.title = cursor.getString(1);
			    show.showLink = cursor.getString(2);
			    show.status = cursor.getString(3);
			    show.isSeledted = cursor.getInt(4)==1?true:false;
			    show.isSubscribed = cursor.getInt(5)==1?true:false;
		    }
		    
		    cursor.close();
            db.close();
		    return show;
	 }
	  

	 public int getCount(){
		 int count = 0;
		 String selectQuery = "SELECT  count(*) FROM " + TABLE_SHOWS;
		    SQLiteDatabase db = this.getReadableDatabase();
		    Cursor cursor = db.rawQuery(selectQuery, null);
		    if (cursor.moveToFirst()) {
		        do {
		        	count = cursor.getInt(0);
		        } while (cursor.moveToNext());
		    }
		  
		  return count;  
	 }
	 
	 
	 public int getSubscribeCount(){
		 int count = 0;
		 String selectQuery = "SELECT  count(*) FROM " + TABLE_SHOWS + " where " + KEY_ISSUBSCRIBE + "=1" ;
		    SQLiteDatabase db = this.getReadableDatabase();
		    Cursor cursor = db.rawQuery(selectQuery, null);
		    if (cursor.moveToFirst()) {
		        do {
		        	count = cursor.getInt(0);
		        } while (cursor.moveToNext());
		    }
		  
		  return count;  
	 }
	 
	 
	 public ArrayList<EZTVShowItem> getAllShows() {

		    // Select All Query
		    String selectQuery = "SELECT  * FROM " + TABLE_SHOWS;
		 
		    SQLiteDatabase db = this.getReadableDatabase();
		    Cursor cursor = db.rawQuery(selectQuery, null);
		 
		    // looping through all rows and adding to list
		    if (cursor.moveToFirst()) {
		        do {
		            EZTVShowItem show = new EZTVShowItem();
                    show.showId = Integer.parseInt(cursor.getString(0));
                    show.title = cursor.getString(1);
                    show.showLink = cursor.getString(2);
                    show.status = cursor.getString(3);
                    show.isSeledted = cursor.getInt(4)==1?true:false;
                    show.isSubscribed = cursor.getInt(5)==1?true:false;
		            shows.add(show);
		        } while (cursor.moveToNext());
		    }
		 
		    cursor.close();
		    db.close();
		    return shows;
	 }

    public void deleteAll(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SHOWS, null, null);
    }

    public ArrayList<EZTVShowItem> getMyShows() {

        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_SHOWS + " where " + KEY_ISSUBSCRIBE+ "=1";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                EZTVShowItem show = new EZTVShowItem();
                show.showId = Integer.parseInt(cursor.getString(0));
                show.title = cursor.getString(1);
                show.showLink = cursor.getString(2);
                show.status = cursor.getString(3);
                show.isSeledted = cursor.getInt(4)==1?true:false;
                show.isSubscribed = cursor.getInt(5)==1?true:false;
                shows.add(show);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return shows;
    }

	 public int updateShow(EZTVShowItem show) {
	    SQLiteDatabase db = this.getWritableDatabase();
	    
	    ContentValues values = new ContentValues();
         values.put(KEY_SHOW_ID, show.showId);
         values.put(KEY_TITLE, show.title);
         values.put(KEY_SHOWLINK, show.showLink);
         values.put(KEY_STATUS, show.status);
         values.put(KEY_ISSELECTED, show.isSeledted);
         values.put(KEY_ISSUBSCRIBE, show.isSubscribed);
	    
	    return db.update(TABLE_SHOWS, values, KEY_SHOW_ID + " = ?",
	            new String[] { String.valueOf(show.showId) });
	 }
	  
	 // Deleting single contact
	 public void deleteShow(EZTVShowItem show) {
		   SQLiteDatabase db = this.getWritableDatabase();
		    db.delete(TABLE_SHOWS, KEY_SHOW_ID + " = ?",
		            new String[] { String.valueOf(show.showId) });
		    db.close();
	 }
    
    
}
