package com.hamaksoftware.tvbrowser.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;

import java.util.UUID;

public class AppPref {

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

    public SharedPreferences getPreference() {
        return _sharedPrefs;
    }

    public void setClientName(String data) {
        editor.putString("client_name", data);
        editor.commit();
    }

    public String getClientName() {
        return _sharedPrefs.getString("client_name", "");
    }

    public void setClientIPAddress(String data) {
        editor.putString("client_host", data);
        editor.commit();
    }

    public String getClientIPAddress() {
        return _sharedPrefs.getString("client_host", "");
    }

    public void setClientPort(String data) {
        editor.putString("client_port", data);
        editor.commit();
    }

    public int getClientPort() {
        return Integer.parseInt(_sharedPrefs.getString("client_port", "8080"));
    }

    public void setAuth(boolean data) {
        editor.putBoolean("use_auth", data);
        editor.commit();
    }

    public boolean getAuth() {
        return _sharedPrefs.getBoolean("use_auth", true);
    }

    public void setClientUsername(String data) {
        editor.putString("client_username", data);
        editor.commit();
    }

    public String getClientUsername() {
        return _sharedPrefs.getString("client_username", "");
    }

    public void setClientPassword(String data) {
        editor.putString("client_password", data);
        editor.commit();
    }

    public String getClientPassword() {
        return _sharedPrefs.getString("client_password", "");
    }

    public void setClientType(String data) {
        editor.putString("client_type", data);
        editor.commit();
    }

    public String getClientType() {
        return _sharedPrefs.getString("client_type", "");
    }

    public int getConnectionTimeout() {
        return Integer.valueOf(_sharedPrefs.getString("connection_timeout", "5"));
    }

    public int getRequestTimeout() {
        return Integer.valueOf(_sharedPrefs.getString("request_timeout", "10"));
    }


    public boolean getUseSubscription() {
        return _sharedPrefs.getBoolean("use_subscription", false);
    }


    public String getDeviceRegId() {
        return _sharedPrefs.getString("device_reg_id", "");
    }

    public void setDeviceRegId(String data) {
        editor.putString("device_reg_id", data);
        editor.commit();
    }

    public int getAppRegVersion() {
        return _sharedPrefs.getInt("app_reg_version", Integer.MIN_VALUE);
    }

    public void setAppRegVersion(String data) {
        editor.putInt("app_reg_version", Integer.MIN_VALUE);
        editor.commit();
    }

    public boolean getAutoSend() {
        return _sharedPrefs.getBoolean("auto_send_torrent", false);
    }

    public int getAutoSendQuality() {
        return Integer.parseInt(_sharedPrefs.getString("auto_send_quality", "1"));
    }


    public void setDeviceId(String id) {
        editor.putString("info", id).commit();
    }

    public synchronized String getDeviceId() {
        String id = Secure.getString(c.getContentResolver(), Secure.ANDROID_ID);
        if (id != null) {
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
