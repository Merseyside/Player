package com.merseyside.admin.player.ActivitesAndFragments;

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
import com.merseyside.admin.player.Utilities.DBHelper;
import com.merseyside.admin.player.Utilities.PrintString;
import com.merseyside.admin.player.Utilities.Settings;

import java.util.HashMap;

/**
 * Created by Admin on 10.01.2017.
 */

public class LibraryFragment extends Fragment implements View.OnClickListener{
    private RelativeLayout all_tracks, rated, megamix, comment, recently_added;
    private PlaylistTracks tracks_fragment;
    private TextView header_textView;
    private ImageView header;
    private Settings settings;

    android.support.v4.app.FragmentTransaction fTrans;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.library_fragment, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        settings = new Settings(getActivity());

        header = (ImageView) getView().findViewById(R.id.library_header);
        PrintString.printLog("lifeCycle", Settings.getScreenHeight() + " " + Settings.getScreenWidth());
        header.setImageBitmap(Settings.library_header);

        header_textView = (TextView) getView().findViewById(R.id.library_textview);
        settings.setTextViewFont(header_textView, null);

        all_tracks = (RelativeLayout) getView().findViewById(R.id.all_tracks_layout);
        all_tracks.setOnClickListener(this);

        rated = (RelativeLayout) getView().findViewById(R.id.rated_layout);
        rated.setOnClickListener(this);

        megamix = (RelativeLayout) getView().findViewById(R.id.megamix_tracks_layout);
        megamix.setOnClickListener(this);

        comment = (RelativeLayout) getView().findViewById(R.id.commented_tracks_layout);
        comment.setOnClickListener(this);

        recently_added = (RelativeLayout) getView().findViewById(R.id.recently_added_layout);
        recently_added.setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        HashMap<String, String> data = new HashMap<String, String>();
        fTrans = getActivity().getSupportFragmentManager().beginTransaction();
        fTrans.setCustomAnimations(R.anim.slide_right_to_left, R.anim.slide_in_left,R.anim.slide_left_to_right, R.anim.slide_in_right);
        data.put("image", "");
        data.put("id", "");
        switch(view.getId()){
            case R.id.all_tracks_layout:
                data.put("table", DBHelper.ALL_TRACKS);
                break;
            case R.id.rated_layout:
                data.put("table", DBHelper.RATED);
                break;
            case R.id.megamix_tracks_layout:
                data.put("table", DBHelper.MEGAMIX);
                break;
            case R.id.commented_tracks_layout:
                data.put("table", DBHelper.COMMENT);
                break;
            case R.id.recently_added_layout:
                data.put("table", DBHelper.RECENTLY_ADDED);
                break;
        }
        tracks_fragment = new PlaylistTracks();
        tracks_fragment.putArguments(data);
        fTrans.replace(R.id.main_fragment, tracks_fragment);
        fTrans.addToBackStack(null);
        fTrans.commit();
    }
}
