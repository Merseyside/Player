package com.merseyside.admin.player.Utilities;

import android.content.Context;
import android.widget.MediaController;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.merseyside.admin.player.ActivitesAndFragments.Player_Fragment;
import com.merseyside.admin.player.AdaptersAndItems.Track;

/**
 * Created by Admin on 20.05.2017.
 */

public abstract class Player implements MediaController.MediaPlayerControl, ExoPlayer.EventListener{
    protected Context context;
    protected SimpleExoPlayer player;

    static float MAX_VOLUME;
    private static float SPEED;
    static boolean volume_changed, speed_changed;
    protected Player_Fragment.Type type;
    TransitionPlayer transitionPlayer;

    protected void createPlayer(){
        TrackSelector trackSelector = new DefaultTrackSelector();
        LoadControl loadControl = new DefaultLoadControl();

        player = ExoPlayerFactory.newSimpleInstance(context, trackSelector, loadControl);
        player.addListener(this);
        player.setAudioDebugListener(new AudioRendererEventListener() {
            @Override
            public void onAudioEnabled(DecoderCounters counters) {}
            @Override
            public void onAudioSessionId(int audioSessionId) {
                EqualizerEngine equalizerEngine = new EqualizerEngine(context);
                equalizerEngine.setEqualizers(audioSessionId);
            }
            @Override
            public void onAudioDecoderInitialized(String decoderName, long initializedTimestampMs, long initializationDurationMs) {}

            @Override
            public void onAudioInputFormatChanged(Format format) {}

            @Override
            public void onAudioTrackUnderrun(int bufferSize, long bufferSizeMs, long elapsedSinceLastFeedMs) {}

            @Override
            public void onAudioDisabled(DecoderCounters counters) {}
        });
        setMaxVolume(EqualizerEngine.getVolumeFloat(), false);
        setVolumeMax();
        setPlayerSpeed(EqualizerEngine.getSpeedFloat());

    }

    protected void setVolumeMin(){
        player.setVolume(0.2f);
    }

    protected void setVolumeMax(){
        if (exist()) player.setVolume(MAX_VOLUME);
    }

    protected void setVolume(float volume){
        PrintString.printLog("Volume", volume + "");
        if (exist()) player.setVolume(volume);
    }

    protected boolean exist(){
        return !(player == null);
    }

    protected static void setMaxVolume(float volume, boolean byEqualizer){
        MAX_VOLUME = volume;
        if (byEqualizer) volume_changed = true;
    }

    protected float getVolume(){
        if (exist())
            return player.getVolume();
        else return 0f;
    }

    protected void releaseMP(){
        if (player != null) {
            try {
                player.stop();
                player.release();
                player = null;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isPlaying(){
        if (exist())
            return player.getPlayWhenReady();
        else return false;
    }

    public static void setSpeed(float speed){
        speed_changed = true;
        SPEED = speed;
    }

    protected void setPlayerSpeed(){
        if (player!=null) player.setPlaybackParameters(new PlaybackParameters(SPEED, 1.0f));
    }

    private void setPlayerSpeed(float speed){
        SPEED = speed;
        if (player!=null)player.setPlaybackParameters(new PlaybackParameters(SPEED, 1.0f));
    }

    public void onClick(PlaybackManager.Action action)
    {
        if (player == null) return;
        else if (action == PlaybackManager.Action.PLAY)
        {
            if (type == Player_Fragment.Type.STREAM){
                if (!isPlaying()) {}
                else {
                    releaseMP();
                }
            }
            else {
                if (!isPlaying())
                    start();
                else {
                    pause();

                }
            }
        }
    }
}
