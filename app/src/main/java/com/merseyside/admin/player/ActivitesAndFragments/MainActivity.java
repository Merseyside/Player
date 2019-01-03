package com.merseyside.admin.player.ActivitesAndFragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDex;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.merseyside.admin.player.AdaptersAndItems.Track;
import com.merseyside.admin.player.BuildConfig;
import com.merseyside.admin.player.Dialogs.InfoDialog;
import com.merseyside.admin.player.Dialogs.PlaybackOrderDialog;
import com.merseyside.admin.player.Dialogs.VideoDialog;
import com.merseyside.admin.player.LastFm.LastFmEngine;
import com.merseyside.admin.player.LastFm.LastFmFragment;
import com.merseyside.admin.player.MegamixLibrary.MegamixCreator;
import com.merseyside.admin.player.R;
import com.merseyside.admin.player.SettingsActivities.SettingsFragment;
import com.merseyside.admin.player.Utilities.LocaleHelper;
import com.merseyside.admin.player.Utilities.MySnackbar;
import com.merseyside.admin.player.Utilities.PlaybackManager;
import com.merseyside.admin.player.Utilities.PrintString;
import com.merseyside.admin.player.Utilities.Settings;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {
    private FragmentTransaction fTrans;
    private android.support.v4.app.FragmentManager fm;
    private StreamFragment stream;
    private PlaylistFragment playlist;
    private static Player_Fragment player;
    private MemoryFragment memory;
    private LibraryFragment library;
    private SettingsFragment settingsFragment;
    private LastFmFragment lastFm;
    private Settings settings;
    private EqualizerFragment equalizer;
    private AboutPlayerFragment about;
    private static MegamixCreator megamixCreator;
    private static LastFmEngine lastFmEngine;
    private ImageButton drawer_button, equalizer_button, player_button, order_button;
    private DrawerLayout drawer;
    private int currentTheme;
    private String currentLanguage;
    private PlaybackManager playbackManager;
    private CheckBox chbMegamix;
    private boolean isMegamix;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    public static Player_Fragment get_player_fragment(){
        return player;
    }
    public static MegamixCreator get_creator(){ return megamixCreator;}
    public static LastFmEngine getLastFmEngine() {
        return lastFmEngine;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
        MultiDex.install(base);
    }

    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        settings = new Settings(MainActivity.this);
        settings.bindService(getApplicationContext());
        settings.saveLogcatToFile();
        //settings.checkFirstLaunch();

        PrintString.printLog("lifeCycle", "MainActivity onCreate");
        megamixCreator = new MegamixCreator();
        lastFmEngine = new LastFmEngine(this);
        fm = getSupportFragmentManager();
        setupTheme();
        setContentView(R.layout.activity_main);
        drawer_button = (ImageButton) findViewById(R.id.drawer_toggle);
        drawer_button.setOnClickListener(this);

        player_button = (ImageButton) findViewById(R.id.action_player);
        player_button.setOnClickListener(this);

        equalizer_button = (ImageButton) findViewById(R.id.action_equalizer);
        equalizer_button.setOnClickListener(this);

        order_button = (ImageButton) findViewById(R.id.action_order);
        order_button.setOnClickListener(this);

        chbMegamix = (CheckBox) findViewById(R.id.action_megamix);
        chbMegamix.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                Settings.setServiceListener(new Settings.ServiceListener() {
                    @Override
                    public void onServiceConnected(PlaybackManager playbackManager1) {
                        playbackManager = playbackManager1;
                        playbackManager.setMegamixMode(chbMegamix.isChecked());
                    }

                    @Override
                    public void onServiceDisconnected(PlaybackManager playbackManager) {}
                });
                if (isMegamix != isChecked) {
                    if (playbackManager != null) {
                        playbackManager.setMegamixMode(isChecked);
                    }
                    isMegamix = isChecked;
                    if (isChecked) {
                        chbMegamix.setBackground(settings.getTintedDrawable(getResources(), R.drawable.megamix_cb, R.color.white));
                        if (playbackManager != null) {
                            if (playbackManager.isNowPlaying())
                                new MySnackbar(MainActivity.this, player_button, R.string.megamix_will_start).show();
                        }
                    } else {
                        chbMegamix.setBackground(settings.getTintedDrawable(getResources(), R.drawable.megamix_cb, R.color.grey));
                    }
                }
            }
        });

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        stream = new StreamFragment();
        playlist = new PlaylistFragment();
        player = new Player_Fragment();
        memory = new MemoryFragment();
        library = new LibraryFragment();
        settingsFragment = new SettingsFragment();
        equalizer = new EqualizerFragment();
        about = new AboutPlayerFragment();
        lastFm = new LastFmFragment();

        super.onCreate(savedInstanceState);

        showRateDialog();
        showVideoDialog();

        try {
            if (fm.findFragmentById(R.id.main_fragment) == null) {
                PrintString.printLog("lifeCycle", "Created");
                fTrans = getSupportFragmentManager().beginTransaction();
                if ((Settings.isProVersion() && !Settings.LICENSE) || Settings.TRIAL_OVER) fTrans.add(R.id.main_fragment, about);
                else fTrans.add(R.id.main_fragment, player);
                fTrans.addToBackStack(null);
                fTrans.commit();
            }
        } catch (IllegalStateException ignored){}
        Settings.verifyStoragePermissions(this);
    }

    private void setupTheme(){
        PrintString.printLog("lifeCycle", Settings.LANGUAGE);
        String lang = Locale.getDefault().getLanguage();
        String[] some_array = getResources().getStringArray(R.array.languages_entry_values);
        boolean found = false;
        for (String str : some_array){
            if (str.equals(lang)){
                found = true;
                break;
            }
        }
        if (!found) lang = "en";
        if (Settings.LANGUAGE.equals("")) {
            LocaleHelper.setLocale(getApplicationContext(), lang);
            settings.savePreference(Settings.APP_PREFERENCES_LANGUAGE, lang);
            currentLanguage = lang;
        }
        else {
            LocaleHelper.setLocale(getApplicationContext(), Settings.LANGUAGE);
            currentLanguage = Settings.LANGUAGE;
        }
        currentTheme = settings.getThemeByString();
        if (fm.getBackStackEntryCount() != 0)
            for(int i = 0; i < fm.getBackStackEntryCount(); ++i) {
                fm.popBackStack();
            }
        setTheme(currentTheme);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentTheme != settings.getThemeByString() || !currentLanguage.equals(Settings.LANGUAGE)) {
            Settings.THEME = null;
            settings.restart(getApplicationContext());
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        Fragment currentFragment = fm.findFragmentById(R.id.main_fragment);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (Settings.isProVersion() && !Settings.LICENSE || Settings.TRIAL_OVER) {
            if (currentFragment instanceof AboutPlayerFragment) moveTaskToBack(true);
        } else if (currentFragment instanceof Player_Fragment){
            moveTaskToBack(true);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        if ((!Settings.isProVersion() || Settings.LICENSE) && !Settings.TRIAL_OVER){
            int id = item.getItemId();
            fTrans = fm.beginTransaction();

            if (Settings.ANIMATION)
                fTrans.setCustomAnimations(R.anim.slide_right_to_left, R.anim.slide_in_left, R.anim.slide_left_to_right, R.anim.slide_in_right);
            switch (id) {
                case R.id.nav_memory:
                    fTrans.replace(R.id.main_fragment, memory);
                    break;
                case R.id.nav_stream:
                    fTrans.replace(R.id.main_fragment, stream);
                    break;
                case R.id.nav_playlist:
                    fTrans.replace(R.id.main_fragment, playlist);
                    break;
                case R.id.nav_settings:
                    fTrans.replace(R.id.main_fragment, settingsFragment);
                    break;
                case R.id.nav_library:
                    fTrans.replace(R.id.main_fragment, library);
                    break;
                case R.id.nav_lastfm:
                    fTrans.replace(R.id.main_fragment, lastFm);
                    break;
                case R.id.nav_share:
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.share_main));
                    sendIntent.setType("text/*");
                    startActivity(sendIntent);
                    break;
                case R.id.nav_about:
                    fTrans.replace(R.id.main_fragment, about);
                    break;
                case R.id.nav_exit:
                    settings.close(true);
                    break;
            }
            fTrans.addToBackStack(null);
            fTrans.commit();

            drawer.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        PrintString.printLog("lifeCycle", "MainActivity onDestroy" );
    }

    @Override
    public void onStop(){
        super.onStop();
        PrintString.printLog("onStop", "MainActivity onStop");
    }

    @Override
    public void onPause(){
        super.onPause();
        PrintString.printLog("lifeCycle", "MainActivity onPause");
    }

    @Override
    public void onClick(View view) {
        if ((!Settings.isProVersion() || Settings.LICENSE) && !Settings.TRIAL_OVER) {
            int id = view.getId();
            Fragment currentFragment = fm.findFragmentById(R.id.main_fragment);
            switch (id) {
                case R.id.action_player:
                    try {
                        fTrans = fm.beginTransaction();
                        if (Settings.ANIMATION)
                            fTrans.setCustomAnimations(R.anim.slide_up_to_down, R.anim.slide_in_right, R.anim.slide_right_to_left, R.anim.slide_up);
                        fTrans.replace(R.id.main_fragment, player);
                        fTrans.addToBackStack(null);
                        fTrans.commit();
                    } catch (IllegalStateException ignored) {}
                    break;
                case R.id.action_equalizer:
                    if (!(currentFragment instanceof EqualizerFragment)) {
                        try {
                            fTrans = fm.beginTransaction();
                            if (Settings.ANIMATION)
                                fTrans.setCustomAnimations(R.anim.slide_right_to_left, R.anim.slide_in_left, R.anim.slide_left_to_right, R.anim.slide_in_right);
                            fTrans.replace(R.id.main_fragment, equalizer);
                            fTrans.addToBackStack(null);
                            fTrans.commit();
                        } catch (IllegalStateException ignored) {}
                    }
                    break;

                case R.id.drawer_toggle:
                    drawer.openDrawer(GravityCompat.START);
                    break;
                case R.id.action_order:
                    Settings.setServiceListener(new Settings.ServiceListener() {
                        @Override
                        public void onServiceConnected(PlaybackManager playbackManager1) {
                            playbackManager = playbackManager1;
                        }

                        @Override
                        public void onServiceDisconnected(PlaybackManager playbackManager) {
                        }
                    });
                    if (playbackManager.isValid() && playbackManager.getPlaylist() != null && playbackManager.getPlaylist().size() != 0) {
                        ArrayList<Track> playlist = playbackManager.getPlaylist();
                        int currentPosition = playbackManager.getCurrentPosition();
                        PlaybackOrderDialog order_dialog = new PlaybackOrderDialog(this, playlist, currentPosition, new PlaybackOrderDialog.MyOrderDialogListener() {
                            @Override
                            public void itemClicked(ArrayList<Track> list, boolean isChanged, int currentPosition) {
                                if (isChanged) {
                                    playbackManager.setPlaylist(list);
                                }
                                playbackManager.startPlayByPosition(currentPosition);
                                if (isChanged)
                                    get_player_fragment().orderChanged(playbackManager.getCurrentPosition(), list);
                            }

                            @Override
                            public void playlistChanged(ArrayList<Track> list) {
                                playbackManager.setPlaylist(list);
                                get_player_fragment().orderChanged(playbackManager.getCurrentPosition(), list);
                            }

                            @Override
                            public void currentTrackDragged(int newPosition, ArrayList<Track> list) {
                                playbackManager.setPlaylist(list);
                                playbackManager.setCurrentPosition(newPosition);
                                get_player_fragment().orderChanged(playbackManager.getCurrentPosition(), list);
                            }
                        });
                        order_dialog.show();
                    }
                    break;
            }
        }
    }

    private void showRateDialog(){
        Settings.START_SESSIONS++;
        settings.savePreference(Settings.APP_PREFERENCES_START_SESSIONS, Settings.START_SESSIONS);
        if (!Settings.RATED && Settings.START_SESSIONS % 5 == 0 && Settings.START_SESSIONS != 0) {
            ArrayList<String> list = new ArrayList<>();
            list.add(getString(R.string.rate_us));
            final InfoDialog ratedDialog = new InfoDialog(this, getString(R.string.rate), list, getString(R.string.rate));
            ratedDialog.setInfoDialogListener(new InfoDialog.InfoDialogListener() {
                @Override
                public void checkboxClicked(boolean isChecked) {
                    if (isChecked) settings.savePreference(Settings.APP_PREFERENCES_RATED, true);
                    String packName;
                    if (Settings.isProVersion()) packName = Settings.getProVersionPackageName();
                    else packName = Settings.getTrialVersionPackageName();
                    Intent rateIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                            "http://market.android.com/details?id=" + packName));
                    startActivity(rateIntent);
                    ratedDialog.dismiss();
                }
            });
            ratedDialog.show();
        }
    }

    private void showVideoDialog(){
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(false)
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);

        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);

        long cacheExpiration = 60*60*6;
        if (mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }

        mFirebaseRemoteConfig.fetch(cacheExpiration)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            PrintString.printLog("RemoteConfig", "success");

                            mFirebaseRemoteConfig.activateFetched();
                        }
                        displayVideoDialog();
                        //checkNewVersion();
                    }
                });
    }

    private void displayVideoDialog(){
        String tag = mFirebaseRemoteConfig.getString(Settings.VIDEO_TAG_KEY);
        if (!Settings.VIDEO_TAG.equals(tag) || !Settings.VIDEO_CLICKED && !tag.equals("null")) {
            Settings.VIDEO_CLICKED = false;
            settings.savePreference(Settings.APP_PREFERENCES_VIDEO_CLICKED, false);
            final String url = mFirebaseRemoteConfig.getString(Settings.VIDEO_URL_KEY);
            final String title = mFirebaseRemoteConfig.getString(Settings.VIDEO_TITLE_KEY);
            Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.video_teaser);
            Settings.VIDEO_TAG = tag;
            settings.savePreference(Settings.APP_PREFERENCES_VIDEO_TAG, tag);
            VideoDialog video = new VideoDialog(this, image, title, new VideoDialog.VideoDialogListener() {
                @Override
                public void userClickedVideo() {
                    Settings.VIDEO_CLICKED = true;
                    settings.savePreference(Settings.APP_PREFERENCES_VIDEO_CLICKED, true);
                    Intent rateIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(rateIntent);
                }

                @Override
                public void userClickedCancel() {}
            });
            video.show();
        }
    }

    private void checkNewVersion(){
        String version = mFirebaseRemoteConfig.getString(Settings.VERSION_KEY);
        if (!version.equals(Settings.CURRENT_VERSION)){
            ArrayList<String> info = new ArrayList<>();
            info.add(getString(R.string.new_version_information));
            final InfoDialog dialog = new InfoDialog(this, getString(R.string.new_version), info, getString(R.string.google_play));
            dialog.setInfoDialogListener(new InfoDialog.InfoDialogListener() {
                @Override
                public void checkboxClicked(boolean isChecked) {
                    String packName = Settings.isProVersion() ? Settings.getProVersionPackageName() : Settings.getTrialVersionPackageName();
                    Intent versionIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://market.android.com/details?id=" + packName));
                    startActivity(versionIntent);
                    dialog.dismiss();
                }
            });
            dialog.show();
        }
    }
}
