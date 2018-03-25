package com.merseyside.admin.player.AdaptersAndItems;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Log;

import com.merseyside.admin.player.R;

/**
 * Created by Admin on 17.05.2017.
 */

public class Transition {

    private String path;
    private String name;
    private int duration;
    private int turningPoint;
    private boolean needsToIncrease, needsToFade;

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getTurningPoint() {
        return turningPoint;
    }

    public void setTurningPoint(int turningPoint) {
        this.turningPoint = turningPoint;
    }

    public boolean isNeedsToIncrease() {
        return needsToIncrease;
    }

    public void setNeedsToIncrease(boolean needsToIncrease) {
        this.needsToIncrease = needsToIncrease;
    }

    public boolean isNeedsToFade() {
        return needsToFade;
    }

    public void setNeedsToFade(boolean needsToFade) {
        this.needsToFade = needsToFade;
    }

    public Transition(String name, String path, int duration, int turningPoint, boolean needsToIncrese, boolean needsToFade){
        this.name = name;
        this.path = path;
        this.duration = duration;
        this.turningPoint = turningPoint;
        this.needsToIncrease = needsToIncrese;
        this.needsToFade = needsToFade;
    }

    /*public static void getDurations(Context context){
        Uri mediaPath;
        MediaMetadataRetriever mmr;
        mediaPath = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.transition_1);
        mmr = new MediaMetadataRetriever();
        mmr.setDataSource(context, mediaPath);
        Log.d("Duration", mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));

        mediaPath = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.transition_2);
        mmr = new MediaMetadataRetriever();
        mmr.setDataSource(context, mediaPath);
        Log.d("Duration", mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));

        mediaPath = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.transition_3);
        mmr = new MediaMetadataRetriever();
        mmr.setDataSource(context, mediaPath);
        Log.d("Duration", mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));

        mediaPath = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.transition_4);
        mmr = new MediaMetadataRetriever();
        mmr.setDataSource(context, mediaPath);
        Log.d("Duration", mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));

        mediaPath = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.transition_5);
        mmr = new MediaMetadataRetriever();
        mmr.setDataSource(context, mediaPath);
        Log.d("Duration", mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));

        mediaPath = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.transition_6);
        mmr = new MediaMetadataRetriever();
        mmr.setDataSource(context, mediaPath);
        Log.d("Duration", mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));

        mediaPath = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.transition_7);
        mmr = new MediaMetadataRetriever();
        mmr.setDataSource(context, mediaPath);
        Log.d("Duration", mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
    }*/

}
