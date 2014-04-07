package com.hamaksoftware.eztvdroid.utils;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class ProfileHandler extends DBHandler {
	ArrayList<ClientProfile> profiles;
    
	public ProfileHandler(Context context) {
		super(context);
		profiles = new ArrayList<ClientProfile>(0);
		super.onCreate(this.getWritableDatabase());
	}

	
	public void addProfile(ClientProfile profile) {
 	   SQLiteDatabase db = this.getWritableDatabase();
 	   
 	    ContentValues values = new ContentValues();
 	    values.put(KEY_NAME, profile.name);
 	    values.put(KEY_HOST, profile.host); 
 	    values.put(KEY_PORT, profile.port); 
 	    values.put(KEY_UID, profile.username);
 	    values.put(KEY_PWD, profile.password); 
 	    values.put(KEY_AUTH, profile.useAuth);
 	    values.put(KEY_TYPE, profile.clientType);
 	    
 	    db.insert(TABLE_PROFILES, null, values);
 	    db.close();
 }
 
	 // Getting single contact
	 public ClientProfile getProfile(int id) {
		   SQLiteDatabase db = this.getReadableDatabase();
		   
		    Cursor cursor = db.query(TABLE_PROFILES, new String[] { KEY_PROFILE_ID,
		            KEY_NAME, KEY_HOST, KEY_PORT, KEY_UID, KEY_PWD, KEY_AUTH, KEY_TYPE}, KEY_PROFILE_ID + "=?",
		            new String[] { String.valueOf(id) }, null, null, null, null);
		    if (cursor != null)
		        cursor.moveToFirst();
		    
		    ClientProfile profile = new ClientProfile();
		    profile.id = Integer.parseInt(cursor.getString(0));
		    profile.name = cursor.getString(1);
		    profile.host = cursor.getString(2);
		    profile.port = cursor.getInt(3);
		    profile.username = cursor.getString(4);
		    profile.password = cursor.getString(5);
		    profile.useAuth = cursor.getInt(6)==1?true:false;
		    profile.clientType = cursor.getInt(7);
		    
		    cursor.close();
		    db.close();
		    return profile;
	 }

	 public int getCount(){
		 int count = 0;
		 String selectQuery = "SELECT  count(*) FROM " + TABLE_PROFILES;
		    SQLiteDatabase db = this.getReadableDatabase();
		    Cursor cursor = db.rawQuery(selectQuery, null);
		    if (cursor.moveToFirst()) {
		        do {
		        	count = cursor.getInt(0);
		        } while (cursor.moveToNext());
		    }
		  
		  return count;  
	 }
	 
	 
	 
	 public ArrayList<ClientProfile> getAllProfiles() {
		    String selectQuery = "SELECT  * FROM " + TABLE_PROFILES;
		 
		    SQLiteDatabase db = this.getReadableDatabase();
		    Cursor cursor = db.rawQuery(selectQuery, null);
	
		    if (cursor.moveToFirst()) {
		        do {
		            ClientProfile profile = new ClientProfile();
				    profile.id = Integer.parseInt(cursor.getString(0));
				    profile.name = cursor.getString(1);
				    profile.host = cursor.getString(2);
				    profile.port = cursor.getInt(3);
				    profile.username = cursor.getString(4);
				    profile.password = cursor.getString(5);
				    profile.useAuth = cursor.getInt(6)==1?true:false;
				    profile.clientType = cursor.getInt(7);
		            profiles.add(profile);
		        } while (cursor.moveToNext());
		    }
		 
		    cursor.close();
		    db.close();
		    return profiles;
	 }
	  


	 public int updateProfile(ClientProfile profile) {
	    SQLiteDatabase db = this.getWritableDatabase();
	    
	    ContentValues values = new ContentValues();
	    values.put(KEY_NAME, profile.name);
	    values.put(KEY_HOST, profile.host); 
	    values.put(KEY_PORT, profile.port); 
	    values.put(KEY_UID, profile.username);
	    values.put(KEY_PWD, profile.password); 
	    values.put(KEY_AUTH, profile.useAuth);
	    values.put(KEY_TYPE, profile.clientType);
	    
	    return db.update(TABLE_PROFILES, values, KEY_PROFILE_ID + " = ?",
	            new String[] { String.valueOf(profile.id) });
	 }
	  
	 public void deleteProfile(ClientProfile profile) {
		   SQLiteDatabase db = this.getWritableDatabase();
		    db.delete(TABLE_PROFILES, KEY_PROFILE_ID + " = ?",
		            new String[] { String.valueOf(profile.id) });
		    db.close();
	 }
}
