package com.hamaksoftware.tvbrowser.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hamaksoftware.tvbrowser.models.Show;

import java.util.ArrayList;

public class ShowHandler{

	private ArrayList<Show> shows;
    private Context ctx;

    final String KEY_SHOW_ID = "show_id";
    final String KEY_TITLE = "show_title";
    final String KEY_SHOWLINK = "show_link";
    final String KEY_STATUS = "show_status";
    final String KEY_ISSELECTED = "show_isselected";
    final String KEY_ISSUBSCRIBE = "show_issubscribe";

    final String TABLE_SHOWS = "myshows";

    public ShowHandler(Context ctx) {
        this.ctx = ctx;
        shows = new ArrayList<Show>(0);
    }

    public void addShow(Show show) {
    	   SQLiteDatabase db = DBHandler.getInstance(ctx).getWritableDatabase();
    	   
    	    ContentValues values = new ContentValues();
    	    values.put(KEY_SHOW_ID,show.showId);
    	    values.put(KEY_TITLE, show.title);
    	    values.put(KEY_SHOWLINK, show.showLink);
    	    values.put(KEY_STATUS, show.status);
    	    values.put(KEY_ISSELECTED, show.isSeledted);
    	    values.put(KEY_ISSUBSCRIBE, show.isSubscribed);

    	    db.insert(TABLE_SHOWS, null, values);
    	    //db.close();
    }


	 public Show getShow(int id) {

		   SQLiteDatabase db = DBHandler.getInstance(ctx).getReadableDatabase();
		    Cursor cursor = db.query(TABLE_SHOWS, new String[] { KEY_SHOW_ID,KEY_TITLE, KEY_SHOWLINK, KEY_STATUS, KEY_ISSELECTED, KEY_ISSUBSCRIBE}, KEY_SHOW_ID + "=?",
		            new String[] { String.valueOf(id) }, null, null, null, null);
		    //if (cursor != null) cursor.moveToFirst();
		    Show show = null;
		    while(cursor.moveToNext()){
		    	show = new Show();
			    show.showId = Integer.parseInt(cursor.getString(0));
			    show.title = cursor.getString(1);
			    show.showLink = cursor.getString(2);
			    show.status = cursor.getString(3);
			    show.isSeledted = cursor.getInt(4)==1?true:false;
			    show.isSubscribed = cursor.getInt(5)==1?true:false;
		    }
		    
		    cursor.close();
            //db.close();
		    return show;
	 }
	  

	 public int getCount(){
		 int count = 0;
		 String selectQuery = "SELECT  count(*) FROM " + TABLE_SHOWS;
		    SQLiteDatabase db = DBHandler.getInstance(ctx).getReadableDatabase();
		    Cursor cursor = db.rawQuery(selectQuery, null);
		    if (cursor.moveToFirst()) {
		        do {
		        	count = cursor.getInt(0);
		        } while (cursor.moveToNext());
		    }

          cursor.close();
          //db.close();
		  return count;  
	 }
	 
	 
	 public int getSubscribeCount(){
        int count = 0;
        String selectQuery = "SELECT  count(*) FROM " + TABLE_SHOWS + " where " + KEY_ISSUBSCRIBE + "=1" ;
        SQLiteDatabase db = DBHandler.getInstance(ctx).getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                count = cursor.getInt(0);
            }while(cursor.moveToNext());
        }
        cursor.close();
        //db.close();
        return count;
	 }
	 
	 
	 public ArrayList<Show> getAllShows() {

		    // Select All Query
		    String selectQuery = "SELECT  * FROM " + TABLE_SHOWS;
		 
		    SQLiteDatabase db = DBHandler.getInstance(ctx).getReadableDatabase();
		    Cursor cursor = db.rawQuery(selectQuery, null);
		 
		    // looping through all rows and adding to list
		    if (cursor.moveToFirst()) {
		        do {
		            Show show = new Show();
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
		    //db.close();
		    return shows;
	 }

    public void deleteAll(){
        SQLiteDatabase db = DBHandler.getInstance(ctx).getWritableDatabase();
        db.delete(TABLE_SHOWS, null, null);
        db.close();
    }

    public ArrayList<Show> getMyShows() {

        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_SHOWS + " where " + KEY_ISSUBSCRIBE+ "=1";

        SQLiteDatabase db = DBHandler.getInstance(ctx).getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Show show = new Show();
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
        //db.close();
        return shows;
    }

	 public int updateShow(Show show) {
	    SQLiteDatabase db = DBHandler.getInstance(ctx).getWritableDatabase();
	    
	    ContentValues values = new ContentValues();
         values.put(KEY_SHOW_ID, show.showId);
         values.put(KEY_TITLE, show.title);
         values.put(KEY_SHOWLINK, show.showLink);
         values.put(KEY_STATUS, show.status);
         values.put(KEY_ISSELECTED, show.isSeledted);
         values.put(KEY_ISSUBSCRIBE, show.isSubscribed);
	    
	    int count = db.update(TABLE_SHOWS, values, KEY_SHOW_ID + " = ?",
	            new String[] { String.valueOf(show.showId) });
        //db.close();
        return count;
	 }
	  
	 // Deleting single contact
	 public void deleteShow(Show show) {
		   SQLiteDatabase db = DBHandler.getInstance(ctx).getWritableDatabase();
		    db.delete(TABLE_SHOWS, KEY_SHOW_ID + " = ?",
		            new String[] { String.valueOf(show.showId) });
		    //db.close();
	 }
    
    
}
