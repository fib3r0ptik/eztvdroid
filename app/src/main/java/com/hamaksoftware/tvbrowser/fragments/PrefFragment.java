package com.hamaksoftware.tvbrowser.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.hamaksoftware.tvbrowser.R;
import com.hamaksoftware.tvbrowser.activities.Main;
import com.hamaksoftware.tvbrowser.asynctasks.VerifyClient;
import com.hamaksoftware.tvbrowser.utils.AppPref;
import com.hamaksoftware.tvbrowser.utils.Utility;

public class PrefFragment extends PreferenceFragment implements IAsyncTaskListener {
    private AppPref pref;
    protected Main base;
    private SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
            if (key.equalsIgnoreCase("client_type") || key.equalsIgnoreCase("client_name") ||
                    key.equalsIgnoreCase("client_host") || key.equalsIgnoreCase("client_port") ||
                    key.equalsIgnoreCase("use_auth") || key.equalsIgnoreCase("client_username") || key.equalsIgnoreCase("client_password")) {
                boolean canVerify = !pref.getClientType().equals("") && !pref.getClientIPAddress().equals("") &&
                        pref.getClientPort() > 0 && !pref.getClientUsername().equals("") && !pref.getClientPassword().equals("");

                if (canVerify) {
                    try {
                        VerifyClient verify = new VerifyClient(getActivity());
                        verify.asyncTaskListener = PrefFragment.this;
                        verify.execute();
                    }catch (Exception e){
                        base.showToast("Cannot verify at this time.",Toast.LENGTH_SHORT);
                    }
                }
            }
        }
    };

    public PrefFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        pref = new AppPref(getActivity());
        SharedPreferences _pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        _pref.registerOnSharedPreferenceChangeListener(listener);
        base = (Main) getActivity();
        base.toggleHintLayout(false);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        base.currentFragmentTag = 0;
        base.invalidateOptionsMenu();
        base.setTitle(getString(R.string.app_name));
        base.toggleHintLayout(true);
    }


    @Override
    public void onTaskCompleted(Object data, String ASYNC_ID) {
        if (data != null) {
            Boolean success = (Boolean) data;
            String msg = success ? getString(R.string.message_verify_profile_success) : getString(R.string.message_verify_profile_failed);
            Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
            if (success) {
                Utility.getInstance(getActivity()).saveProfile();
            }
        }
    }

    @Override
    public void onTaskWorking(String ASYNC_ID) {
        Toast.makeText(getActivity(),
                getString(R.string.message_verify_profile_working).replace("$", pref.getClientIPAddress()), Toast.LENGTH_LONG).show();
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
