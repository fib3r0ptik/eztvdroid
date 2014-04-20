package com.hamaksoftware.eztvdroid.fragments;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import com.hamaksoftware.eztvdroid.R;
public class PrefFragment extends PreferenceFragment {

    public PrefFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

}
