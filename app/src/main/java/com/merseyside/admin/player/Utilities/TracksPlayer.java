package com.merseyside.admin.player.Utilities;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.MediaController;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.merseyside.admin.player.ActivitesAndFragments.EqualizerFragment;
import com.merseyside.admin.player.ActivitesAndFragments.MainActivity;
import com.merseyside.admin.player.ActivitesAndFragments.Player_Fragment;
import com.merseyside.admin.player.AdaptersAndItems.Track;
import com.merseyside.admin.player.AdaptersAndItems.Transition;
import com.merseyside.admin.player.AdaptersAndItems.TransitionCreator;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by Admin on 13.12.2016.
 */

public class TracksPlayer extends Player {

    private boolean isLooping, isNotificated, isMiddleNotificated, transitionStarts;
    private Player_Fragment.Type type;
    private String url;
    private TrackDecoder decoder;
    private int silencePosition = 0;

    private Timer mTimer;
    private PlayerKeeper keeper;
    private final int KEEPER_DELAY = 100;
    private static boolean isEndOfTrackClose;
    private int crossfade;
    private Transition transition;
    protected Track track;
    private Thread fadingThread, increaseThread;

    protected void makeOppositeFading(){
        final int increase;
        if (track.isMegamixTrack()){
            if (track.getIncrease()==0) {
                setVolumeMax();
                return;
            }
            increase = track.getIncrease();
        }
        else increase = Settings.CROSSFADE;
        if (track.isMegamixTrack() || Settings.CROSSFADE > 0) {
            increaseThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    float volume = 0f;
                    int sleepTime = increase / 20;
                    float delta_volume = MAX_VOLUME/20;
                    while (volume < MAX_VOLUME && !Thread.currentThread().isInterrupted()) {
                        volume += delta_volume;
                        setVolume(volume);
                        PrintString.printLog("Volume", "Opposite " + String.valueOf(volume));
                        try {
                            Thread.sleep(sleepTime);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            });
            increaseThread.start();
        } else setVolumeMax();
    }

    protected void makeFading(){
        if (Settings.FADING || track.isMegamixTrack()) {
            final int fading;
            if (track.isMegamixTrack()) {
                if ((fading = track.getFading()) == 0) {
                    return;
                }
            }
            else fading = Settings.CROSSFADE;
            fadingThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    float volume = MAX_VOLUME;
                    float delta_volume = MAX_VOLUME / 20;
                    int sleepTime = fading / 20;
                    if (track.isMegamixTrack()) try {
                        Thread.sleep(track.getCrossfadeDuration() - fading);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    while (volume > 0f && !Thread.currentThread().isInterrupted()) {
                        volume -= delta_volume;
                        setVolume(volume);
                        PrintString.printLog("Fading", "Fading " + String.valueOf(volume));
                        try {
                            Thread.sleep(sleepTime);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            });
            fadingThread.start();
        }
    }

    private class PlayerKeeper extends TimerTask {
        @Override
        public void run() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (volume_changed) {
                        setVolume(MAX_VOLUME);
                        volume_changed = false;
                    }
                    if (speed_changed){
                        speed_changed = false;
                        setPlayerSpeed();
                    }
                    if (type != Player_Fragment.Type.STREAM) {

                        if (track.isMegamixTrack() && !isLooping) {
                            if (getCurrentPosition() >= track.getDurationForPlayer()) {
                                releaseMP();
                                if (!isNotificated && !isLooping) {
                                    Handler mainHandler = new Handler(context.getMainLooper());
                                    Runnable myRunnable = new Runnable() {
                                        @Override
                                        public void run() {
                                            myInterface.endOfTrack();
                                        }
                                    };
                                    mainHandler.post(myRunnable);
                                }
                            }
                        }

                        if (!isMiddleNotificated && getCurrentPosition() > track.getDurationForPlayer()/2){
                            myInterface.middleOfTrack();
                            isMiddleNotificated = true;
                            HashMap<String, String> map = new HashMap<>();
                            map.put("artist", track.getArtist());
                            map.put("name", track.getName());
                            if (track.isMegamixTrack()) FirebaseEngine.logEvent(context, "PLAYER_MEGAMIX", map);
                            else FirebaseEngine.logEvent(context, "PLAYER_TRACK", map);
                        }

                        if (transition != null && !transitionStarts){
                            if (getCurrentPosition() >= track.getDurationForPlayer() - track.getTransit_duration() - crossfade){
                                Handler mainHandler = new Handler(context.getMainLooper());
                                Runnable myRunnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        transitionPlayer.playTransition(transition, track.getTransit_duration());
                                        transitionStarts = true;
                                    }
                                };
                                mainHandler.post(myRunnable);
                            }
                        }

                        if (crossfade > 0 && !isNotificated && !isLooping) {
                            if (getCurrentPosition() >= (track.getDurationForPlayer() - (crossfade + silencePosition))) {
                                isNotificated = true;
                                isEndOfTrackClose = true;
                                PrintString.printLog("Service", crossfade + " " + silencePosition);
                                makeFading();
                                Handler mainHandler = new Handler(context.getMainLooper());
                                Runnable myRunnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        myInterface.endOfTrackIsClose();
                                    }
                                };
                                mainHandler.post(myRunnable);
                            }
                        }
                        if (Settings.SKIP_SILENCE && !isNotificated && !isLooping) {
                            if (getCurrentPosition() > (track.getDurationForPlayer() - silencePosition)) {
                                Handler mainHandler = new Handler(context.getMainLooper());
                                Runnable myRunnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        myInterface.endOfTrack();
                                    }
                                };
                                mainHandler.post(myRunnable);
                            }
                        }
                    }
                }
            }).start();
        }
    }

    private void startPlayerKeeper(){
        mTimer = new Timer();
        keeper = new PlayerKeeper();
        mTimer.schedule(keeper, 0, KEEPER_DELAY);
    }

    private void resetTimers(){
        if (mTimer!= null) {
            mTimer.cancel();
            keeper.cancel();
        }
    }

    @Override
    public void onLoadingChanged(boolean isLoading) {}
    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

        switch (playbackState){
            case ExoPlayer.STATE_READY:{
                break;
            }
            case ExoPlayer.STATE_ENDED:{
                if (isLooping){
                    playTrack(track);
                }
                else {
                    releaseMP();
                    if (!isNotificated) myInterface.endOfTrack();
                }
            }
        }
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {}
    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {}
    @Override
    public void onPlayerError(ExoPlaybackException error) {}
    @Override
    public void onPositionDiscontinuity() {}

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
    }

    public TracksPlayer(Context context){
        this.context = context;
        volume_changed = false;
        createPlayer();
    }

    public void setPlayerListener(MyPlayerInterface myPlayerInterface){
        myInterface = myPlayerInterface;
    }

    private MyPlayerInterface myInterface;

    public interface MyPlayerInterface{
        void endOfTrack();
        void endOfTrackIsClose();
        void middleOfTrack();
    }

    protected void createPlayer(){
        super.createPlayer();
        isMiddleNotificated = false;
        isNotificated = false;
    }

    public void playStream(String url) {
        createPlayer();
        type = Player_Fragment.Type.STREAM;
        Uri builtUri = Uri.parse(url);
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context, Util.getUserAgent(context, "MegamixPlayer"));
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        MediaSource streamSource = new ExtractorMediaSource(builtUri, dataSourceFactory, extractorsFactory, null, null);
        type = Player_Fragment.Type.STREAM;
        player.prepare(streamSource);
        player.setPlayWhenReady(true);
        startPlayerKeeper();
        FirebaseEngine.logEvent(context, "STREAM", null);
    }

    public void playTrack(final Track track) {
        createPlayer();
        this.track = track;
        this.url = track.getPath();
        Uri builtUri = Uri.parse(url);
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context, Util.getUserAgent(context, "MegamixPlayer"));
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        MediaSource streamSource = new ExtractorMediaSource(builtUri, dataSourceFactory, extractorsFactory, null, null);

        type = Player_Fragment.Type.MEMORY;
        player.prepare(streamSource);
        if (isEndOfTrackClose) {
            setVolume(0f);
            makeOppositeFading();
            isEndOfTrackClose = false;
        }
        if (track.isMegamixTrack()) seekTo(Integer.valueOf(track.getStartPoint()));
        player.setPlayWhenReady(true);

        if (track.isMegamixTrack()) {
            crossfade = track.getCrossfadeDuration();
            if (crossfade < 500) crossfade = 600;
            silencePosition = 0;
        } else {
            if (Settings.SKIP_SILENCE){
                decoder = new TrackDecoder(url, (int) track.getDurationLong());
                decoder.setMySilenceFindListener(new TrackDecoder.MySilenceFindListener() {
                    @Override
                    public void silenceFound(int silencePointMS) {
                        silencePosition = silencePointMS;
                    }

                    @Override
                    public void silenceNotFound(int silencePointMS) {
                        silencePosition = silencePointMS;
                    }
                });
                decoder.execute();
            }
            crossfade = Settings.CROSSFADE;
        }
        startPlayerKeeper();
        try {
            transitionStarts = false;
            transition = TransitionCreator.CreateTransition(track.getTransition(), context);
            if (transitionPlayer == null)transitionPlayer = new TransitionPlayer(context);
        } catch (IllegalArgumentException ignored){
            transition = null;
        }
    }

    @Override
    public void start() throws NullPointerException {
        if (exist())
        player.setPlayWhenReady(true);
        startPlayerKeeper();
    }

    @Override
    public void pause() {
        if (exist()) {
            player.setPlayWhenReady(false);
            resetTimers();
            if (transitionPlayer != null && transitionPlayer.isPlaying()){
                transitionPlayer.releaseMP();
            }
        }
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

    protected void setLooping(boolean isLooping){
        this.isLooping = isLooping;
    }

    @Override
    public int getBufferPercentage() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    public void onClick(PlaybackManager.Action action){
        super.onClick(action);
        if (transitionPlayer!=null && transitionPlayer.isPlaying()) transitionPlayer.onClick(PlaybackManager.Action.PLAY);
    }

    public void releaseMP() {
        super.releaseMP();
        resetTimers();
        if (fadingThread != null) fadingThread.interrupt();
        if (increaseThread != null) increaseThread.interrupt();
    }
}
