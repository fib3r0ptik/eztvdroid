package com.hamaksoftware.eztvpal.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;

import java.util.UUID;

public class AppPref{
	
    private SharedPreferences _sharedPrefs;
    private Editor editor;
	private static String uniqueID = null;
	private static final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";
    private Context c;
    
    public AppPref(Context context) {
        //this._sharedPrefs = context.getSharedPreferences(APP_SHARED_PREFS, Activity.MODE_PRIVATE);
        c = context;
    	_sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    	editor = _sharedPrefs.edit();
    }

    public SharedPreferences getPreference(){
    	return _sharedPrefs;
    }

    public String getClientName(){
        return _sharedPrefs.getString("client_name", "");
    }
    public String getClientIPAddress(){
        return _sharedPrefs.getString("client_host", "");
    }

    public int getClientPort(){
        return Integer.parseInt(_sharedPrefs.getString("client_port", "8080"));
    }

    public boolean getAuth(){
        return _sharedPrefs.getBoolean("use_auth", true);
    }

    public String getClientUsername(){
        return _sharedPrefs.getString("client_username", "");
    }

    public String getClientPassword(){
        return _sharedPrefs.getString("client_password", "");
    }
    public String getClientType(){
        return _sharedPrefs.getString("client_type", "");
    }

    public int getConnectionTimeout(){
    	return Integer.valueOf(_sharedPrefs.getString("connection_timeout", "5"));
    }
    
    public int getRequestTimeout(){
    	return Integer.valueOf(_sharedPrefs.getString("request_timeout", "10"));
    }
    
    public int getFeedRefreshInterval(){
    	return Integer.valueOf(_sharedPrefs.getString("refresh_interval_feed", "15"));
    }
    
    public int getRefreshInterval(){
    	return Integer.valueOf(_sharedPrefs.getString("refresh_interval_torrent", "15"));
    }
    
    
    public void setRefreshInterval(int val){
    	editor.putString("refresh_interval_torrent", val+"");
    	editor.commit();
    }
    
    public void setFeedCache(String feed_cache){
    	editor.putString("feed_cache", feed_cache);
    	editor.commit();
    }
    
    public String getFeedCache(){
    	return _sharedPrefs.getString("feed_cache", "");
    }
    
    public String getEZTVUsername(){
    	return _sharedPrefs.getString("eztv_username", "");
    }
    
    public String getEZTVPassword(){
    	return _sharedPrefs.getString("eztv_password", "");
    }
    
    public void setEZTVUsername(String username){
    	editor.putString("eztv_username", username);
    	editor.commit();
    }
    
   public void setEZTVPassword(String password){
	   	editor.putString("eztv_password", password);
	   	editor.commit();
    }
   
   
   public String getEZTVHashedPassword(){
   	return _sharedPrefs.getString("eztv_hashed_password", "");
   }
   
   
  public void setEZTVHashedPassword(String password){
	   	editor.putString("eztv_hashed_password", password);
	   	editor.commit();
   }
   
   public void setMyShowCache(String data){
	   	editor.putString("shows_cache", data);
	   	editor.commit();
   }
   
   public String getMyshowCache(){
	   return _sharedPrefs.getString("shows_cache", "");
   }
   
  public void setEZTVLatestEpisodeCache(String data){
	   	editor.putString("eztv_latest_cache", data);
	   	editor.commit();
  }
  
  public String getEZTVLatestEpisodeCache(){
	   return _sharedPrefs.getString("eztv_latest_cache", "");
  }
   
  public void setEZTVShowsCache(String data){
	   	editor.putString("eztv_shows_cache", data);
	   	editor.commit();
}

public String getEZTVShowsCache(){
	return _sharedPrefs.getString("eztv_shows_cache", "");
}
  
  
  public void setEZTVRequestSesID(String data){
	   	editor.putString("eztv_ses_id", data);
	   	editor.commit();
  }

  public String getEZTVRequestSesID(){
	  	return _sharedPrefs.getString("eztv_ses_id", "");
  }
  
  public boolean getUseSubscription(){
	 return _sharedPrefs.getBoolean("use_subscription", false);
  }
	  
  public boolean getUseEZTV(){
	 return _sharedPrefs.getBoolean("use_eztv", false);
  }
  
  public void setUseSubscription(boolean data){
   	editor.putBoolean("use_subscription", data);
   	editor.commit();
  }
  
  
  public String getDeviceRegId(){
	  return _sharedPrefs.getString("device_reg_id", "");
  }
  
  public void setDeviceRegId(String data){
	   	editor.putString("device_reg_id", data);
	   	editor.commit();
  }
  
  public int getAppRegVersion(){
	  return _sharedPrefs.getInt("app_reg_version", Integer.MIN_VALUE);
  }
  
  public void setAppRegVersion(String data){
	   	editor.putInt("app_reg_version", Integer.MIN_VALUE);
	   	editor.commit();
  }
  
  public boolean getAutoSend(){
	 return _sharedPrefs.getBoolean("auto_send_torrent", false);
  }
	
  public int getAutoSendQuality(){
	  return Integer.parseInt(_sharedPrefs.getString("auto_send_quality", "1"));
  }

  
  public void setAutoSend(boolean data){
   	editor.putBoolean("auto_send_torrent", data);
   	editor.commit();
  }

  public void setDeviceId(String id){
      editor.putString("info",id).commit();
  }
   
	public synchronized String getDeviceId() {
        String id = Secure.getString(c.getContentResolver(),Secure.ANDROID_ID);
        if(id != null) {
            setDeviceId(id);
            return id;
        }
	    if (uniqueID == null) {
	        uniqueID = _sharedPrefs.getString(PREF_UNIQUE_ID, null);
	        if (uniqueID == null) {
	            uniqueID = UUID.randomUUID().toString();
                setDeviceId(uniqueID);
	        }
	    }
	    return uniqueID;
	}
  
  
  
  
}
