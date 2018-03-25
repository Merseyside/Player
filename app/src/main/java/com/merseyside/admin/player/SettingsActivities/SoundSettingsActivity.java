package com.merseyside.admin.player.SettingsActivities;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.merseyside.admin.player.R;
import com.merseyside.admin.player.Utilities.Settings;

/**
 * Created by Admin on 28.12.2016.
 */

public class SoundSettingsActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.sound_settings);
    }
}
