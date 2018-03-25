package com.merseyside.admin.player.Utilities;

/**
 * Created by Admin on 21.12.2016.
 */

public class ServiceConstants {
    public interface ACTION {
        String PREV_ACTION = "com.merseyside.admin.player.action.prev";
        String PLAY_ACTION = "com.merseyside.admin.player.action.play";
        String NEXT_ACTION = "com.merseyside.admin.player.action.next";
        String STARTFOREGROUND_ACTION = "com.merseyside.admin.player.action.startforeground";
        String STOPFOREGROUND_ACTION = "com.merseyside.admin.player.action.stopforeground";
        String BECOME_NOISY = "become_noisy";
        String CALLING_START = "calling_start";
        String CALLING_END = "calling_end";
        String AUDIOFOCUS_GAIN = "audiofocus_gain";
        String AUDIOFOCUS_LOSS = "audiofocus_loss";
        String AUDIOFOCUS_DUCK = "audiofocus_duck";
        String AUDIOFOCUS_TRANS= "audiofocus_trans";
        String HEADSET_PLUGGED = "headset_plugged";
        String CLOSE_ACTION = "com.merseyside.admin.player.action.close";
    }
   public interface NOTIFICATION_ID {
        int FOREGROUND_SERVICE = 101;
    }
}
