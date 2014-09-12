package com.hamaksoftware.tvbrowser.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import com.hamaksoftware.tvbrowser.R;
import com.hamaksoftware.tvbrowser.utils.Utility;

public class Splash extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Utility.getInstance(getApplicationContext()).registerInBackground();
        Utility.getInstance(getApplicationContext()).getProfile();

        setContentView(R.layout.splash);

        TextView t = (TextView) findViewById(R.id.spalsh_version);
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            int versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
            t.setText("Version " + versionName + " b" + versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            t.setVisibility(View.GONE);
        }


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent mainIntent = new Intent(Splash.this, Main.class);
                Splash.this.startActivity(mainIntent);
                Splash.this.finish();
            }
        }, 3000);

    }

}
