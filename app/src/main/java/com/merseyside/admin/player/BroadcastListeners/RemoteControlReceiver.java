package com.merseyside.admin.player.BroadcastListeners;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.KeyEvent;

import com.merseyside.admin.player.Utilities.Settings;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Admin on 19.12.2016.
 */

public class RemoteControlReceiver extends BroadcastReceiver {

    private static MyRemoteControlListener listener;
    public interface MyRemoteControlListener{
        void playButtonPressed();
        void nextTrackButtonPressed();
        void previousTrackButtonPressed();
        void headsetPlugged();
    }

    private static boolean isAvailable = true;
    private Timer mTimer;
    private MyTimerTask myTimer;

    public void setRemoteControlListener(MyRemoteControlListener listener){
        this.listener = listener;
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_MEDIA_BUTTON.equals(action)) {
            if (Settings.PROCESS_PRESSING){
                KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                if (isAvailable) {
                    if (mTimer != null) mTimer.cancel();
                    if (myTimer != null) myTimer.cancel();
                    mTimer = new Timer();
                    myTimer = new MyTimerTask();
                    if (KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE == event.getKeyCode()) {
                        listener.playButtonPressed();
                    } else if (KeyEvent.KEYCODE_MEDIA_PLAY == event.getKeyCode()) {
                        listener.playButtonPressed();
                    } else if (KeyEvent.KEYCODE_MEDIA_PAUSE == event.getKeyCode()) {
                        listener.playButtonPressed();
                    } else if (KeyEvent.KEYCODE_MEDIA_STOP == event.getKeyCode()) {
                        listener.playButtonPressed();
                    } else if (KeyEvent.KEYCODE_MEDIA_NEXT == event.getKeyCode()) {
                        listener.nextTrackButtonPressed();
                    } else if (KeyEvent.KEYCODE_MEDIA_PREVIOUS == event.getKeyCode()) {
                        listener.previousTrackButtonPressed();
                    }

                    isAvailable = false;
                    mTimer.schedule(myTimer, 500);
                }
            }
        } else if (action.equals(Intent.ACTION_HEADSET_PLUG)) {
            int state = intent.getIntExtra("state", -1);
            switch (state) {
                case 0:

                    break;
                case 1:
                    if (Settings.START_HEADSET) listener.headsetPlugged();
                    break;

            }
        }
        else if (action.equals(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED)) {
            int state = intent.getIntExtra(BluetoothA2dp.EXTRA_STATE, BluetoothA2dp.STATE_DISCONNECTED);
            if (state == BluetoothA2dp.STATE_CONNECTED) {
                if (Settings.START_BLUETOOTH)
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(6000);
                                Handler mainHandler = new Handler(context.getMainLooper());
                                Runnable myRunnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        listener.headsetPlugged();
                                    }
                                };
                                mainHandler.post(myRunnable);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
            } else if (state == BluetoothA2dp.STATE_DISCONNECTED) {}
        }
    }

    public class MyTimerTask extends TimerTask {

        @Override
        public void run() {
            isAvailable = true;
        }
    }
}