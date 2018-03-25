package com.merseyside.admin.player.ActivitesAndFragments;

import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.merseyside.admin.player.AdaptersAndItems.Track;
import com.merseyside.admin.player.R;
import com.merseyside.admin.player.Utilities.Settings;

import java.util.Locale;

/**
 * Created by Admin on 11.08.2016.
 */
public class Info extends android.support.v4.app.Fragment {

    private TextView title, artist, bitrate, duration, album, path, genre, location, date;
    private Track track;
    private Settings settings;
    private final String TRACK_KEY = "track";

    public void setTrack(Track track){
        this.track = track;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings = new Settings(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.info, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) track = savedInstanceState.getParcelable(TRACK_KEY);

        title = (TextView) getView().findViewById(R.id.info_name);
        artist = (TextView) getView().findViewById(R.id.info_artist);
        duration = (TextView) getView().findViewById(R.id.info_duration);
        bitrate = (TextView) getView().findViewById(R.id.info_bitrate);
        album = (TextView) getView().findViewById(R.id.info_album);
        path = (TextView) getView().findViewById(R.id.info_path);
        genre = (TextView) getView().findViewById(R.id.info_genre);
        location = (TextView) getView().findViewById(R.id.info_location);
        date = (TextView) getView().findViewById(R.id.info_date);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(TRACK_KEY, track);
    }

    @Override
    public void onStart() {
        super.onStart();

        MediaMetadataRetriever retriver = new MediaMetadataRetriever();
        try {
            retriver.setDataSource(track.getPath());
            retriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            title.setText(retriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
            if (title.getText().equals("")) title.setText(track.getName());
            artist.setText(retriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
            if (artist.getText().equals("")) artist.setText(track.getArtist());
            album.setText(retriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
            duration.setText(userInterfaceDuration(Long.valueOf(retriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION))));
            bitrate.setText(retriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE));
            path.setText(track.getPath());
            genre.setText(retriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE));
            location.setText(retriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_LOCATION));
            date.setText(settings.getDaysToDate(track.getDate()));
        }catch (RuntimeException ignored){}
    }

    public String userInterfaceDuration(long mils){
        int total = (int) mils/1000;
        int minutes = total / 60;
        int seconds = total % 60;
        return String.format(Locale.ENGLISH,"%d:%02d", minutes, seconds);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
