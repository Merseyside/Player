package com.merseyside.admin.player.SettingsActivities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.merseyside.admin.player.R;
import com.merseyside.admin.player.Utilities.PrintString;
import com.merseyside.admin.player.Utilities.Settings;

/**
 * Created by Admin on 28.12.2016.
 */

public class SettingsFragment extends Fragment implements View.OnClickListener{
    private RelativeLayout interface_layout, sound, other, headset_bluetooth, lastfm, megamix;
    private TextView header_textView;
    private ImageView header;
    private Settings settings;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.settings_fragment, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        settings = new Settings(getActivity());

        header = (ImageView) getView().findViewById(R.id.settings_header);
        PrintString.printLog("lifeCycle", Settings.getScreenHeight() + " " + Settings.getScreenWidth());
        header.setImageBitmap(Settings.settings_header);

        header_textView = (TextView) getView().findViewById(R.id.settings_textview);
        settings.setTextViewFont(header_textView, null);

        interface_layout = (RelativeLayout) getActivity().findViewById(R.id.interface_layout);
        interface_layout.setOnClickListener(this);

        sound = (RelativeLayout) getActivity().findViewById(R.id.sound_layout);
        sound.setOnClickListener(this);

        other = (RelativeLayout) getActivity().findViewById(R.id.other_layout);
        other.setOnClickListener(this);

        headset_bluetooth = (RelativeLayout) getActivity().findViewById(R.id.headset_bluetooth_layout);
        headset_bluetooth.setOnClickListener(this);

        lastfm = (RelativeLayout) getActivity().findViewById(R.id.lastfm_layout);
        lastfm.setOnClickListener(this);

        megamix = (RelativeLayout) getActivity().findViewById(R.id.megamix_layout);
        megamix.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent = null;
        switch(view.getId()){
            case R.id.interface_layout:
                intent = new Intent(getActivity(), InterfaceSettingsActivity.class);
                break;
            case R.id.sound_layout:
                intent = new Intent(getActivity(), SoundSettingsActivity.class);
                break;

            case R.id.other_layout:
                intent = new Intent(getActivity(), OtherSettingsActivity.class);
                break;

            case R.id.headset_bluetooth_layout:
                intent = new Intent(getActivity(), HeadsetBluetoothSettingsActivity.class);
                break;

            case R.id.lastfm_layout:
                intent = new Intent(getActivity(), LastfmSettingsActivity.class);
                break;

            case R.id.megamix_layout:
                intent = new Intent(getActivity(), MegamixSettingsActivity.class);
                break;
        }
        startActivity(intent);
    }
}
