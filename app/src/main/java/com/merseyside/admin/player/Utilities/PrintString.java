package com.merseyside.admin.player.Utilities;

import android.util.Log;
import java.util.HashMap;

/**
 * Created by Admin on 17.01.2017.
 */

public class PrintString {

    private static HashMap<String, Boolean> map;
    public PrintString(){
        map = new HashMap<>();
        map.put("lifeCycle", true);
        map.put("Slider", true);
        map.put("LastError", true);
        map.put("Track", true);
        map.put("regex", true);
        map.put("Fading", true);
        map.put("Service", true);
        map.put("Error", false);
        map.put("Focus", false);
        map.put("Equalizer", true);
        map.put("setPrepared", true);
        map.put("Volume", true);
        map.put("Rating", true);
        map.put("Stream", true);
        map.put("Preset", true);
        map.put("Adapter",  true);
        map.put("MegamixCreator", true);
        map.put("Char", true);
        map.put("Intent", true);
        map.put("Widget", true);
        map.put("Calendar", true);
        map.put("LastFm", true);
        map.put("Slider1", true);
        map.put("Parser", true);
        map.put("License", true);
        map.put("FadingTransition", true);
        map.put("RemoteConfig", true);
    }
    public static void printLog(String key, String str){
        try {
            if (map.containsKey(key) && map.get(key) && Settings.WRITE_LOG) Log.d(key, str);
        } catch(NullPointerException ignored){}
    }

    public void printStackTrace(){
        Log.d("Rating", Log.getStackTraceString(new Exception()));
    }
}
