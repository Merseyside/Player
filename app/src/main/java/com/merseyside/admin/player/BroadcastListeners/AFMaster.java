package com.merseyside.admin.player.BroadcastListeners;

import android.media.AudioManager;

import com.merseyside.admin.player.Utilities.Settings;

/**
 * Created by Admin on 19.12.2016.
 */

public class AFMaster implements AudioManager.OnAudioFocusChangeListener {

    public interface MyAudioFocusListener{
        void audioFocusGain();
        void audioFocusTransient();
        void audioFocusTransientCanDuck();
        void audioFocusLoss();
    }

    private MyAudioFocusListener listener;

    public void setMyAudioFocusListener(MyAudioFocusListener myAudioFocusListener){
        this.listener = myAudioFocusListener;
    }

    public boolean getAudioFocus(AudioManager audioManager){
        if (Settings.AUDIOFOCUS) {
            int durationHint = AudioManager.AUDIOFOCUS_GAIN;

            int requestResult = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, durationHint);
            return requestResult == 1;
        }
        else return false;
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        if (Settings.AUDIOFOCUS) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS:
                    listener.audioFocusLoss();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    listener.audioFocusTransient();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    listener.audioFocusTransientCanDuck();
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    listener.audioFocusGain();
                    break;
            }
        }
    }
}
