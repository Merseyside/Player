package com.merseyside.admin.player.SettingsActivities;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.merseyside.admin.player.R;
import com.merseyside.admin.player.Utilities.Settings;

/**
 * Created by Admin on 03.02.2017.
 */

public class HeadsetBluetoothSettingsActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.headset_bluetooth_settings);
    }
}
