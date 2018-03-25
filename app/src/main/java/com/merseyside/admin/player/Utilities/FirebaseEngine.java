package com.merseyside.admin.player.Utilities;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Admin on 15.05.2017.
 */

public class FirebaseEngine {
    private static FirebaseAnalytics mFirebaseAnalytics;

    public static void logEvent(Context context, String event, HashMap<String, String> params){
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
        Bundle bundle = new Bundle();
        if (params != null)
            for (Map.Entry<String, String> entry : params.entrySet()){
                String key = entry.getKey();
                String value = entry.getValue();
                bundle.putString(key, value);
            }
        mFirebaseAnalytics.logEvent(event, bundle);
    }
}
