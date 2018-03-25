package com.merseyside.admin.player.ActivitesAndFragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.google.firebase.crash.FirebaseCrash;
import com.merseyside.admin.player.AdaptersAndItems.SQLItem;
import com.merseyside.admin.player.Dialogs.AddToPlaylistDialog;
import com.merseyside.admin.player.Dialogs.CommentDialog;
import com.merseyside.admin.player.Dialogs.PlayerTrackDialog;
import com.merseyside.admin.player.Dialogs.ShareDialog;
import com.merseyside.admin.player.Dialogs.SleepTimerDialog;
import com.merseyside.admin.player.LastFm.LastFmEngine;
import com.merseyside.admin.player.LastFm.LastFmFragment;
import com.merseyside.admin.player.MegamixLibrary.MegamixCreator;
import com.merseyside.admin.player.R;
import com.merseyside.admin.player.Utilities.DBHelper;
import com.merseyside.admin.player.Utilities.FileManager;
import com.merseyside.admin.player.Utilities.InfoForPlayer;
import com.merseyside.admin.player.Utilities.M3UParser;
import com.merseyside.admin.player.Utilities.MySnackbar;
import com.merseyside.admin.player.Utilities.ParentFragment;
import com.merseyside.admin.player.Utilities.PlaybackManager;
import com.merseyside.admin.player.Utilities.PrintString;
import com.merseyside.admin.player.Utilities.Settings;
import com.merseyside.admin.player.AdaptersAndItems.Track;
import com.merseyside.admin.player.Utilities.SliderManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class Player_Fragment extends ParentFragment implements SliderManager.SliderManagerListener,View.OnClickListener,
        SeekBar.OnSeekBarChangeListener,ViewPagerEx.OnPageChangeListener {

    private Timer tracksTimer;
    private TracksTask tracksMyTimer;

    private class TracksTask extends TimerTask{
        static final int TRACKS_DELAY = 600;
        TracksTask(){
        }
        @Override
        public void run() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    playbackManager.startPlayByPosition(currentPosition);
                    settings.startIntentService();
                }
            });
        }
    }

    private static final int SLIDER_DELAY = 700;
    private Timer sliderTimer;
    private SliderTask sliderMyTimer;

    private void resetSliderTimer(int action){
        if (sliderTimer != null) sliderTimer.cancel();
        if (sliderMyTimer != null) sliderMyTimer.cancel();
        sliderTimer = new Timer();
        sliderMyTimer = new SliderTask(action);
    }

    private void pageSelected(int position){
        try {
            PrintString.printLog("Slider", "isPrepared? " + sliderManager.isPrepared());
            if (sliderManager.isPrepared()) {
                int action = sliderManager.move(position);
                if (playlist!=null && currentPosition + action < playlist.size()) {
                    currentTrack = playlist.get(currentPosition + action);
                    refreshInterface(false);
                }
                resetSliderTimer(action);
                sliderTimer.schedule(sliderMyTimer,SLIDER_DELAY);
            }
        } catch (NullPointerException ignored){
            FirebaseCrash.report(ignored);
        }
    }

    private class SliderTask extends TimerTask {

        private int action;
        public SliderTask(int action){
            this.action = action;
        }
        @Override
        public void run() {
            if (getActivity()!=null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (action == 1) playbackManager.nextTrack();
                        else if (action == -1) playbackManager.previousTrack();
                        else if (action != 0) playbackManager.startPlayByPosition(playbackManager.getCurrentPosition() + action);
                    }
                });
            }
        }
    }
    @Override
    public void onPageScrollStateChanged(int state) {
        PrintString.printLog("Slider", "state = " + state);
        sliderManager.setState(state);
    }
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

    @Override
    public void onPageSelected(int position) {
        pageSelected(position);
    }


    private class PlayerHelper{

        public void shuffle(){
            Collections.shuffle(playlist);
        }

        public boolean isValid(){
            boolean flag = true;
            if (currentType == Player_Fragment.Type.PLAYLIST || currentType == Player_Fragment.Type.MEMORY){
                if (currentTrack!=null){
                    File file = new File(currentTrack.getPath());
                    if (file.exists()){
                        if (duration <= 0) {
                            flag = false;
                        }
                        if (currentItem==null || currentItem.length()==0){
                            flag = false;
                        }
                        if (playlist == null || playlist.size()==0){
                            flag = false;
                        }
                    }
                }
                else flag = false;
            }
            else if (currentType == Player_Fragment.Type.STREAM){

                if (currentURL == null){
                    flag = false;
                }
            }
            else return false;

            return flag;
        }

        String userInterfaceDuration(long mils){
            int total = (int) mils/1000;
            int minutes = total / 60;
            int seconds = total % 60;

            return String.format(Locale.ENGLISH,"%d:%02d", minutes, seconds);
        }

        private void changePlayButton(){
            if (playbackManager != null) {
                try {
                    if (playbackManager.isNowPlaying()) {
                        btn_play.setImageResource(R.drawable.pause);
                        start_duration.clearAnimation();
                    }
                    else {
                        btn_play.setImageResource(R.drawable.play);
                        start_duration.startAnimation(animBlink);
                    }
                } catch (NullPointerException ignored){}
            }
        }

        void startSeekBarChanger(){
            if (playbackManager != null) {
                isRunning = true;
                start_duration.setText(playerHelper.userInterfaceDuration(0));
                try {
                    duration = currentTrack.getDurationLong();
                } catch (NumberFormatException ignored) {}
                end_duration.setText(playerHelper.userInterfaceDuration(duration));
                seek_bar.setMax((int) duration);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (isRunning) {
                            try {
                                Thread.sleep(350);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (!isSeekBarDragging) {
                                try {
                                    if (currentTrack != null) {
                                        if (currentTrack.isMegamixTrack())
                                            seek_bar.setProgress(playbackManager.getCurrentProgress() - Integer.valueOf(currentTrack.getStartPoint()));
                                        else
                                            seek_bar.setProgress(playbackManager.getCurrentProgress());
                                        if (getActivity() != null) {
                                            getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    start_duration.setText(playerHelper.userInterfaceDuration(seek_bar.getProgress()));
                                                }
                                            });
                                        }
                                    }
                                } catch (NumberFormatException ignored) {
                                }
                            }
                        }
                    }
                    }).start();
            }
        }

        private void setPreferences(){
            if (currentItem != null) {
                PrintString.printLog("lifeCycle", currentItem + " " + getStringType(currentType));
                settings.savePreference(Settings.APP_PREFERENCES_LAST_ITEM, currentItem);
                settings.savePreference(Settings.APP_PREFERENCES_LAST_INSTANCE, getStringType(currentType));
                settings.savePreference(Settings.APP_PREFERENCES_LOOPING, isLooping);
                settings.savePreference(Settings.APP_PREFERENCES_SHUFFLE, chbShuffle.isChecked());
            }
        }

        private String getStringType(Type type){
            if (type == Type.STREAM) return DBHelper.TABLE_STREAMS_NAME;
            else if(type == Type.MEMORY) return DBHelper.TABLE_MEMORY_NAME;
            else if(type == Type.PLAYLIST) return DBHelper.TABLE_PLAYLIST_NAME;
            else return "http";
        }

        private Type getTypeFromString(String type){
            switch (type) {
                case DBHelper.TABLE_STREAMS_NAME:
                    return Type.STREAM;
                case DBHelper.TABLE_MEMORY_NAME:
                    return Type.MEMORY;
                case DBHelper.TABLE_PLAYLIST_NAME:
                    return Type.PLAYLIST;
                default:
                    return Type.HTTP;
            }
        }
    }

    private ImageButton btn_play, btn_prev, btn_forward, btn_nextItem, btn_prevItem, btn_track_options, btn_options;
    private SliderLayout coverSlider;
    private CheckBox chbLoop, chbShuffle;
    private TextView track_name, artist, start_duration, end_duration;
    private SeekBar seek_bar;
    private RatingBar rating;

    public enum Type{HTTP, STREAM, MEMORY, PLAYLIST}
    private String currentItem;

    private Type currentType;
    private int currentPosition;
    private String currentURL;

    private ArrayList<Track> playlist;

    private long duration;
    private Track currentTrack;

    private FileManager manager;
    private LastFmFragment lastFmFragment;
    private Settings settings;
    private PlayerHelper playerHelper;
    private SliderManager sliderManager;

    private PlaybackManager playbackManager;

    boolean bound = false;

    private InfoForPlayer infoForPlayer;
    private Info info_fragment;

    private FragmentTransaction fTrans;

    private boolean isLooping = false;
    private boolean isShuffle = false;
    private boolean isRunning = false;
    private boolean isOnPause = false;
    private boolean isSeekBarDragging = false;

    private View slider_view;
    private Animation animBlink;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.player, null);
    }

    private void createPlaybackManager(){
        playerHelper = new PlayerHelper();
        Settings.setServiceListener(new Settings.ServiceListener() {
            @Override
            public void onServiceConnected(final PlaybackManager playbackManager1) {
                PrintString.printLog("Service", "onServiceConnected");
                playbackManager = playbackManager1;
                playbackManager.setMyPlaybackManagerListener(new PlaybackManager.MyPlaybackManagerListener() {
                    @Override
                    public void playPlaylist(Track track) {
                        PrintString.printLog("lifeCycle", "Player Fragment playPlaylist " + playbackManager.getCurrentPosition());
                        currentPosition = playbackManager.getCurrentPosition();
                        currentTrack = track;
                        if (currentTrack!=null)
                        refreshInterface(true);
                    }

                    @Override
                    public void playMemory(Track track) {
                        currentPosition = playbackManager.getCurrentPosition();
                        currentTrack = track;
                        if (currentTrack!=null) {
                            refreshInterface(true);
                        } else {
                            isRunning = false;
                            setupPlayer();
                        }
                    }

                    @Override
                    public void playStream(String item, String URL){
                        currentItem = item;
                        currentURL = URL;
                        refreshInterface(true);
                    }

                    @Override
                    public void error(String err) {}

                    @Override
                    public void playButtonPressed() {
                        playerHelper.changePlayButton();
                    }
                });
                bound = true;
                if (infoForPlayer==null) {
                    PrintString.printLog("lifeCycle", "here");
                    if (playbackManager.isAlreadyPlaying()) {
                        setupPlayer(playbackManager.getInformation());
                        sliderManager.bind(coverSlider, getActivity(), slider_view);
                        if (currentType != Player_Fragment.Type.STREAM) {
                            sliderManager.setInfo(playbackManager.getPlaylist().size(), playbackManager.getCurrentPosition(), currentType);
                        } else sliderManager.setInfo(1, 0, currentType);
                    } else {
                        setupPlayer();
                        sliderManager.bind(coverSlider, getActivity(), slider_view);
                        if (!playbackManager.isValid())
                            if (playerHelper.isValid()) {
                                playbackManager.setPlayerInfo(getInfoForPlayer());
                                if (currentType == Player_Fragment.Type.STREAM && playbackManager.isValid()) {
                                    sliderManager.setInfo(1, 0, currentType);
                                } else if (playbackManager != null && playbackManager.getPlaylist() != null) {
                                    sliderManager.setInfo(playbackManager.getPlaylist().size(), playbackManager.getCurrentPosition(), currentType);
                                }
                            }
                    }
                }
                refreshInterface(true);
            }

            @Override
            public void onServiceDisconnected(PlaybackManager playbackManager) {
                bound = false;
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings = new Settings(getActivity());
        manager = new FileManager(getActivity());
        sliderManager = new SliderManager(getActivity(), getResources());
        sliderManager.setSliderManagerListener(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        PrintString.printLog("lifeCycle", "Player Fragment onActivityCreated");
        chbLoop = (CheckBox) getView().findViewById(R.id.loop_cb);
        chbShuffle = (CheckBox) getView().findViewById(R.id.shuffle_cb);

        btn_prev = (ImageButton) getView().findViewById(R.id.prev_track);
        btn_prev.setOnClickListener(this);

        btn_play = (ImageButton) getView().findViewById(R.id.play_track);
        btn_play.setOnClickListener(this);

        btn_forward = (ImageButton) getView().findViewById(R.id.forward_track);
        btn_forward.setOnClickListener(this);

        btn_nextItem = (ImageButton) getView().findViewById(R.id.next_item);
        btn_nextItem.setOnClickListener(this);

        btn_prevItem = (ImageButton) getView().findViewById(R.id.prev_item);
        btn_prevItem.setOnClickListener(this);

        btn_track_options = (ImageButton) getView().findViewById(R.id.note);
        btn_track_options.setOnClickListener(this);

        btn_options = (ImageButton) getView().findViewById(R.id.options);
        btn_options.setOnClickListener(this);

        track_name = (TextView) getView().findViewById(R.id.track_name);
        artist = (TextView) getView().findViewById(R.id.track_artist);
        start_duration = (TextView) getView().findViewById(R.id.start_duration);
        end_duration = (TextView) getView().findViewById(R.id.end_duration);

        coverSlider = (SliderLayout) getView().findViewById(R.id.slider);
        coverSlider.addOnPageChangeListener(this);

        animBlink = AnimationUtils.loadAnimation(getActivity(), R.anim.blink_animation);

        chbLoop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (playbackManager != null) {
                    playbackManager.setLooping(isChecked);
                }
                isLooping = isChecked;
                if (isChecked){
                    chbLoop.setBackground(settings.getTintedDrawable(getResources(), R.drawable.loop, R.color.white));
                }
                else {
                    chbLoop.setBackground(settings.getTintedDrawable(getResources(), R.drawable.loop, R.color.grey));
                }
            }
        });

        chbShuffle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (playbackManager != null) {
                    playbackManager.shufflePlaylist(isChecked);
                    playlist = playbackManager.getPlaylist();
                    if (playbackManager != null && sliderManager.isPrepared())
                        sliderManager.orderChanged(playbackManager.getCurrentPosition());
                }
                if (isChecked){
                    chbShuffle.setBackground(settings.getTintedDrawable(getResources(), R.drawable.shuffle, R.color.white));
                }
                else {
                    chbShuffle.setBackground(settings.getTintedDrawable(getResources(), R.drawable.shuffle, R.color.grey));
                }
            }
        });

        seek_bar = (SeekBar) getView().findViewById(R.id.seekBar);
        seek_bar.setOnSeekBarChangeListener(this);

        rating = (RatingBar) getView().findViewById(R.id.rating);
        PrintString.printLog("Rating", "onActivityCreated");
        rating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean fromUser) {
                if (fromUser) {
                    if (playbackManager!= null && playbackManager.getPlaylist() != null) {
                        PrintString ps = new PrintString();
                        ps.printStackTrace();
                        PrintString.printLog("Rating", v + "");
                        if (playbackManager.setRating((int) v))
                            if (manager != null) manager.setTrack(playbackManager.getCurrentTrack());
                    }
                } else if (currentTrack != null) rating.setRating(currentTrack.getRating());
            }
        });

        slider_view = getView().findViewById(R.id.slider_view);
        if (playbackManager == null) createPlaybackManager();

        if (infoForPlayer != null) startPlay();
        sliderManager.bind(coverSlider, getActivity(), slider_view);
        if (playbackManager != null) {
            if (((playlist = playbackManager.getPlaylist()) != null && playbackManager.getPlaylist().size() != 0)|| currentType == Type.STREAM) {
                if (currentType != Type.STREAM)
                    sliderManager.setInfo(playbackManager.getPlaylist().size(), playbackManager.getCurrentPosition(), currentType);
                else sliderManager.setInfo(1, 0, currentType);
            }
        }
    }

    public void startPlay(){
        if (playbackManager == null){
            settings = new Settings(getActivity());
            createPlaybackManager();
            startPlay();
            return;
        }
        if ((currentType = infoForPlayer.getType()) == Type.STREAM) {
            playbackManager.setPlayerInfo(infoForPlayer);
            playbackManager.playPressed();
            infoForPlayer = null;
        }else {
            infoForPlayer.setShuffle(false);
            infoForPlayer.setLooping(isLooping);
            if (playbackManager.setPlayerInfo(infoForPlayer)) {
                currentItem = infoForPlayer.getItem();
                currentType = infoForPlayer.getType();
                currentTrack = infoForPlayer.getPlaylist().get(infoForPlayer.getCurrentTrack_position());
                PrintString.printLog("lifeCycle", "Track position " + playbackManager.getCurrentPosition() + "");
                infoForPlayer = null;
                playbackManager.playPressed();
                playerHelper.setPreferences();
            }
        }
        if (bound) {
            try {
                settings.startIntentService();
            } catch (NullPointerException ignored){
                settings = new Settings(getContext());
                settings.startIntentService();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        PrintString.printLog("lifeCycle", "Player Fragment OnStart()");
    }

    @Override
    public void onResume() {
        super.onResume();
        PrintString.printLog("lifeCycle", "Player Fragment onResume");
        isOnPause = false;
        refreshInterface(true);
        if (sliderManager.isOnPause() && sliderManager.isPrepared() && playbackManager != null){
            sliderManager.setOnPause(false);
            sliderManager.setTrack(playbackManager.getCurrentPosition());
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        PrintString.printLog("lifeCycle", "Player Fragment onPause");
        playerHelper.setPreferences();
        isRunning = false;
        isOnPause = true;
        if (playbackManager != null && playbackManager.isAlreadyPlaying()){
            sliderManager.setOnPause(true);
        }
    }

    private void clearViews(){
        btn_play = null;
        btn_prev = null;
        btn_forward = null;
        btn_nextItem = null;
        btn_prevItem = null;
        btn_track_options = null;
        coverSlider = null;
        chbLoop = null;
        chbShuffle = null;
        track_name = null;
        artist = null;
        end_duration = null;
        rating = null;
        System.gc();
    }

    @Override
    public void onStop(){
        super.onStop();
    }

    private void setupPlayer(InfoForPlayer infoForPlayer){
        try {
            currentType = infoForPlayer.getType();
            currentItem = infoForPlayer.getItem();
            if (currentType == Type.MEMORY || currentType == Type.PLAYLIST) {
                if (currentItem != null) {
                    isLooping = infoForPlayer.isLooping();
                    isShuffle = infoForPlayer.isShuffle();
                    playlist = infoForPlayer.getPlaylist();
                    if (playlist != null) {
                        currentPosition = infoForPlayer.getCurrentTrack_position();
                        currentTrack = playlist.get(currentPosition);
                        duration = currentTrack.getDurationLong();
                    } else PrintString.printLog("Error", "player_fragment can not setup player No last_item");
                }  else PrintString.printLog("Error", "player_fragment can not setup player No last_instance");
            }
            else if (currentType == Type.STREAM) {
                if (currentItem != null) {
                    currentURL = manager.getStreamURL(currentItem);
                }
            }
        }catch(NullPointerException e){
            PrintString.printLog("Error", "NullPointerException in setupPlayer()");
            e.printStackTrace();
        }
        PrintString.printLog("lifeCycle", "SetupPlayer2");
    }

    private void setupPlayer(){
        try {
            String last_instance = Settings.LAST_INSTANCE;
            currentItem = Settings.LAST_ITEM;
            isLooping = Settings.LOOPING;
            isShuffle = Settings.SHUFFLE;
            currentPosition = Settings.LAST_POSITION;
            if ((last_instance.equals(playerHelper.getStringType(Type.MEMORY))) || (last_instance.equals(playerHelper.getStringType(Type.PLAYLIST)))) {
                if (currentItem != null) {
                    switch (currentItem) {
                        case DBHelper.ALL_TRACKS:
                            playlist = manager.getAllTracks();
                            break;
                        case DBHelper.RATED:
                            playlist = manager.getRatedTracks();
                            break;
                        case DBHelper.MEGAMIX:
                            playlist = manager.getMegamixTracks();
                            break;
                        case DBHelper.COMMENT:
                            playlist = manager.getCommentedTracks();
                            break;
                        case DBHelper.RECENTLY_ADDED:
                            playlist = manager.getRecentlyAddedTracks();
                            break;
                        default:
                            playlist = manager.getTracksFromPlaylistAndMemory(last_instance, currentItem);
                            break;
                    }
                    if (playlist != null) {
                        if (isShuffle && !playbackManager.isShuffled){
                            playerHelper.shuffle();
                            playbackManager.isShuffled = true;
                            playbackManager.isShuffle = true;
                        }
                        try {
                            currentTrack = playlist.get(currentPosition);
                        }catch (IndexOutOfBoundsException e){
                            currentTrack = null;
                            playlist = null;
                        }
                        currentType = playerHelper.getTypeFromString(last_instance);
                        duration = currentTrack.getDurationLong();
                    } else PrintString.printLog("Error", "player_fragment can not setup player No last_item");
                }  else PrintString.printLog("Error", "player_fragment can not setup player No last_instance");
            }
            else if (last_instance.equals(playerHelper.getStringType(Type.STREAM))) {
                if (!currentItem.equals("")) {
                    currentURL = manager.getStreamURL(currentItem);
                    currentType = Type.STREAM;
                    btn_track_options.setClickable(false);
                }
            }
        }catch(NullPointerException e){
            PrintString.printLog("Error", "NullPointerException in setupPlayer()");
            e.printStackTrace();
        }
        PrintString.printLog("lifeCycle", "SetupPlayer");
    }

    private void refreshInterface(boolean isRefreshByService){
        if (isAdded() && !isOnPause) {
            sliderManager.setOnPause(false);
            PrintString.printLog("lifeCycle", "Player Fragment refreshInteface");
            setVisibilityByType(currentType);
            try {
                if (currentType == Type.STREAM) {
                    track_name.setText(currentItem);
                    artist.setText(currentURL);
                    playerHelper.changePlayButton();
                } else if (currentType != null) {
                    PrintString.printLog("lifeCycle", "Player Fragment refreshInteface inside");
                    chbLoop.setChecked(isLooping);
                    track_name.setText(currentTrack.getName());
                    artist.setText(currentTrack.getArtist());
                    playerHelper.changePlayButton();
                    rating.setRating(currentTrack.getRating());
                    if (isRefreshByService) {
                        playerHelper.startSeekBarChanger();
                        if (playbackManager != null)
                            chbShuffle.setChecked(playbackManager.isShuffle);
                        else chbShuffle.setChecked(isShuffle);
                    }

                    if (isRefreshByService && playbackManager != null && sliderManager.isPrepared()) {
                        PrintString.printLog("lifeCycle", "Player Fragment setTrack");
                        sliderManager.setTrack(playbackManager.getCurrentPosition());
                    }
                }
            } catch (NullPointerException e) {
                PrintString.printLog("Error", "NullPointerException in refreshInterface()" + e.toString());
            }
        }
    }

    private void setVisibilityByType(Type type){
        if (type != Type.STREAM){
            chbLoop.setVisibility(View.VISIBLE);
            chbShuffle.setVisibility(View.VISIBLE);
            btn_prev.setVisibility(View.VISIBLE);
            btn_forward.setVisibility(View.VISIBLE);
            start_duration.setVisibility(View.VISIBLE);
            end_duration.setVisibility(View.VISIBLE);
            rating.setVisibility(View.VISIBLE);
            btn_prevItem.setVisibility(View.VISIBLE);
            btn_nextItem.setVisibility(View.VISIBLE);
            seek_bar.setVisibility(View.VISIBLE);
            btn_track_options.setEnabled(true);
        }
        else {
            btn_track_options.setEnabled(false);
            chbLoop.setVisibility(View.INVISIBLE);
            chbShuffle.setVisibility(View.INVISIBLE);
            seek_bar.setVisibility(View.GONE);
            btn_prev.setVisibility(View.INVISIBLE);
            btn_forward.setVisibility(View.INVISIBLE);
            start_duration.setVisibility(View.GONE);
            end_duration.setVisibility(View.GONE);
            rating.setVisibility(View.GONE);
            btn_prevItem.setVisibility(View.INVISIBLE);
            btn_nextItem.setVisibility(View.INVISIBLE);
        }
    }

    private InfoForPlayer getInfoForPlayer(){
        return new InfoForPlayer(currentType, currentItem, playlist, currentPosition, isShuffle, isLooping, currentURL);
    }

    public void setInfo(InfoForPlayer infoForPlayer){
        this.infoForPlayer = infoForPlayer;
    }

    @Override
    public void onClick(View v) {

        switch(v.getId())
        {
            case R.id.play_track:
                if (!playbackManager.playPressed())
                    new MySnackbar(getActivity(), btn_play, R.string.playlist_not_set).show();
                break;

            case R.id.prev_track:
                prevTrack();
                break;

            case R.id.forward_track:
                nextTrack();
                break;

            case R.id.prev_item:
                if (playbackManager != null && playbackManager.isValid() && sliderManager.isPrepared()) {
                    ArrayList<SQLItem> items = manager.getItemsFromTable(playerHelper.getStringType(currentType));
                    boolean found = false;
                    for (int i = items.size() - 1; i >= 0; i--) {
                        if (found) {
                            ArrayList<Track> list = manager.getTracksFromPlaylistAndMemory(playerHelper.getStringType(currentType), items.get(i).getName());
                            if (list.size() != 0) {
                                sliderManager.resetSettings();
                                playlist = list;
                                currentItem = items.get(i).getName();
                                currentPosition = 0;
                                infoForPlayer = getInfoForPlayer();
                                playbackManager.isShuffled = false;
                                startPlay();
                                sliderManager.setInfo(playbackManager.getPlaylist().size(), playbackManager.getCurrentPosition(), currentType);
                                break;
                            }
                        } else if (items.get(i).getName().equals(currentItem)) found = true;
                    }
                }
                else new MySnackbar(getActivity(), btn_play, R.string.loading).show();
                break;
            case R.id.next_item:
                if (playbackManager != null && playbackManager.isValid() && sliderManager.isPrepared()) {
                    ArrayList<SQLItem> items = manager.getItemsFromTable(playerHelper.getStringType(currentType));
                    boolean found = false;
                    for (SQLItem item : items) {
                        if (found) {
                            ArrayList<Track> list = manager.getTracksFromPlaylistAndMemory(playerHelper.getStringType(currentType), item.getName());
                            if (list.size() != 0) {
                                sliderManager.resetSettings();
                                playlist = manager.getTracksFromPlaylistAndMemory(playerHelper.getStringType(currentType), item.getName());
                                currentItem = item.getName();
                                currentPosition = 0;
                                infoForPlayer = getInfoForPlayer();
                                playbackManager.isShuffled = false;
                                startPlay();
                                sliderManager.setInfo(playbackManager.getPlaylist().size(), playbackManager.getCurrentPosition(), currentType);
                                break;
                            }
                        } else if (item.getName().equals(currentItem)) found = true;
                    }
                } else new MySnackbar(getActivity(), btn_play, R.string.loading).show();
                break;
            case R.id.note:
                PlayerTrackDialog dialog = new PlayerTrackDialog(getActivity(), new PlayerTrackDialog.PlayerTrackDialogListener() {
                    @Override
                    public void userSelectedDelete() {
                        if (playbackManager != null && playbackManager.isValid()) {
                            manager.deleteTrack(playbackManager.getCurrentTrack());
                            sliderManager.delete(playbackManager.getCurrentPosition());
                            playbackManager.removeCurrentTrack();
                            playlist = playbackManager.getPlaylist();
                        }
                    }

                    @Override
                    public void userSelectedInfo() {
                        if (currentTrack != null) {
                            info_fragment = new Info();
                            info_fragment.setTrack(currentTrack);
                            startInfoFragment();
                        }
                    }

                    @Override
                    public void userSelectedToPlaylist() {
                        if (playbackManager != null &&  playbackManager.isValid()) {
                            manager = new FileManager(getActivity());
                            if (manager.getCountOfItems(DBHelper.TABLE_PLAYLIST_NAME) != 0) {
                                AddToPlaylistDialog dialog = new AddToPlaylistDialog(getActivity(), new AddToPlaylistDialog.MyAddToPlaylistDialogListener() {
                                    @Override
                                    public void userSelectedPlaylist(String name, String url) {
                                        File file = new File(url);
                                        if (file.exists()) {
                                            M3UParser parser = new M3UParser(file, getActivity());
                                            ArrayList<Track> track = new ArrayList<>();
                                            track.add(playbackManager.getCurrentTrack());
                                            if (!parser.addTracksToPlaylist(track)){
                                                new MySnackbar(getActivity(), btn_play, R.string.cant_write).show();
                                            } else new MySnackbar(getActivity(), btn_play, R.string.successfully_added).show();
                                        }
                                    }
                                });
                                dialog.show();
                            } else
                                new MySnackbar(getActivity(), btn_play, R.string.playlists_not_found, true).show();
                        }
                    }

                    @Override
                    public void userSelectedShare() {
                        if (currentTrack != null) {
                            ShareDialog dialog = new ShareDialog(getActivity(), currentTrack.cloneTrack());
                            dialog.show();
                        }
                    }

                    @Override
                    public void userSelectedMegamixCreator() {
                        if (currentTrack != null && currentTrack.getType() == Track.MEMORY_TRACK) {
                            if (Settings.checkExternalPath(currentTrack.getPlaylistPath())) {
                                ArrayList<Track> list = new ArrayList<>();
                                for (Track track : playbackManager.getPlaylist()){
                                    if (track.getType() != Track.INTERNET_TRACK) list.add(track);
                                }
                                if (list.size()!=0) {
                                    PrintString.printLog("MegamixCreator", currentPosition + "");
                                    MegamixCreator megamixCreator = MainActivity.get_creator();
                                    megamixCreator.setTracks(list, list.indexOf(playbackManager.getPlaylist().get(currentPosition)), playerHelper.getStringType(currentType), currentItem);
                                    fTrans = getActivity().getSupportFragmentManager().beginTransaction();
                                    if (Settings.ANIMATION) fTrans.setCustomAnimations(R.anim.slide_right_to_left, R.anim.slide_in_left,R.anim.slide_left_to_right, R.anim.slide_in_right);
                                    fTrans.replace(R.id.main_fragment, megamixCreator);
                                    fTrans.addToBackStack(null);
                                    fTrans.commit();
                                } else new MySnackbar(getActivity(), btn_play, R.string.megamix_memory_only).show();
                            } else new MySnackbar(getActivity(), btn_play, R.string.permission_denied, true).show();
                        } else new MySnackbar(getActivity(), btn_play, R.string.megamix_memory_only).show();
                    }

                    @Override
                    public void userSelectedCommentTrack() {
                        if (currentTrack!=null && Settings.checkExternalPath(currentTrack.getPlaylistPath()) && currentTrack.getType() == Track.MEMORY_TRACK){
                            CommentDialog dialog = new CommentDialog(getActivity(), currentTrack.getComment(), new CommentDialog.CommentDialogListener() {
                                @Override
                                public void commentSaved(String comment) {
                                    currentTrack.setComment(comment);
                                    manager.setTrack(currentTrack);
                                }
                            });
                            dialog.show();
                        }
                    }

                    @Override
                    public void userSelectedSimilarTracks() {
                        if (playerHelper.isValid()) {
                            lastFmFragment = new LastFmFragment();
                            lastFmFragment.getSimilarTracks(currentTrack.getArtist(), currentTrack.getName());
                            fTrans = getActivity().getSupportFragmentManager().beginTransaction();
                            if (Settings.ANIMATION) fTrans.setCustomAnimations(R.anim.slide_right_to_left, R.anim.slide_in_left,R.anim.slide_left_to_right, R.anim.slide_in_right);
                            fTrans.replace(R.id.main_fragment, lastFmFragment);
                            fTrans.addToBackStack(null);
                            fTrans.commit();
                        }
                    }

                    @Override
                    public void userSelectedLoveTrack() {
                        if (currentTrack != null) {
                            LastFmEngine lastFmEngine = MainActivity.getLastFmEngine();
                            if (lastFmEngine != null && lastFmEngine.loveTrack(currentTrack.getArtist(), currentTrack.getName()))
                                new MySnackbar(getActivity(), btn_play, R.string.successfully_liked).show();
                            else
                                new MySnackbar(getActivity(), btn_play, R.string.error_love).show();
                        }
                    }

                    @Override
                    public void userSelectedArtistsTracks() {
                        if (playerHelper.isValid()) {
                            lastFmFragment = new LastFmFragment();
                            lastFmFragment.getArtistsTracks(currentTrack.getArtist());
                            fTrans = getActivity().getSupportFragmentManager().beginTransaction();
                            if (Settings.ANIMATION) fTrans.setCustomAnimations(R.anim.slide_right_to_left, R.anim.slide_in_left,R.anim.slide_left_to_right, R.anim.slide_in_right);
                            fTrans.replace(R.id.main_fragment, lastFmFragment);
                            fTrans.addToBackStack(null);
                            fTrans.commit();
                        }
                    }
                });
                dialog.show();
                break;

            case R.id.options:
                showMenu(btn_options);
                break;
        }
    }

    private void nextTrack(){
        //if (playbackManager!= null) currentPosition = playbackManager.getCurrentPosition();
        if (playlist!=null && currentPosition + 1 < playlist.size()) {
            currentPosition++;
            resetTracksTimers();
            tracksTimer = new Timer();
            tracksMyTimer = new TracksTask();
            tracksTimer.schedule(tracksMyTimer, TracksTask.TRACKS_DELAY);
            currentTrack = playlist.get(currentPosition);
            refreshInterface(false);
            if (sliderManager.isPrepared()) sliderManager.setSliderPosition(currentPosition);
        } else new MySnackbar(getActivity(), btn_play, R.string.end_of_playlist);
    }

    private void prevTrack(){
        //if (playbackManager!= null) currentPosition = playbackManager.getCurrentPosition();
        if (playlist!=null && currentPosition - 1 >= 0) {
            currentPosition--;
            resetTracksTimers();
            tracksTimer = new Timer();
            tracksMyTimer = new TracksTask();
            tracksTimer.schedule(tracksMyTimer, TracksTask.TRACKS_DELAY);
            currentTrack = playlist.get(currentPosition);
            refreshInterface(false);
            if (sliderManager.isPrepared()) sliderManager.setSliderPosition(currentPosition);
        } else new MySnackbar(getActivity(), btn_play, R.string.end_of_playlist);
    }

    private void resetTracksTimers(){
        if (tracksTimer!=null){
            tracksTimer.cancel();
            tracksTimer = null;
        }
        if (tracksMyTimer != null){
            tracksMyTimer.cancel();
            tracksMyTimer = null;
        }
    }

    private void startInfoFragment() {
        fTrans = getActivity().getSupportFragmentManager().beginTransaction();
        fTrans.replace(R.id.main_fragment,info_fragment);
        fTrans.addToBackStack(null);
        fTrans.commit();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        if (b){
            start_duration.setText(playerHelper.userInterfaceDuration(i));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        isSeekBarDragging = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        try {
            if (currentTrack.isMegamixTrack()) playbackManager.seekTo(seekBar.getProgress() + Integer.valueOf(currentTrack.getStartPoint()));
            else playbackManager.seekTo(seekBar.getProgress());
            isSeekBarDragging = false;
        }catch (NullPointerException ignored){}
    }

    public void addOrder(ArrayList<Track> order){
        playbackManager.addOrder(order);
    }

    @Override
    public void putArguments(HashMap<String, String> data) {
        super.putArguments(data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        clearViews();
    }

    @Override
    public void getOneMoreTrack(int position) {
        sliderManager.setOneMoreTrack(playbackManager.getTrack(position), position);
    }

    @Override
    public void sliderPrepared() {
        sliderManager.setTrack(playbackManager.getCurrentPosition());
    }

    public boolean isValidToStartPlayback(){
        return playbackManager != null;
    }

    public void orderChanged(int position, ArrayList<Track> playlist){
        if (isAdded()) {
            sliderManager.orderChanged(position);
            currentPosition = position;
            this.playlist = playlist;
        }
    }

    protected void showMenu(View v){
        PopupMenu popupMenu = new PopupMenu(getActivity(), v);
        popupMenu.inflate(R.menu.options_menu);

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.sleep_timer:
                        if (playbackManager != null) {
                            SleepTimerDialog dialog = new SleepTimerDialog(getActivity(), playbackManager.isSleepTimer());
                            dialog.setSleepTimerDialogListener(new SleepTimerDialog.SleepTimerDialogListener() {
                                @Override
                                public void setTimerClicked(int minutes) {
                                    playbackManager.setSleepTimer(minutes);
                                }

                                @Override
                                public void negativeButtonClicked() {
                                }

                                @Override
                                public void stopTimerClicked() {
                                    playbackManager.stopSleepTimer();
                                }
                            });
                            dialog.show();
                        }
                        break;
                }
                return false;
            }
        });
        popupMenu.show();
    }
}