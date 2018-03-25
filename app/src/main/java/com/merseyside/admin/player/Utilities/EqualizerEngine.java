package com.merseyside.admin.player.Utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.preference.PreferenceManager;

/**
 * Created by Admin on 22.01.2017.
 */

public class EqualizerEngine{

    private static final String APP_PREFERENCES_EQUALIZER_ENABLE = "equalizer_enable";
    private static final String APP_PREFERENCES_BASSBOOST_FREQUENCE = "bassboost_FREQUENCE";
    private static final String APP_PREFERENCES_EQUALIZER_FREQUENCE_1 = "equalizer_frequence_1";
    private static final String APP_PREFERENCES_EQUALIZER_FREQUENCE_2 = "equalizer_frequence_2";
    private static final String APP_PREFERENCES_EQUALIZER_FREQUENCE_3 = "equalizer_frequence_3";
    private static final String APP_PREFERENCES_EQUALIZER_FREQUENCE_4 = "equalizer_frequence_4";
    private static final String APP_PREFERENCES_EQUALIZER_FREQUENCE_5 = "equalizer_frequence_5";
    private static final String APP_PREFERENCES_EQUALIZER_VOLUME = "equalizer_volume";
    private static final String APP_PREFERENCES_EQUALIZER_PLAYBACK_SPEED = "equalizer_speed";

    private static int FREQUENCES[];
    private static boolean EQUALIZER_ENABLE;

    private static int VOLUME;
    private static int SPEED;

    private static Equalizer equalizer;
    private static BassBoost bassBoost;

    private com.merseyside.admin.player.Utilities.Settings settings;
    private Context context;

    public EqualizerEngine(Context context){
        FREQUENCES = new int[6];
        this.settings = new Settings(context);
        this.context = context;
        getEqualizerFrequences();
    }

    public Equalizer getEqualizer(){
        return equalizer;
    }

    public BassBoost getBassBoost(){
        return bassBoost;
    }

    public static float getSpeedFloat(){
        return ((float)(SPEED)*0.5f)/10f;
    }

    public static int getSpeedInt(){
        return SPEED;
    }

    public void setEqualizers(int audioSession){
        try {
            equalizer = new Equalizer(0, audioSession);
            equalizer.setEnabled(EQUALIZER_ENABLE);
            for (short i = 1; i < FREQUENCES.length; i++) {
                equalizer.setBandLevel((short) (i - 1), (short) FREQUENCES[i]);
            }
            bassBoost = new BassBoost(0, audioSession);
            bassBoost.setStrength((short) FREQUENCES[0]);
            bassBoost.setEnabled(EQUALIZER_ENABLE);
        } catch (IllegalArgumentException ignored){}
    }

    public int[] getFrequences(){
        return FREQUENCES;
    }

    public boolean getEqualizerEnable(){
        return EQUALIZER_ENABLE;
    }

    public void setEqualizerEnable(boolean isEnable) {
        EQUALIZER_ENABLE = isEnable;
        if (equalizer != null) equalizer.setEnabled(isEnable);
        if (bassBoost != null) bassBoost.setEnabled(isEnable);
    }

    public void saveEqualizerSettings(){
        settings.savePreference(APP_PREFERENCES_EQUALIZER_ENABLE, EQUALIZER_ENABLE);
        settings.savePreference(APP_PREFERENCES_BASSBOOST_FREQUENCE, FREQUENCES[0]);
        settings.savePreference(APP_PREFERENCES_EQUALIZER_FREQUENCE_1, FREQUENCES[1]);
        settings.savePreference(APP_PREFERENCES_EQUALIZER_FREQUENCE_2, FREQUENCES[2]);
        settings.savePreference(APP_PREFERENCES_EQUALIZER_FREQUENCE_3, FREQUENCES[3]);
        settings.savePreference(APP_PREFERENCES_EQUALIZER_FREQUENCE_4, FREQUENCES[4]);
        settings.savePreference(APP_PREFERENCES_EQUALIZER_FREQUENCE_5, FREQUENCES[5]);
        settings.savePreference(APP_PREFERENCES_EQUALIZER_VOLUME, VOLUME);
        settings.savePreference(APP_PREFERENCES_EQUALIZER_PLAYBACK_SPEED, SPEED);
    }

    public void setEqualiserBand(int pos, int value){
        FREQUENCES[pos+1] = value;
        if (equalizer != null) equalizer.setBandLevel((short) pos, (short)value);
    }

    public void setBassBoost(int value){
        FREQUENCES[0] = value;
        if (bassBoost != null) bassBoost.setStrength((short)value);
    }

    public void getEqualizerFrequences(){
        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(context);
        EQUALIZER_ENABLE = sPref.getBoolean(APP_PREFERENCES_EQUALIZER_ENABLE, false);
        FREQUENCES[0] = sPref.getInt(APP_PREFERENCES_BASSBOOST_FREQUENCE, 0);
        FREQUENCES[1] = sPref.getInt(APP_PREFERENCES_EQUALIZER_FREQUENCE_1, 0);
        FREQUENCES[2] = sPref.getInt(APP_PREFERENCES_EQUALIZER_FREQUENCE_2, 0);
        FREQUENCES[3] = sPref.getInt(APP_PREFERENCES_EQUALIZER_FREQUENCE_3, 0);
        FREQUENCES[4] = sPref.getInt(APP_PREFERENCES_EQUALIZER_FREQUENCE_4, 0);
        FREQUENCES[5] = sPref.getInt(APP_PREFERENCES_EQUALIZER_FREQUENCE_5, 0);
        VOLUME = sPref.getInt(APP_PREFERENCES_EQUALIZER_VOLUME, 100);
        SPEED = sPref.getInt(APP_PREFERENCES_EQUALIZER_PLAYBACK_SPEED, 20);
    }

    public static float getVolumeFloat(){
        return (float)(VOLUME)/100f;
    }

    public static int getVolumeInt(){
        return VOLUME;
    }

    public void setVolume(int volume){
        VOLUME = volume;
        Player.setMaxVolume((float)VOLUME / 100f, true);
    }

    public void setSpeed(float speed){
        SPEED = (int) ((speed*10)/0.5);
        Player.setSpeed(speed);
    }

}
