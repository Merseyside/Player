package com.merseyside.admin.player.ActivitesAndFragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.merseyside.admin.player.BuildConfig;
import com.merseyside.admin.player.R;
import com.merseyside.admin.player.Utilities.PrintString;

/**
 * Created by Admin on 25.05.2017.
 */

public class SplashActivity extends AppCompatActivity {
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private final int SPLASH_DISPLAY_TIME = 1000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent mainIntent = new Intent(SplashActivity.this,
                        MainActivity.class);
                SplashActivity.this.startActivity(mainIntent);
                SplashActivity.this.finish();
                handler.removeCallbacks(this);
            }
        }, SPLASH_DISPLAY_TIME);
    }
}
