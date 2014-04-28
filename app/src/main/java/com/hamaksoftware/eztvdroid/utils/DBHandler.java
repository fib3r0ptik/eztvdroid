package com.hamaksoftware.eztvdroid.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;



public class DBHandler extends SQLiteOpenHelper{
	private static final int DATABASE_VERSION = 6;
	private static final String DATABASE_NAME = "EZTVDB";
	final String TABLE_PROFILES = "profiles";
	final String TABLE_SHOWS = "myshows";
	
    final String KEY_PROFILE_ID = "client_id";
    final String KEY_NAME = "client_name";
    final String KEY_HOST = "client_host";
    final String KEY_PORT = "client_port";
    final String KEY_UID = "client_username";
    final String KEY_PWD = "client_password";
    final String KEY_AUTH = "client_useauth";
    final String KEY_TYPE = "client_type";
	
    
    final String KEY_SHOW_ID = "show_id";
    final String KEY_TITLE = "show_title";
    final String KEY_SHOWLINK = "show_link";
    final String KEY_STATUS = "show_status";
    final String KEY_ISSELECTED = "show_isselected";
    final String KEY_ISSUBSCRIBE = "show_issubscribe";

	public DBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
	 }

	@Override
	public void onCreate(SQLiteDatabase db) {

        String CREATE_PROFILES_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_PROFILES + "("
                + KEY_PROFILE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_NAME + " TEXT,"
                + KEY_HOST + " TEXT," +KEY_PORT+" TEXT, "+ KEY_UID + " TEXT," 
                + KEY_PWD + " TEXT," + KEY_AUTH + " INTEGER," + KEY_TYPE + " INTEGER)";
        db.execSQL(CREATE_PROFILES_TABLE);
        
        String CREATE_SHOWS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_SHOWS + "("
                + KEY_SHOW_ID + " INTEGER PRIMARY KEY," + KEY_TITLE + " TEXT,"
                + KEY_SHOWLINK + " TEXT," +KEY_STATUS+" TEXT, "+ KEY_ISSELECTED + " INTEGER,"
                + KEY_ISSUBSCRIBE + " INTEGER)";
        db.execSQL(CREATE_SHOWS_TABLE);
        db.close();
        //Log.i("sql","oncreate");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//Log.i("sql","upgrade");
		//db.execSQL("DROP TABLE IF EXISTS " + TABLE_SHOWS);
		//db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROFILES);
		onCreate(db);
		
	}
}
