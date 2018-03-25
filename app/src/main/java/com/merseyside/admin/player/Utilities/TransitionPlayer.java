package com.merseyside.admin.player.Utilities;

import android.content.Context;
import android.net.Uri;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.merseyside.admin.player.ActivitesAndFragments.Player_Fragment;
import com.merseyside.admin.player.AdaptersAndItems.Transition;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Admin on 20.05.2017.
 */

public class TransitionPlayer extends Player{

    private Transition transition;
    private String url;
    private Timer mTimer;
    private PlayerKeeper keeper;
    private final int KEEPER_DELAY = 50;
    private boolean isTurningPoint = false;

    public TransitionPlayer(Context context){
        this.context = context;
    }


    private class PlayerKeeper extends TimerTask {
        @Override
        public void run() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (!isTurningPoint && getCurrentPosition() > transition.getTurningPoint()){
                        makeFading();
                        isTurningPoint = true;
                    }
                }
            }).start();
        }
    }

    private void makeFading(){
        final int fading = transition.getDuration() - transition.getTurningPoint();
        new Thread(new Runnable() {
            @Override
            public void run() {
                float volume = MAX_VOLUME;
                float delta_volume = MAX_VOLUME / 20;
                int sleepTime = fading / 20;
                while (volume > 0f) {
                    volume -= delta_volume;
                    setVolume(volume);
                    PrintString.printLog("FadingTransition", "Fading " +  String.valueOf(volume));
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void makeOppositeFading(){
        final int increase;
        increase = transition.getTurningPoint();
        new Thread(new Runnable() {
            @Override
            public void run() {
                float volume = 0f;
                int sleepTime = increase / 20;
                float delta_volume = MAX_VOLUME/20;
                while (volume < MAX_VOLUME) {
                    volume += delta_volume;
                    setVolume(volume);
                    PrintString.printLog("Volume", "Opposite " + String.valueOf(volume));
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void playTransition(final Transition transition, final int duration){

        createPlayer();
        this.transition = transition;
        this.url = transition.getPath();
        type = Player_Fragment.Type.MEMORY;
        Uri builtUri = Uri.parse(url);
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context, Util.getUserAgent(context, "MegamixPlayer"));
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        MediaSource streamSource = new ExtractorMediaSource(builtUri, dataSourceFactory, extractorsFactory, null, null);

        player.prepare(streamSource);
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (duration > transition.getTurningPoint()){
                    try {
                        Thread.sleep(duration - transition.getTurningPoint());
                    } catch (InterruptedException ignored) {}
                } else if (duration != 0){
                    seekTo(transition.getTurningPoint() - duration);
                }
                if (transition.isNeedsToIncrease()){
                    setVolume(0f);
                    makeOppositeFading();
                }
                player.setPlayWhenReady(true);
                if (transition.isNeedsToFade()) startPlayerKeeper();

            }
        }).start();
    }

    private void startPlayerKeeper(){
        resetTimers();
        mTimer.schedule(keeper, 0, KEEPER_DELAY);
        isTurningPoint = false;
    }

    private void resetTimers(){
        if (mTimer!= null) {
            mTimer.cancel();
            keeper.cancel();
        }
        mTimer = new Timer();
        keeper = new PlayerKeeper();
    }

    @Override
    public void start() {
        if (exist())
            player.setPlayWhenReady(true);
    }

    @Override
    public void pause() {
        if (exist())
            player.setPlayWhenReady(false);
    }

    @Override
    public int getDuration() {
        return (int) player.getDuration();
    }

    @Override
    public int getCurrentPosition() {
        try {
            return player.getDuration() == ExoPlayer.STATE_ENDED ? 0
                    : (int) player.getCurrentPosition();
        }catch (IllegalStateException e){
            return 0;
        }
        catch (NullPointerException e){
            return 0;
        }
    }

    @Override
    public void seekTo(int i) {
        if (exist()) player.seekTo(i);
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return false;
    }

    @Override
    public boolean canSeekBackward() {
        return false;
    }

    @Override
    public boolean canSeekForward() {
        return false;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {}

    @Override
    public void onLoadingChanged(boolean isLoading) {}

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == ExoPlayer.STATE_ENDED) releaseMP();
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {}

    @Override
    public void onPositionDiscontinuity() {}

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {}
}
