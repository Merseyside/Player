package com.merseyside.admin.player.Utilities;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.google.firebase.FirebaseApp;
import com.merseyside.admin.player.ActivitesAndFragments.AboutPlayerFragment;
import com.merseyside.admin.player.Dialogs.InfoDialog;
import com.merseyside.admin.player.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Admin on 09.06.2016.
 */
public class Settings implements SharedPreferences.OnSharedPreferenceChangeListener {

    private SharedPreferences sPref;
    private SharedPreferences.Editor editor;
    private Context context;
    private ServiceConnection sConn;

    public interface ServiceListener{
        void onServiceConnected(PlaybackManager playbackManager);
        void onServiceDisconnected(PlaybackManager playbackManager);
    }

    private static ServiceListener serviceListener;

    private static PlaybackManager playbackManager;

    public static void setServiceListener(ServiceListener serviceListener1){
        serviceListener = serviceListener1;
        if (playbackManager!=null) serviceListener.onServiceConnected(playbackManager);
    }

    public void startIntentService()  throws NullPointerException{
        startIntentService = new Intent(context, PlaybackManager.class);
        startIntentService.setAction(ServiceConstants.ACTION.STARTFOREGROUND_ACTION);
        context.startService(startIntentService);
    }

    public native int getSomeValue();
    public native void init(Context context);

    private static int day_of_year;

    public static final String VIDEO_TAG_KEY = "videoTag";
    public static final String VIDEO_URL_KEY = "videoURL";
    public static final String VIDEO_TITLE_KEY = "videoTitle";
    public static final String VERSION_KEY = "version";

    public static final String CITRICA_FONT = "fonts/citrica.ttf";
    public static final String DEFAULT_FONT = "default";
    public static final String LAYS_FONT = "fonts/lays.ttf";

    public static final String APP_PREFERENCES_LAST_INSTANCE = "instance";
    public static final String APP_PREFERENCES_LAST_ITEM = "item";
    public static final String APP_PREFERENCES_LOOPING = "looping";
    public static final String APP_PREFERENCES_SHUFFLE = "shuffle";
    public static final String APP_PREFERENCES_SKIP_SILENCE = "silence";
    public static final String APP_PREFERENCES_CROSSFADE = "crossfade";
    public static final String APP_PREFERENCES_AUDIOFOCUS = "audiofocus";
    public static final String APP_PREFERENCES_FADING = "fading";
    public static final String APP_PREFERENCES_SLIDER_BUFFER = "sliderbuffer";
    public static final String APP_PREFERENCES_SLIDER_ANIMATION = "slideranimation";
    public static final String APP_PREFERENCES_THEME = "theme";
    public static final String APP_PREFERENCES_LANGUAGE = "language";
    public static final String APP_PREFERENCES_HEADSET_PAUSE = "headset_pause";
    public static final String APP_PREFERENCES_START_HEADSET = "start_headset";
    public static final String APP_PREFERENCES_START_BLUETOOTH = "start_bluetooth";
    public static final String APP_PREFERENCES_PROCESS_PRESSING = "process_pressing";
    public static final String APP_PREFERENCES_CONTINUE_AFTER_CALL = "continue_after_call";
    public static final String APP_PREFERENCES_MEMORY_VIEW = "memory_view";
    public static final String APP_PREFERENCES_MEGAMIX_DURATION = "megamix_duration";
    public static final String APP_PREFERENCES_SORTING_ORDER = "sorting_order";
    public static final String APP_PREFERENCES_FIRST_LAUNCH = "first_launch";
    public static final String APP_PREFERENCES_LOAD_NEXT_TRACK = "load_next_track";
    public static final String APP_PREFERENCES_LOAD_PREV_TRACK = "load_prev_track";
    public static final String APP_PREFERENCES_SHOW_MEGAMIX_WARNING_DIALOG = "megamix_warning_dialog";
    public static final String APP_PREFERENCES_REMEMBER_PASS = "remember_pass";
    public static final String APP_PREFERENCES_PASSWORD = "password";
    public static final String APP_PREFERENCES_USERNAME = "username";
    public static final String APP_PREFERENCES_LASTFM_VIEW = "lastfm_view";
    public static final String APP_PREFERENCES_WIFI = "wifi";
    public static final String APP_PREFERENCES_LASTFM_START = "lastfm_start";
    public static final String APP_PREFERENCES_SCROBBLE_LASTFM = "scrobble_lastfm";
    public static final String APP_PREFERENCES_NOW_PLAYING_LASTFM = "now_playing_lastfm";
    public static final String APP_PREFERENCES_RECENTLY_ADDED = "recently_added";
    public static final String APP_PREFERENCES_METADATA = "metadata";
    public static final String APP_PREFERENCES_ANIMATION = "animation";
    public static final String APP_PREFERENCES_GRID_SIZE = "grid_size";
    public static final String APP_PREFERENCES_LIST_SIZE = "list_size";
    public static final String APP_PREFERENCES_WRITE_LOG = "write_log";
    public static final String APP_PREFERENCES_SHOW_FOLDERS_INFO_DIALOG = "folder_info_dialog";
    public static final String APP_PREFERENCES_SHOW_PLAYLIST_INFO_DIALOG = "playlist_info_dialog";
    public static final String APP_PREFERENCES_SCREEN_WIDTH = "screen_width";
    public static final String APP_PREFERENCES_SCREEN_HEIGHT = "screen_height";
    public static final String APP_PREFERENCES_FONT = "font";
    public static final String APP_PREFERENCES_LICENSE = "license";
    public static final String APP_PREFERENCES_LICENSE_CHECKED = "license_checked";
    public static final String APP_PREFERENCES_TRACKS_VIEW = "tracks_view";
    public static final String APP_PREFERENCES_ROW_COUNT = "row_count";
    public static final String APP_PREFERENCES_START_SESSIONS = "start_sessions";
    public static final String APP_PREFERENCES_RATED = "app_rated";
    public static final String APP_PREFERENCES_IS_ORIGINAL = "is_original";
    public static final String APP_PREFERENCES_TRANSITION = "transition";
    public static final String APP_PREFERENCES_GREETING = "greeting";
    public static final String APP_PREFERENCES_VIDEO_TAG = "videoTag";
    public static final String APP_PREFERENCES_VIDEO_CLICKED = "video_clicked";
    public static final String APP_PREFERENCES_LAST_POSITION = "last_position";

    public static boolean SHUFFLE;
    public static String LAST_INSTANCE;
    public static String LAST_ITEM;
    public static boolean LOOPING;
    public static boolean SKIP_SILENCE;
    public static int CROSSFADE;
    public static boolean AUDIOFOCUS;
    public static boolean FADING;
    public static int SLIDER_BUFFER;
    public static String SLIDER_ANIMATION;
    public static String THEME;
    public static String LANGUAGE;
    public static boolean HEADSET_PAUSE;
    public static boolean START_HEADSET;
    public static boolean START_BLUETOOTH;
    public static boolean PROCESS_PRESSING;
    public static boolean CONTINUE_AFTER_CALL;
    public static String MEMORY_VIEW;
    public static float MEGAMIX_DURATION;
    public static boolean SORTING_ORDER;
    public static int FIRST_LAUNCH;
    public static boolean LOAD_NEXT_TRACK;
    public static boolean LOAD_PREV_TRACK;
    public static boolean SHOW_MEGAMIX_WARNING_DIALOG;
    public static boolean REMEMBER_PASS;
    public static String PASSWORD;
    public static String USERNAME;
    public static String LASTFM_VIEW;
    public static boolean WIFI;
    public static String LASTFM_START;
    public static boolean LASTFM_NOW_PLAYING;
    public static boolean LASTFM_SCROBBLE;
    public static int RECENTLY_ADDED;
    public static boolean METADATA;
    public static boolean ANIMATION;
    public static int GRID_SIZE;
    public static int LIST_SIZE;
    public static boolean WRITE_LOG;
    public static boolean FOLDERS_INFO_DIALOG;
    public static boolean PLAYLIST_INFO_DIALOG;
    public static int SCREEN_WIDTH;
    public static int SCREEN_HEIGHT;
    public static String FONT;
    public static boolean LICENSE;
    public static int LICENSE_CHECKED;
    public static boolean TRIAL_OVER;
    public static String TRACKS_VIEW;
    public static int ROW_COUNT;
    public static int START_SESSIONS;
    public static boolean RATED;
    public static boolean IS_ORIGINAL;
    public static int TRANSITION;
    public static boolean GREETING;
    public static String VIDEO_TAG;
    public static boolean VIDEO_CLICKED;
    public static int LAST_POSITION;

    private static DBHelper dbHelper;

    public static final String CALLER_ACTIVITY_ADD_EXTERNAL_PLAYLIST = "1";
    public static final String CALLER_ACTIVITY_ADD_TRACKS_TO_PLAYLIST = "2";
    public static final String CALLER_ACTIVITY_ADD_FOLDERS= "3";
    public static final int ORIGINAL = 1;
    public static final int NOT_ORIGINAL = 0;

    public static final String LIST_VIEW = "list_view";
    public static final String GRID_VIEW = "grid_view";

    public static final String FORMATS_PATTERN = "^.+\\.mp3$|^.+\\.wav$|^.+\\.ogg$|^.+\\.m4a$";

    private static com.merseyside.admin.player.Utilities.Point screen_dimensions;

    public final static int SILENCE_END_DURATION = 12000;
    public final static int CROSSFADE_AND_INCREASE_DURATION = 15000;
    private final static int TRIAL_DURATION = 30;

    private static final boolean PRO_VERSION = false;
    public static final String CURRENT_VERSION = "0.8.5";
    private static final String PRO_VERSION_PACKAGENAME = "com.merseyside.admin.exoplayer.pro";
    private static final String TRIAL_VERSION_PACKAGENAME = "com.merseyside.admin.exoplayer";

    public static Bitmap equalizer_header;
    public static Bitmap settings_header;
    public static Bitmap playlist_header;
    public static Bitmap memory_header;
    public static Bitmap stream_header;
    public static Bitmap library_header;
    public static Bitmap tracks_header;
    public static Bitmap track;
    public static Bitmap lastfm_header;

    private static File externalLocation, sdcardLocation;

    public static Intent startIntentService, stopIntentService;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
            Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE
    };

    public void freeMemory(){
        System.runFinalization();
        Runtime.getRuntime().gc();
        System.gc();
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    public boolean checkWifiOnAndConnected() {
        WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiMgr.isWifiEnabled()) {
            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
            if(wifiInfo.getNetworkId() == -1 ){
                return false;
            }
            return true;
        }
        else {
            return false;
        }
    }

    public Settings(Context context){
        this.context = context;
        PrintString ps = new PrintString();
        if (THEME == null || THEME.equals("")) {
            getAllPreferences();
        }
        if (dbHelper == null) dbHelper = new DBHelper(context);
    }

    public static DBHelper getDbHelper(){
        return dbHelper;
    }

    private void stopService(boolean stopAnyway){
        if (playbackManager != null) {
            if (!playbackManager.isNowPlaying() || stopAnyway) {
                try {
                    context.startService(stopIntentService);
                    playbackManager.updateWidget(true);
                } catch (NullPointerException ignored) {}
            }
        }
        PrintString.printLog("onDestroy", "onDestroy");
        if (sConn != null) {
            try {
                context.unbindService(sConn);
            } catch(IllegalArgumentException ignored){}
        }
    }

    public void bindService(Context context){
        startIntentService = new Intent(context, PlaybackManager.class);
        startIntentService.setAction(ServiceConstants.ACTION.STARTFOREGROUND_ACTION);
        stopIntentService = new Intent(context, PlaybackManager.class);
        stopIntentService.setAction(ServiceConstants.ACTION.STOPFOREGROUND_ACTION);

        sConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                playbackManager = ((PlaybackManager.MyBinder) iBinder).getService();
                playbackManager.setBind(true);
                PrintString.printLog("Service", "bind in settings");
                if (serviceListener!=null) serviceListener.onServiceConnected(playbackManager);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                if (serviceListener!=null) serviceListener.onServiceDisconnected(playbackManager);
            }
        };
        context.bindService(startIntentService, sConn, Context.BIND_AUTO_CREATE);
    }

    public void saveLogcatToFile() {
        if (Settings.WRITE_LOG) {
            String fileName = "playerLog.txt";
            Map<String, File> externalLocations = ExternalStorage.getAllStorageLocations();
            File sdCard = externalLocations.get(ExternalStorage.SD_CARD);
            File outputFile = new File(sdCard.getAbsolutePath(), fileName);
            PrintString.printLog("lifeCycle", outputFile.getAbsolutePath());
            try {
                @SuppressWarnings("unused")
                Process process = Runtime.getRuntime().exec("logcat -f " + outputFile.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void verifyStoragePermissions(Activity activity) {
        int writePermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
        int internetPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.INTERNET);
        int mountPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS);
        int networkPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_NETWORK_STATE);
        int wifiPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_WIFI_STATE);
        if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED
                || internetPermission != PackageManager.PERMISSION_GRANTED || mountPermission != PackageManager.PERMISSION_GRANTED
                || networkPermission != PackageManager.PERMISSION_GRANTED || wifiPermission != PackageManager.PERMISSION_GRANTED)  {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    private void decode_images() throws ArithmeticException{
        if (equalizer_header == null) equalizer_header = Settings.decodeSampledBitmapFromResource(context.getResources(), R.drawable.equalizer_header, Settings.getScreenWidth(), (int)(Settings.getScreenWidth()/2.4));
        if (settings_header == null) settings_header = Settings.decodeSampledBitmapFromResource(context.getResources(), R.drawable.settings_header, Settings.getScreenWidth(), (int)(Settings.getScreenWidth()/2.4));
        if (playlist_header == null) playlist_header = Settings.decodeSampledBitmapFromResource(context.getResources(), R.drawable.playlist_header, Settings.getScreenWidth(), (int)(Settings.getScreenWidth()/2.4));
        if (memory_header == null) memory_header = Settings.decodeSampledBitmapFromResource(context.getResources(), R.drawable.memory_header, Settings.getScreenWidth(), (int)(Settings.getScreenWidth()/2.4));
        if (library_header == null)  library_header = Settings.decodeSampledBitmapFromResource(context.getResources(), R.drawable.library_header, Settings.getScreenWidth(), (int)(Settings.getScreenWidth()/2.4));
        if (stream_header == null) stream_header = Settings.decodeSampledBitmapFromResource(context.getResources(), R.drawable.stream_header, Settings.getScreenWidth(), (int)(Settings.getScreenWidth()/2.4));
        if (tracks_header == null) tracks_header = Settings.decodeSampledBitmapFromResource(context.getResources(), R.drawable.tracks_header, Settings.getScreenWidth(), (int)(Settings.getScreenWidth()/2.4));
        if (lastfm_header == null) lastfm_header = Settings.decodeSampledBitmapFromResource(context.getResources(), R.drawable.lastfm_header, Settings.getScreenWidth(), (int)(Settings.getScreenWidth()/2.4));
        track = BitmapFactory.decodeResource(context.getResources(), getAttributeId(getThemeByString(), R.attr.theme_dependent_track_cover));
    }

    public void getAllPreferences() {
        PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(this);
        sPref = PreferenceManager.getDefaultSharedPreferences(context);
        LAST_INSTANCE = sPref.getString(APP_PREFERENCES_LAST_INSTANCE, "");
        LAST_ITEM = sPref.getString(APP_PREFERENCES_LAST_ITEM, "");
        LOOPING = sPref.getBoolean(APP_PREFERENCES_LOOPING, false);
        SHUFFLE = sPref.getBoolean(APP_PREFERENCES_SHUFFLE, false);
        SKIP_SILENCE = sPref.getBoolean(APP_PREFERENCES_SKIP_SILENCE, false);
        CROSSFADE = sPref.getInt(APP_PREFERENCES_CROSSFADE, 0);
        AUDIOFOCUS = sPref.getBoolean(APP_PREFERENCES_AUDIOFOCUS, true);
        FADING = sPref.getBoolean(APP_PREFERENCES_FADING, true);
        SLIDER_BUFFER = sPref.getInt(APP_PREFERENCES_SLIDER_BUFFER, 3);
        SLIDER_ANIMATION = sPref.getString(APP_PREFERENCES_SLIDER_ANIMATION, "Default");
        THEME = sPref.getString(APP_PREFERENCES_THEME, "Dark");
        LANGUAGE = sPref.getString(APP_PREFERENCES_LANGUAGE, "");
        HEADSET_PAUSE = sPref.getBoolean(APP_PREFERENCES_HEADSET_PAUSE, true);
        START_HEADSET = sPref.getBoolean(APP_PREFERENCES_START_HEADSET, false);
        START_BLUETOOTH = sPref.getBoolean(APP_PREFERENCES_START_BLUETOOTH, false);
        PROCESS_PRESSING = sPref.getBoolean(APP_PREFERENCES_PROCESS_PRESSING, true);
        CONTINUE_AFTER_CALL = sPref.getBoolean(APP_PREFERENCES_CONTINUE_AFTER_CALL, false);
        MEMORY_VIEW = sPref.getString(APP_PREFERENCES_MEMORY_VIEW, GRID_VIEW);
        String dur = sPref.getString(APP_PREFERENCES_MEGAMIX_DURATION, "0.7f");
        MEGAMIX_DURATION = Float.valueOf(dur);
        if (MEGAMIX_DURATION > 1) MEGAMIX_DURATION = 0.7f;
        SORTING_ORDER = sPref.getBoolean(APP_PREFERENCES_SORTING_ORDER, true);
        FIRST_LAUNCH = sPref.getInt(APP_PREFERENCES_FIRST_LAUNCH, 0);
        LOAD_NEXT_TRACK = sPref.getBoolean(APP_PREFERENCES_LOAD_NEXT_TRACK, true);
        LOAD_PREV_TRACK = sPref.getBoolean(APP_PREFERENCES_LOAD_PREV_TRACK, true);
        SHOW_MEGAMIX_WARNING_DIALOG = sPref.getBoolean(APP_PREFERENCES_SHOW_MEGAMIX_WARNING_DIALOG, true);
        REMEMBER_PASS = sPref.getBoolean(APP_PREFERENCES_REMEMBER_PASS, false);
        PASSWORD = sPref.getString(APP_PREFERENCES_PASSWORD, "");
        USERNAME = sPref.getString(APP_PREFERENCES_USERNAME, "");
        LASTFM_VIEW = sPref.getString(APP_PREFERENCES_LASTFM_VIEW, GRID_VIEW);
        WIFI = sPref.getBoolean(APP_PREFERENCES_WIFI, false);
        LASTFM_START = sPref.getString(APP_PREFERENCES_LASTFM_START, "0");
        LASTFM_SCROBBLE = sPref.getBoolean(APP_PREFERENCES_SCROBBLE_LASTFM, true);
        LASTFM_NOW_PLAYING = sPref.getBoolean(APP_PREFERENCES_NOW_PLAYING_LASTFM, true);
        RECENTLY_ADDED = sPref.getInt(APP_PREFERENCES_RECENTLY_ADDED, 7);
        METADATA = sPref.getBoolean(APP_PREFERENCES_METADATA, true);
        ANIMATION = sPref.getBoolean(APP_PREFERENCES_ANIMATION, true);
        LIST_SIZE = sPref.getInt(APP_PREFERENCES_LIST_SIZE, 0);
        GRID_SIZE = sPref.getInt(APP_PREFERENCES_GRID_SIZE, 0);
        WRITE_LOG = sPref.getBoolean(APP_PREFERENCES_WRITE_LOG, false);
        FOLDERS_INFO_DIALOG = sPref.getBoolean(APP_PREFERENCES_SHOW_FOLDERS_INFO_DIALOG, true);
        PLAYLIST_INFO_DIALOG = sPref.getBoolean(APP_PREFERENCES_SHOW_PLAYLIST_INFO_DIALOG, true);
        SCREEN_WIDTH = sPref.getInt(APP_PREFERENCES_SCREEN_WIDTH, 0);
        SCREEN_HEIGHT = sPref.getInt(APP_PREFERENCES_SCREEN_HEIGHT, 0);
        FONT = sPref.getString(APP_PREFERENCES_FONT, CITRICA_FONT);
        LICENSE = sPref.getBoolean(APP_PREFERENCES_LICENSE, false);
        LICENSE_CHECKED = sPref.getInt(APP_PREFERENCES_LICENSE_CHECKED, -1);
        TRACKS_VIEW = sPref.getString(APP_PREFERENCES_TRACKS_VIEW, LIST_VIEW);
        ROW_COUNT = sPref.getInt(APP_PREFERENCES_ROW_COUNT, 3);
        START_SESSIONS = sPref.getInt(APP_PREFERENCES_START_SESSIONS, 0);
        RATED = sPref.getBoolean(APP_PREFERENCES_RATED, false);
        IS_ORIGINAL = sPref.getBoolean(APP_PREFERENCES_IS_ORIGINAL, false);
        TRANSITION = Integer.valueOf(sPref.getString(APP_PREFERENCES_TRANSITION, "0"));
        GREETING = sPref.getBoolean(APP_PREFERENCES_GREETING, false);
        VIDEO_TAG = sPref.getString(APP_PREFERENCES_VIDEO_TAG, "null");
        VIDEO_CLICKED = sPref.getBoolean(APP_PREFERENCES_VIDEO_CLICKED, false);
        LAST_POSITION = sPref.getInt(APP_PREFERENCES_LAST_POSITION, 0);

        EqualizerEngine equalizerEngine = new EqualizerEngine(context);
        equalizerEngine.getEqualizerFrequences();
        getScreenSize();
        decode_images();
        getStorageLocations();
    }

    public void setGridSize(int size) {
        savePreference(APP_PREFERENCES_GRID_SIZE, size);
        GRID_SIZE = size;
    }

    public void setListSize(int size){
        savePreference(APP_PREFERENCES_LIST_SIZE, size);
        LIST_SIZE = size;
    }

    public void savePreference(String preference, int value) {
        PrintString.printLog("Equalizer", preference + " " + value);
        sPref = PreferenceManager.getDefaultSharedPreferences(context);
        editor = sPref.edit();
        editor.putInt(preference, value);
        editor.commit();
    }

    public void savePreference(String preference, String value) {
        sPref = PreferenceManager.getDefaultSharedPreferences(context);
        editor = sPref.edit();
        editor.putString(preference, value);
        editor.commit();
    }

    public void savePreference(String preference, boolean value) {
        sPref = PreferenceManager.getDefaultSharedPreferences(context);
        editor = sPref.edit();
        editor.putBoolean(preference, value);
        editor.commit();
    }

    public String loadStringPreference(String preference) {
        sPref = PreferenceManager.getDefaultSharedPreferences(context);
        if(sPref.contains(preference)) {
            return sPref.getString(preference, "");
        }
        return null;
    }

    public boolean loadBoolPreference(String preference) {
        sPref = PreferenceManager.getDefaultSharedPreferences(context);
        if(sPref.contains(preference)) {
            return sPref.getBoolean(preference, false);
        }
        return false;
    }

    public int loadIntPreference(String preference) {
        sPref = PreferenceManager.getDefaultSharedPreferences(context);
        if(sPref.contains(preference)) {
            return sPref.getInt(preference, 0);
        }
        return 0;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals(APP_PREFERENCES_CROSSFADE)) CROSSFADE = loadIntPreference(s);
        else if (s.equals(APP_PREFERENCES_SKIP_SILENCE)) SKIP_SILENCE = loadBoolPreference(s);
        else if (s.equals(APP_PREFERENCES_AUDIOFOCUS)) AUDIOFOCUS = loadBoolPreference(s);
        else if (s.equals(APP_PREFERENCES_FADING)) FADING = loadBoolPreference(s);
        else if (s.equals(APP_PREFERENCES_SLIDER_ANIMATION)) SLIDER_ANIMATION = loadStringPreference(s);
        else if (s.equals(APP_PREFERENCES_SLIDER_BUFFER)) SLIDER_BUFFER = loadIntPreference(s);
        else if (s.equals(APP_PREFERENCES_THEME)) THEME = loadStringPreference(APP_PREFERENCES_THEME);
        else if (s.equals(APP_PREFERENCES_LANGUAGE)) {
            LANGUAGE =  loadStringPreference(APP_PREFERENCES_LANGUAGE);
            LocaleHelper.setLocale(context, Settings.LANGUAGE);
        }
        else if (s.equals(APP_PREFERENCES_HEADSET_PAUSE)) HEADSET_PAUSE = loadBoolPreference(APP_PREFERENCES_HEADSET_PAUSE);
        else if (s.equals(APP_PREFERENCES_START_HEADSET)) START_HEADSET = loadBoolPreference(APP_PREFERENCES_START_HEADSET);
        else if (s.equals(APP_PREFERENCES_START_BLUETOOTH)) START_BLUETOOTH = loadBoolPreference(APP_PREFERENCES_START_BLUETOOTH);
        else if (s.equals(APP_PREFERENCES_PROCESS_PRESSING)) PROCESS_PRESSING = loadBoolPreference(APP_PREFERENCES_PROCESS_PRESSING);
        else if (s.equals(APP_PREFERENCES_CONTINUE_AFTER_CALL)) CONTINUE_AFTER_CALL = loadBoolPreference(APP_PREFERENCES_CONTINUE_AFTER_CALL);
        else if (s.equals(APP_PREFERENCES_LOAD_NEXT_TRACK)) LOAD_NEXT_TRACK = loadBoolPreference(APP_PREFERENCES_LOAD_NEXT_TRACK);
        else if (s.equals(APP_PREFERENCES_LOAD_PREV_TRACK)) LOAD_PREV_TRACK = loadBoolPreference(APP_PREFERENCES_LOAD_PREV_TRACK);
        else if (s.equals(APP_PREFERENCES_WIFI)) WIFI = loadBoolPreference(APP_PREFERENCES_WIFI);
        else if (s.equals(APP_PREFERENCES_LASTFM_START)) LASTFM_START = loadStringPreference(APP_PREFERENCES_LASTFM_START);
        else if (s.equals(APP_PREFERENCES_SCROBBLE_LASTFM)) LASTFM_SCROBBLE = loadBoolPreference(APP_PREFERENCES_SCROBBLE_LASTFM);
        else if (s.equals(APP_PREFERENCES_NOW_PLAYING_LASTFM)) LASTFM_NOW_PLAYING = loadBoolPreference(APP_PREFERENCES_NOW_PLAYING_LASTFM);
        else if (s.equals(APP_PREFERENCES_RECENTLY_ADDED)) RECENTLY_ADDED = loadIntPreference(APP_PREFERENCES_RECENTLY_ADDED);
        else if (s.equals(APP_PREFERENCES_METADATA)) METADATA = loadBoolPreference(APP_PREFERENCES_METADATA);
        else if (s.equals(APP_PREFERENCES_ANIMATION)) ANIMATION = loadBoolPreference(APP_PREFERENCES_ANIMATION);
        else if (s.equals(APP_PREFERENCES_FONT)) FONT = loadStringPreference(APP_PREFERENCES_FONT);
        else if (s.equals(APP_PREFERENCES_ROW_COUNT)) ROW_COUNT = loadIntPreference(APP_PREFERENCES_ROW_COUNT);
        else if (s.equals(APP_PREFERENCES_TRANSITION)) TRANSITION = Integer.valueOf(loadStringPreference(APP_PREFERENCES_TRANSITION));
        else if (s.equals(APP_PREFERENCES_MEGAMIX_DURATION)){
            String dur = loadStringPreference(APP_PREFERENCES_MEGAMIX_DURATION);
            MEGAMIX_DURATION = Float.valueOf(dur);
        }
    }

    public final int getColor(int id) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 23) {
            return ContextCompat.getColor(context, id);
        } else {
            return context.getResources().getColor(id);
        }
    }

    public void hideKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void getScreenSize(){
        try {
            if (SCREEN_HEIGHT == 0 && SCREEN_WIDTH == 0) {
                Activity act = (Activity) context;
                Display display = act.getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                screen_dimensions = new com.merseyside.admin.player.Utilities.Point(size.x, size.y);
                savePreference(APP_PREFERENCES_SCREEN_WIDTH, size.x);
                savePreference(APP_PREFERENCES_SCREEN_HEIGHT, size.y);
            } else screen_dimensions = new com.merseyside.admin.player.Utilities.Point(SCREEN_WIDTH, SCREEN_HEIGHT);
        } catch (ClassCastException ignored){}
    }

    public static int getScreenWidth(){
        try {
            return screen_dimensions.getWidth();
        } catch (NullPointerException ignored){ return 0;}
    }

    public static int getScreenHeight(){
        try {
            return screen_dimensions.getHeight();
        } catch (NullPointerException ignored){ return 0;}
    }

    public int getThemeByString() {
        if (THEME.equals("Blue")) return R.style.MainActivity_Dark_Blue;
        else if (THEME.equals("Red")) return R.style.MainActivity_Dark_Red;
        else if (THEME.equals("Orange")) return R.style.MainActivity_Dark_Orange;
        else if (THEME.equals("Purple")) return R.style.MainActivity_Dark_Purple;
        else if (THEME.equals("Teal")) return R.style.MainActivity_Teal;
        else if (THEME.equals("Green")) return R.style.MainActivity_Green;
        else return R.style.MainActivity_Dark_Blue;
    }

    public int getAttributeId(int theme, int attribute) {
        TypedArray a = context.getTheme().obtainStyledAttributes(theme, new int[] {attribute});
        return a.getResourceId(0, 0);
    }

    public static String getUserInterfaceDuration(int duration, boolean ms, boolean needMS){
        int mils = 0;
        if (needMS) mils = duration%1000;
        if (ms) duration = duration/1000;
        int minutes = duration / 60;
        int seconds = duration % 60;
        if (needMS) {
            return String.format("%d:%02d:%03d", minutes, seconds, mils);
        }
        return String.format("%d:%02d", minutes, seconds);
    }

    public static String getMetadata(String path, int code){
        MediaMetadataRetriever retriver = new MediaMetadataRetriever();
        retriver.setDataSource(path);
        return retriver.extractMetadata(code);
    }

    public static boolean checkExternalPath(String path){
        if (path!= null) {
            File file = new File(path);
            return file.canWrite();
        }
        else return false;
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight){
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static Bitmap decodeSampledBitmapFromData(byte[] res, int width, int height, int lenght) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(res, 0, lenght, options);
        options.inSampleSize = calculateInSampleSize(options, width, height);

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(res, 0,  lenght, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight){
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public Drawable getTintedDrawable(Resources res, int drawableResId, int colorResId) {
        Drawable drawable = res.getDrawable(drawableResId);
        if (colorResId != 0) {
            int color = getColor(colorResId);
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        }
        return drawable;
    }

    public static boolean checkFilenameValid(String name){
        String PATTERN_COMPILE;
        Pattern pattern;
        Matcher matcher;

        PATTERN_COMPILE = "^[^*&%]+$";
        pattern = Pattern.compile(PATTERN_COMPILE);
        matcher = pattern.matcher(name);

        return matcher.find();
    }

    public void setTextViewFont(TextView textView, String font){
        Typeface typeFace;
        if (font == null) font = FONT;
        if (!font.equals(DEFAULT_FONT)) {
            typeFace = Typeface.createFromAsset(context.getAssets(), font);
            textView.setTypeface(typeFace);
        }
    }

    static int getDayOfYear(){
        return day_of_year;
    }

    public void checkFirstLaunch(){
        if (!IS_ORIGINAL) {
            init(context);
            if (getSomeValue() == ORIGINAL) {
                savePreference(APP_PREFERENCES_IS_ORIGINAL, true);
                IS_ORIGINAL = true;
            }
            else {
                FirebaseEngine.logEvent(context, "NOT_ORIGINAL", null);
                savePreference(APP_PREFERENCES_IS_ORIGINAL, false);
                InfoDialog dialog;
                ArrayList<String> info = new ArrayList<>();
                info.add(context.getString(R.string.pirate_info));
                dialog = new InfoDialog(context, context.getString(R.string.pirate_title), info, context.getString(R.string.google_play));
                dialog.setCancelable(false);
                dialog.setInfoDialogListener(new InfoDialog.InfoDialogListener() {
                    @Override
                    public void checkboxClicked(boolean isChecked) {
                        Intent rateIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                                "http://market.android.com/details?id=" + Settings.getTrialVersionPackageName()));
                        context.startActivity(rateIntent);
                    }
                });
                dialog.show();
            }
        }
        if (IS_ORIGINAL) {
            Calendar calendar = Calendar.getInstance();
            day_of_year = calendar.get(Calendar.DAY_OF_YEAR);
            if (isProVersion()) {
                if (Settings.LICENSE_CHECKED != day_of_year && Settings.LICENSE_CHECKED != -2) {
                    checkLicense(true, null);
                }
            }
            else {
                if (FIRST_LAUNCH == 0 || FIRST_LAUNCH == -1) {
                    FIRST_LAUNCH = createHideFile(day_of_year);
                    savePreference(APP_PREFERENCES_FIRST_LAUNCH, FIRST_LAUNCH);
                    if (FIRST_LAUNCH != -1 && getLeftTrial() <= 0 && !isProVersion()) {
                        TRIAL_OVER = true;
                    } else firstLaunch();
                } else {
                    if (!isProVersion()) {
                        int days_over = getLeftTrial();
                        if (days_over <= 0) {
                            TRIAL_OVER = true;
                        }
                    }
                }
            }
        }
    }

    public static int getLeftTrial(){
        int days_over;
        if (day_of_year < FIRST_LAUNCH){
            days_over = (FIRST_LAUNCH-365+TRIAL_DURATION) - day_of_year;
        } else {
            days_over = (FIRST_LAUNCH + TRIAL_DURATION) - day_of_year;
        }
        return days_over;
    }

    private void firstLaunch(){
        if (!GREETING) {
            savePreference(APP_PREFERENCES_GREETING, true);
            fillStreams();
            InfoDialog dialog;
            if (!isProVersion())
                dialog = new InfoDialog(context, context.getResources().getString(R.string.welcome_greeting), AboutPlayerFragment.getGreeting(context.getResources()));
            else
                dialog = new InfoDialog(context, context.getResources().getString(R.string.thank_you), AboutPlayerFragment.getGreeting(context.getResources()));
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    InfoDialog dialog = new InfoDialog(context, context.getResources().getString(R.string.changelog), AboutPlayerFragment.getLog(context.getResources()));
                    dialog.show();
                }
            });
            dialog.show();
        }
    }

    private void fillStreams(){
        FileManager manager = new FileManager(context);
        manager.saveStream(DBHelper.TABLE_STREAMS_NAME, "Record", "http://online.radiorecord.ru:8101/rr_128", "", null);
        manager.saveStream(DBHelper.TABLE_STREAMS_NAME, "Electron", "http://radio-electron.ru:8000/128", "", null);
        manager.saveStream(DBHelper.TABLE_STREAMS_NAME, "Record Club", "http://online.radiorecord.ru:8102/club_128", "", null);
        manager.saveStream(DBHelper.TABLE_STREAMS_NAME, "Record Megamix", "http://online.radiorecord.ru:8102/mix_128", "", null);
    }

    public void restart(Context context){
        stopService(false);
        Intent i = context.getPackageManager()
                .getLaunchIntentForPackage( context.getPackageName() );
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(i);
    }

    public String getDaysToDate(int days){
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_YEAR, days);
        SimpleDateFormat sdf1 = new SimpleDateFormat("dd-MM-yyyy", getCurrentLocale());
        return sdf1.format(c.getTime());
    }

    public Locale getCurrentLocale(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            return context.getResources().getConfiguration().getLocales().get(0);
        } else{
            return context.getResources().getConfiguration().locale;
        }
    }

    public void close(boolean closeApp){
        if (playbackManager.isNowPlaying()) playbackManager.playPressed();
        stopService(true);
        if (closeApp) {
            Activity act = (Activity) context;
            act.moveTaskToBack(true);
        }
    }

    public static boolean isProVersion(){
        return PRO_VERSION;
    }

    public static String getProVersionPackageName(){
        return PRO_VERSION_PACKAGENAME;
    }

    public static String getTrialVersionPackageName(){
        return TRIAL_VERSION_PACKAGENAME;
    }

    private void getStorageLocations(){
        Map<String, File> externalLocations = ExternalStorage.getAllStorageLocations();
        sdcardLocation = externalLocations.get(ExternalStorage.SD_CARD);
        externalLocation = externalLocations.get(ExternalStorage.EXTERNAL_SD_CARD);
        try {
            sdcardLocation.getName();
        } catch(NullPointerException e) {
            sdcardLocation = new File("/storage");
        }
        try {
            externalLocation.getName();
        } catch(NullPointerException e) {
            externalLocation = new File("/storage");
        }
    }

    public static File getExternalLocation(){
        return externalLocation;
    }

    public static File getSdcardLocation(){
        return sdcardLocation;
    }

    private int createHideFile(int day){
        String filename = "/.system_ipc.txt";
        File hideFile = new File(sdcardLocation.getAbsolutePath(), filename);
        if (!hideFile.exists() && hideFile.length() == 0) {
            try {
                hideFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return -1;
            }
            BufferedWriter bw;
            try {
                bw = new BufferedWriter(new FileWriter(hideFile, true));
                bw.write(String.valueOf(day));
                bw.close();
                return day;
            } catch (IOException ignored) {}
        } else {
            BufferedReader br;
            try {
                PrintString.printLog("License", "ReadFile");
                br = new BufferedReader(new FileReader(hideFile));
                try {
                    String line = br.readLine();
                    PrintString.printLog("License", line);
                    if (line != null && !line.equals(""))
                        day = Integer.valueOf(line);
                    br.close();
                    return day;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return -1;
            }
        }
        return day;
    }

    public static void hideOption(int id, PopupMenu popupMenu) {
        Menu menu = popupMenu.getMenu();
        MenuItem item = menu.findItem(id);
        item.setVisible(false);
    }

    public static void showOption(int id, PopupMenu popupMenu) {
        Menu menu = popupMenu.getMenu();
        MenuItem item = menu.findItem(id);
        item.setVisible(true);
    }

    public void checkLicense(boolean inBackground, LicenseCheckerEngine.LicenseListener licenseListener){
        LicenseCheckerEngine licenseCheckerEngine = new LicenseCheckerEngine(context);
        licenseCheckerEngine.setLicenseListener(licenseListener);
        licenseCheckerEngine.checkLicense(inBackground);
    }
}


