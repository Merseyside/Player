package com.merseyside.admin.player.BroadcastListeners;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import com.merseyside.admin.player.Utilities.Settings;

/**
 * Created by Admin on 19.12.2016.
 */

public class MyBroadcastReceiver {

    public NoisyAudioStreamReceiver myNoisyAudioStreamReceiver = new NoisyAudioStreamReceiver();
    private MyBroadcastListener listener;

    public interface MyBroadcastListener{
        void becomeNoisy();
    }

    public MyBroadcastReceiver(MyBroadcastListener listener){
        this.listener = listener;

    }

    public class NoisyAudioStreamReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction()) && Settings.HEADSET_PAUSE) {
                listener.becomeNoisy();
            }
        }
    }
}
