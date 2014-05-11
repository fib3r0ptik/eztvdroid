package com.hamaksoftware.eztvdroid.fragments;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.hamaksoftware.eztvdroid.R;
import com.hamaksoftware.eztvdroid.asynctasks.VerifyClient;
import com.hamaksoftware.eztvdroid.utils.AppPref;

public class PrefFragment extends PreferenceFragment implements IAsyncTaskListener{

    private SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
            AppPref pref = new AppPref(getActivity());
            boolean canVerify = !pref.getClientType().equals("") && !pref.getClientIPAddress().equals("") &&
                    pref.getClientPort() > 0 && !pref.getClientUsername().equals("") && !pref.getClientPassword().equals("");

            if(canVerify){
                VerifyClient verify = new VerifyClient(getActivity());
                verify.asyncTaskListener = PrefFragment.this;
                verify.execute();
            }
        }
    };

    public PrefFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        SharedPreferences _pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        _pref.registerOnSharedPreferenceChangeListener(listener);
    }


    @Override
    public void onTaskCompleted(Object data, String ASYNC_ID) {
        if(data !=null){
            Boolean success = (Boolean)data;
            String msg  = success? getString(R.string.message_verify_profile_success):getString(R.string.message_verify_profile_failed);
            Toast.makeText(getActivity(),msg,Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onTaskWorking(String ASYNC_ID) {

    }

    @Override
    public void onTaskProgressUpdate(int progress, String ASYNC_ID) {

    }

    @Override
    public void onTaskProgressMax(int max, String ASYNC_ID) {

    }

    @Override
    public void onTaskUpdateMessage(String message, String ASYNC_ID) {

    }

    @Override
    public void onTaskError(Exception e, String ASYNC_ID) {

    }
}
