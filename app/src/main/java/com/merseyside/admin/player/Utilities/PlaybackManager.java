package com.merseyside.admin.player.Utilities;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.merseyside.admin.player.ActivitesAndFragments.MainActivity;
import com.merseyside.admin.player.ActivitesAndFragments.Player_Fragment;
import com.merseyside.admin.player.AdaptersAndItems.Track;
import com.merseyside.admin.player.BroadcastListeners.AFMaster;
import com.merseyside.admin.player.BroadcastListeners.MyBroadcastReceiver;
import com.merseyside.admin.player.BroadcastListeners.MyPhoneStateListener;
import com.merseyside.admin.player.BroadcastListeners.RemoteControlReceiver;
import com.merseyside.admin.player.LastFm.LastFmEngine;
import com.merseyside.admin.player.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Admin on 20.12.2016.
 */

public class PlaybackManager extends Service  {

    private final String TAG = "PlaybackManager";

    public class SleepTimerTask extends TimerTask{

        @Override
        public void run() {
            isSleepTimerOver = true;
        }
    }

    private NotificationManager mNotificationManager;
    MyBinder binder = new MyBinder();
    private  NotificationCompat.Builder notificationBuilder;

    public interface MyPlaybackManagerListener{
        void playPlaylist(Track track);
        void playMemory(Track track);
        void playStream(String item, String URL);
        void error(String err);
        void playButtonPressed();
    }

    private MyPlaybackManagerListener myPlaybackManagerListener;

    public class MyBinder extends Binder {
        public PlaybackManager getService() {
            return PlaybackManager.this;
        }
    }

    private MyBroadcastReceiver broadcast_reciever;
    private RemoteControlReceiver control_reciever;
    private IntentFilter noisyFilter, headsetFilter;
    private AudioManager audioManager;
    private ComponentName mediaComponent;
    private AFMaster afMaster;
    private MyPhoneStateListener phoneListener;
    private TelephonyManager mTelephonyMgr;
    private LastFmEngine lastFmEngine;

    private Timer mTimer;
    private SleepTimerTask sleepTimerTask;

    FileManager manager;

    private TracksPlayer currentPlayer, mainPlayer, additionalPlayer;

    public enum Action{PLAY}
    private ArrayList<Track> playlist;

    private boolean isPlaying = false;
    private boolean isReady = false;
    private boolean isLooping = false;
    private boolean isMegamix = false;
    public boolean isShuffle = false;
    private boolean isBind = false;
    public boolean isShuffled =  false;
    private boolean isAuto = false;
    private boolean isAvailableToPlay = true;
    private boolean isWaitingToContinue = false;
    private boolean isActivityLoading = false;
    private boolean isSleepTimerOver = false;

    private String currentItem;
    private Player_Fragment.Type currentType;
    private int currentPosition;
    private String currentURL;
    private long duration;
    private Track currentTrack;

    private Settings settings;

    public void setMyPlaybackManagerListener(MyPlaybackManagerListener myPlaybackManagerListener){
        this.myPlaybackManagerListener = myPlaybackManagerListener;
    }
    @Override
    public IBinder onBind(Intent intent) {
        isBind = true;
        return binder;
    }

    public void updateWidget(boolean isClosing){
        Context context = getApplicationContext();
        try {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
            ComponentName thisWidget = new ComponentName(context, WidgetProvider.class);
            if (currentType != Player_Fragment.Type.STREAM) {
                remoteViews.setTextViewText(R.id.title, currentTrack.getName());
                remoteViews.setTextViewText(R.id.artist, currentTrack.getArtist());
                remoteViews.setImageViewBitmap(R.id.cover, currentTrack.getCover());
                remoteViews.setTextViewText(R.id.count, currentPosition+1 + "/" + playlist.size());
            } else {
                remoteViews.setTextViewText(R.id.title, currentItem);
                remoteViews.setTextViewText(R.id.artist, currentURL);
                remoteViews.setImageViewBitmap(R.id.cover, BitmapFactory.decodeResource(context.getResources(), R.drawable.internet));
                remoteViews.setTextViewText(R.id.count, "1/1");
            }
            if (isClosing) remoteViews.setImageViewResource(R.id.play, R.drawable.play);
            else {
                if (isNowPlaying()) remoteViews.setImageViewResource(R.id.play, R.drawable.pause);
                else remoteViews.setImageViewResource(R.id.play, R.drawable.play);
            }
            appWidgetManager.updateAppWidget(thisWidget, remoteViews);
        } catch (NullPointerException ignored){
            if (!isActivityLoading) {
                isActivityLoading = true;
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            if (intent.getAction().equals(ServiceConstants.ACTION.STARTFOREGROUND_ACTION)) {
                startForeground(ServiceConstants.NOTIFICATION_ID.FOREGROUND_SERVICE, getMyActivityNotification());
            } else if (intent.getAction().equals(ServiceConstants.ACTION.PREV_ACTION)) {
                previousTrack();
                updateNotification();
            } else if (intent.getAction().equals(ServiceConstants.ACTION.PLAY_ACTION)) {
                playPressed();
                updateNotification();
            } else if (intent.getAction().equals(ServiceConstants.ACTION.NEXT_ACTION)) {
                nextTrack();
                updateNotification();
            } else if (intent.getAction().equals(ServiceConstants.ACTION.STOPFOREGROUND_ACTION)) {
                settings.savePreference(Settings.APP_PREFERENCES_LAST_POSITION, currentPosition);
                stopForeground(true);
                stopSelf();
            } else if (intent.getAction().equals(ServiceConstants.ACTION.CLOSE_ACTION)) {
                settings.close(false);
            }
            updateWidget(false);
        } catch (NullPointerException ignored){}
        return START_STICKY;
    }

    private Notification getMyActivityNotification() {
        Notification notification;

        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }

        if (notificationBuilder == null) {
            notificationBuilder = new NotificationCompat.Builder(this, ServiceConstants.CHANNEL.CHANNEL_ID);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            String name = ServiceConstants.CHANNEL.VERBOSE_NOTIFICATION_CHANNEL_NAME;
            String description = ServiceConstants.CHANNEL.VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION;
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(ServiceConstants.CHANNEL.CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableLights(true);
            channel.setLightColor(Color.GREEN);

            mNotificationManager.createNotificationChannel(channel);
        }

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, (int)System.currentTimeMillis(),
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent previousIntent = new Intent(this, PlaybackManager.class);
        previousIntent.setAction(ServiceConstants.ACTION.PREV_ACTION);
        PendingIntent ppreviousIntent = PendingIntent.getService(this, 0,
                previousIntent, 0);

        Intent playIntent = new Intent(this, PlaybackManager.class);
        playIntent.setAction(ServiceConstants.ACTION.PLAY_ACTION);
        PendingIntent pplayIntent = PendingIntent.getService(this, 0,
                playIntent, 0);

        Intent nextIntent = new Intent(this, PlaybackManager.class);
        nextIntent.setAction(ServiceConstants.ACTION.NEXT_ACTION);
        PendingIntent pnextIntent = PendingIntent.getService(this, 0,
                nextIntent, 0);

        Intent closeIntent = new Intent(this, PlaybackManager.class);
        closeIntent.setAction(ServiceConstants.ACTION.CLOSE_ACTION);
        PendingIntent pcloseIntent = PendingIntent.getService(this, 0,
                closeIntent, 0);

        Bitmap icon;
        String title;
        String text;
        String count;
        if (currentType != Player_Fragment.Type.STREAM){
            icon = currentTrack.getCover();
            if (icon == null){
                icon = BitmapFactory.decodeResource(getResources(), settings.getAttributeId(settings.getThemeByString(), R.attr.theme_dependent_track_cover));
            }
            title = currentTrack.getName();
            text = currentTrack.getArtist();
            if (playlist!=null)
            count = currentPosition+1+"/"+playlist.size();
            else count = "0/0";
        }
        else {
            icon = BitmapFactory.decodeResource(getResources(), R.drawable.internet);
            title = currentItem;
            text = currentURL;
            count = "1/1";
        }
        RemoteViews remoteViews = new RemoteViews(getPackageName(),
                settings.getAttributeId(settings.getThemeByString(), R.attr.theme_dependent_notification));

        remoteViews.setOnClickPendingIntent(R.id.prev, ppreviousIntent);
        remoteViews.setOnClickPendingIntent(R.id.play, pplayIntent);
        remoteViews.setOnClickPendingIntent(R.id.next, pnextIntent);
        remoteViews.setOnClickPendingIntent(R.id.close, pcloseIntent);

        remoteViews.setImageViewBitmap(R.id.cover, icon);
        remoteViews.setTextViewText(R.id.name, title);
        remoteViews.setTextViewText(R.id.artist, text);
        remoteViews.setTextViewText(R.id.count, count);

        if (isNowPlaying()) remoteViews.setImageViewResource(R.id.play, R.drawable.pause);
        else remoteViews.setImageViewResource(R.id.play, R.drawable.play);

        notificationBuilder
                .setSmallIcon(R.drawable.equalizer_icon)
                .setContentTitle(title)
                .setContentText(text)
                .setTicker(getString(R.string.app_name))
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .setPriority(Notification.PRIORITY_MAX)
                .setOnlyAlertOnce(true)
                .setContent(remoteViews);

        return notificationBuilder.build();
    }

    private void updateNotification() {
        Notification notification = getMyActivityNotification();

        mNotificationManager.notify(ServiceConstants.NOTIFICATION_ID.FOREGROUND_SERVICE, notification);
        startForeground(ServiceConstants.NOTIFICATION_ID.FOREGROUND_SERVICE, notification);
    }

    private void setEndOfPlayback(){
        isPlaying = false;
        myPlaybackManagerListener.playButtonPressed();
        updateNotification();
        updateWidget(false);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        settings = new Settings(getApplicationContext());
        mainPlayer = new TracksPlayer(getApplicationContext());
        mainPlayer.setPlayerListener(new TracksPlayer.MyPlayerInterface() {
            @Override
            public void endOfTrack() {
                if (!nextTrack()) setEndOfPlayback();

            }

            @Override
            public void endOfTrackIsClose() {
                isAuto = true;
                if (!nextTrack()) setEndOfPlayback();
            }

            @Override
            public void middleOfTrack() {
                PrintString.printLog("LastFm", "middle");
                if (Settings.LASTFM_SCROBBLE) lastFmEngine.scrobbleTrack(currentTrack.getArtist(), currentTrack.getName());
            }
        });
        additionalPlayer = new TracksPlayer(getApplicationContext());
        additionalPlayer.setPlayerListener(new TracksPlayer.MyPlayerInterface() {
            @Override
            public void endOfTrack() {
                if (!nextTrack()) setEndOfPlayback();
            }

            @Override
            public void endOfTrackIsClose() {
                isAuto = true;
                if (!nextTrack()) setEndOfPlayback();
            }

            @Override
            public void middleOfTrack() {
                PrintString.printLog("LastFm", "middle");
                if (Settings.LASTFM_SCROBBLE) lastFmEngine.scrobbleTrack(currentTrack.getArtist(), currentTrack.getName());
            }
        });
        manager = new FileManager(getApplicationContext());
        currentPlayer = mainPlayer;
        lastFmEngine = MainActivity.getLastFmEngine();
        initBroadcast();
    }

    public boolean setPlayerInfo(InfoForPlayer infoForPlayer){
        if (mainPlayer.exist() || additionalPlayer.exist()) releaseMP();
        isPlaying = false;
        currentType = infoForPlayer.getType();
        if (currentType == Player_Fragment.Type.STREAM) {
            currentURL = infoForPlayer.getURL();
            currentItem = infoForPlayer.getItem();
            if (playlist != null) playlist.clear();
        }
        else {
            playlist = infoForPlayer.getPlaylist();
            currentPosition = infoForPlayer.getCurrentTrack_position();
            if (currentPosition > playlist.size()-1) currentPosition = 0;
            currentItem = infoForPlayer.getItem();
            currentTrack = playlist.get(currentPosition);
            duration = currentTrack.getDurationLong();
            shufflePlaylist(infoForPlayer.isShuffle());
            setLooping(infoForPlayer.isLooping());
            updateWidget(false);
        }
        return isValid();
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
                if (lastFmEngine == null) lastFmEngine = MainActivity.getLastFmEngine();
            }
        }
        else if (currentType == Player_Fragment.Type.STREAM){
            if (currentURL == null){
                flag = false;
                PrintString.printLog("Error", "CurrentURL is wrong");
            }
        }
        else return false;
        return flag;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
    }

    public void initBroadcast(){
        audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        phoneListener = new MyPhoneStateListener();
        mTelephonyMgr = (TelephonyManager)getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        mTelephonyMgr.listen(phoneListener, MyPhoneStateListener.LISTEN_CALL_STATE);
        phoneListener.setCallingListener(new MyPhoneStateListener.MyCallingListener() {
            @Override
            public void callingStart() {
                getIntent(ServiceConstants.ACTION.CALLING_START);

            }

            @Override
            public void callingEnd() {
                getIntent(ServiceConstants.ACTION.CALLING_END);

            }
        });
        afMaster = new AFMaster();
        afMaster.setMyAudioFocusListener(new AFMaster.MyAudioFocusListener() {
            @Override
            public void audioFocusGain() {
                getIntent(ServiceConstants.ACTION.AUDIOFOCUS_GAIN);
            }

            @Override
            public void audioFocusTransient() {
                getIntent(ServiceConstants.ACTION.AUDIOFOCUS_TRANS);
            }

            @Override
            public void audioFocusTransientCanDuck() {
                getIntent(ServiceConstants.ACTION.AUDIOFOCUS_DUCK);
            }

            @Override
            public void audioFocusLoss() {
                getIntent(ServiceConstants.ACTION.AUDIOFOCUS_LOSS);
            }
        });

        broadcast_reciever = new MyBroadcastReceiver(new MyBroadcastReceiver.MyBroadcastListener() {
            @Override
            public void becomeNoisy() {
                getIntent(ServiceConstants.ACTION.BECOME_NOISY);
            }
        });
        control_reciever = new RemoteControlReceiver();
        control_reciever.setRemoteControlListener(new RemoteControlReceiver.MyRemoteControlListener() {
            @Override
            public void playButtonPressed() {
                getIntent(ServiceConstants.ACTION.PLAY_ACTION);
            }

            @Override
            public void nextTrackButtonPressed() {
                getIntent(ServiceConstants.ACTION.NEXT_ACTION);
            }

            @Override
            public void previousTrackButtonPressed() {
                getIntent(ServiceConstants.ACTION.PREV_ACTION);
            }

            @Override
            public void headsetPlugged() {
                getIntent(ServiceConstants.ACTION.HEADSET_PLUGGED);
            }
        });

        noisyFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);

        headsetFilter = new IntentFilter();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            headsetFilter.addAction(AudioManager.ACTION_HEADSET_PLUG);
        }
        headsetFilter.addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED);

        getApplicationContext().registerReceiver(control_reciever, headsetFilter);

        mediaComponent = new ComponentName(getApplicationContext().getPackageName(), RemoteControlReceiver.class.getName());
        audioManager.registerMediaButtonEventReceiver(mediaComponent);
    }

    private void playStream(){
        if (isPlaying) mainPlayer.releaseMP();
        if (isReady = isValid()) {
            isPlaying = true;
            mainPlayer.playStream(currentURL);
            getApplicationContext().registerReceiver(broadcast_reciever.myNoisyAudioStreamReceiver, noisyFilter);
            myPlaybackManagerListener.playStream(currentItem,currentURL);
        }
        else myPlaybackManagerListener.error("Something wrong");
    }

    private void playMemory(){
        if (playTrack() && isBind) myPlaybackManagerListener.playMemory(currentTrack);
        else myPlaybackManagerListener.error("Something wrong");

    }

    private void playPlaylist(){
        PrintString.printLog("lifeCycle","Playback Manager playplaylist Position " + getCurrentPosition());
        if (playTrack() && isBind) myPlaybackManagerListener.playPlaylist(currentTrack);
        else myPlaybackManagerListener.error("Something wrong");
    }

    private boolean playTrack(){
        if (isMegamix) currentTrack = getMegamixTrack(playlist.get(currentPosition));
        else currentTrack = playlist.get(currentPosition);
        if (isReady = isValid()){
            if (Settings.LASTFM_NOW_PLAYING) lastFmEngine.nowPlayingTrack(currentTrack.getArtist(), currentTrack.getName());
            getApplicationContext().registerReceiver(broadcast_reciever.myNoisyAudioStreamReceiver, noisyFilter);
            isPlaying = true;
            currentPlayer.playTrack(currentTrack);
        }
        return isReady;
    }

    private Track getMegamixTrack(Track track){
        if (track.isMegamixTrack()) return track;
        else {
            Track megamix_track = track.cloneTrack();
            int duration = (int)megamix_track.getDurationForPlayer();
            Random rand = new Random();

            int new_dur = (int)(duration * Settings.MEGAMIX_DURATION);
            int start = (duration - new_dur)/2;
            megamix_track.set_start_point(start);
            megamix_track.set_end_point(start + new_dur);

            if (Settings.TRANSITION != 0){
                megamix_track.setTransition(Settings.TRANSITION);
                megamix_track.setCrossfadeDuration(1000);
                megamix_track.setIncrease(1500);
                megamix_track.setFading(1000);
                megamix_track.setTransit_duration(rand.nextInt(4000)+6000);
            } else {
                int dur = 2000;
                megamix_track.setCrossfadeDuration(dur);
                megamix_track.setIncrease(dur);
                megamix_track.setFading(dur);
            }
            megamix_track.setMegamixTrack(true);
            return megamix_track;
        }
    }

    private void startPlay(){
        currentPlayer = getPlayer();
        if (isAuto) isAuto = false;
        else {
            releaseMP();
        }
        if (currentType == Player_Fragment.Type.MEMORY){
            playMemory();
        }
        else if(currentType == Player_Fragment.Type.STREAM){
            playStream();
        }
        else if(currentType == Player_Fragment.Type.PLAYLIST){
            playPlaylist();
        }
    }

    public boolean playPressed() {
        if (isReady = isValid()) {
            if (!isNowPlaying()) afMaster.getAudioFocus(audioManager);
            if (currentType == Player_Fragment.Type.STREAM){
                if (isNowPlaying()) {
                    currentPlayer.onClick(Action.PLAY);
                }
                else startPlay();
            }
            else {
                if (isPlaying) {
                    Log.d("Player", "here");
                    releaseIfPlaying();
                    currentPlayer.onClick(Action.PLAY);
                } else {
                    startPlay();
                }
            }
            myPlaybackManagerListener.playButtonPressed();
            updateNotification();
            updateWidget(false);
        }
        return isReady;
    }

    public boolean previousTrack(){
        if (isReady = isValid()) {
            if (currentType!=Player_Fragment.Type.STREAM) {
                afMaster.getAudioFocus(audioManager);
                if (currentPosition - 1 >= 0) {
                    currentPosition--;
                    releaseIfPlaying();
                    startPlay();
                    updateNotification();
                    updateWidget(false);
                } else return false;
            }
        }
        return isReady;
    }

    public boolean nextTrack(){
        if (!isSleepTimerOver) {
            if (isReady = isValid()) {
                if (currentType != Player_Fragment.Type.STREAM) {
                    afMaster.getAudioFocus(audioManager);
                    if (currentPosition + 1 < playlist.size()) {
                        currentPosition++;
                        releaseIfPlaying();
                        startPlay();
                        updateNotification();
                        updateWidget(false);
                    } else if (isAuto){
                        isPlaying = false;
                        return false;
                    } else return false;
                }
            }
        } else {
            isSleepTimerOver = false;
            stopSleepTimer();
            if (!currentPlayer.isPlaying())settings.close(false);
            setEndOfPlayback();
        }
        return isReady;
    }

    public void releaseIfPlaying(){
        if (mainPlayer.exist() && mainPlayer!=currentPlayer && mainPlayer.isPlaying()) mainPlayer.releaseMP();
        if (additionalPlayer.exist() && additionalPlayer!=currentPlayer && additionalPlayer.isPlaying()) additionalPlayer.releaseMP();
    }

    public void setLooping(boolean isLooping){
        this.isLooping = isLooping;
        currentPlayer.setLooping(isLooping);
    }

    public void shufflePlaylist(boolean isShuffle){
        if (playlist!=null) {
            this.isShuffle = isShuffle;
            if (isShuffle) {
                if (!isShuffled) {
                    Collections.shuffle(playlist);
                    try {
                        int index = playlist.indexOf(currentTrack);
                        Collections.swap(playlist, currentPosition, index);
                    } catch (IndexOutOfBoundsException ignored){}
                    isShuffled = true;
                    if (!isPlaying) myPlaybackManagerListener.playPlaylist(getCurrentTrack());
                }
            } else {
                Track.setSortBy(Track.POSITION);
                Collections.sort(playlist);
                isShuffled = false;
            }
            this.isShuffle = isShuffle;
        }
    }

    public void setMegamixMode(boolean isMegamix){
        this.isMegamix = isMegamix;
    }

    public int getCurrentProgress(){
        if (currentPlayer.exist()){
            return currentPlayer.getCurrentPosition();
        }
        return 0;
    }

    public boolean isNowPlaying() {
        return isPlaying && currentPlayer.exist() && currentPlayer.isPlaying();
    }

    public void seekTo(int pos){
        if (currentPlayer.exist()) {
            currentPlayer.seekTo(pos);
        }
    }

    private void releaseMP(){
        mainPlayer.releaseMP();
        additionalPlayer.releaseMP();
    }

    public boolean isAlreadyPlaying(){
        return isValid();
    }

    public InfoForPlayer getInformation(){
        return new InfoForPlayer(currentType, currentItem, playlist, currentPosition, isShuffle, isLooping, currentURL);
    }

    public void setBind(boolean isAttach){
        this.isBind = isAttach;
    }

    private TracksPlayer getPlayer(){
        if (isAuto){
            if (mainPlayer.exist() && mainPlayer.isPlaying()) return additionalPlayer;
            else if (additionalPlayer.exist() && additionalPlayer.isPlaying()) return mainPlayer;
        }
        else {
            return mainPlayer;
        }
        return mainPlayer;
    }

    public ArrayList<Track> getPlaylist(){
        if (playlist != null) return (ArrayList<Track>) playlist.clone();
        else return null;
    }

    public void setPlaylist(ArrayList<Track> playlist) {
        this.playlist = playlist;
    }

    public void startPlayByPosition(int position){
        currentPosition = position;
        isPlaying = false;
        playPressed();
        updateNotification();
        updateWidget(false);

    }

    public int getCurrentPosition(){
        if (playlist != null)
            return currentPosition;
        else return -1;
    }

    public void setCurrentPosition(int position){
        currentPosition = position;
    }

    public void addOrder(ArrayList<Track> order){
        if (playlist!=null){
            playlist.addAll(currentPosition+1, order);
        }
    }

    public Track getCurrentTrack(){
        if (currentPosition < playlist.size())
            return playlist.get(currentPosition);
        else {
            currentPlayer.releaseMP();
            return null;
        }
    }

    public void removeCurrentTrack(){
        playlist.remove(currentPosition);
        releaseIfPlaying();
        if (currentPosition == playlist.size() && currentPosition!=0) currentPosition--;
        myPlaybackManagerListener.playPlaylist(getCurrentTrack());
        if (isPlaying){
            isPlaying = false;
            playPressed();
        }
    }

    public Track getTrack(int position){
        try {
            return playlist.get(position);
        } catch (IndexOutOfBoundsException e){
            return null;
        } catch (NullPointerException ignored){
            return null;
        }
    }

    public boolean setRating(int rating){
        Track track = playlist.get(currentPosition);
        if (track != null) {
            if (track.getRating() == rating) return false;
            track.setRating(rating);
            playlist.set(currentPosition, track);
        }
        return true;
    }

    private void getIntent(String action){
        switch (action){
            case ServiceConstants.ACTION.AUDIOFOCUS_DUCK:
                PrintString.printLog("Intent", action);
                if (currentPlayer.exist() && isNowPlaying()) {
                    currentPlayer.setVolumeMin();
                    isWaitingToContinue = true;
                }
                break;

            case ServiceConstants.ACTION.AUDIOFOCUS_GAIN:
                PrintString.printLog("Intent", action);
                if (isPlaying && isAvailableToPlay && isWaitingToContinue){
                    if (isNowPlaying()) currentPlayer.setVolumeMax();
                    else {
                        currentPlayer.setVolumeMax();
                        playPressed();
                        updateWidget(false);
                        updateNotification();
                    }
                    isWaitingToContinue = false;
                }
                break;

            case ServiceConstants.ACTION.AUDIOFOCUS_TRANS:
                PrintString.printLog("Intent", action);
                if (isNowPlaying()){
                    playPressed();
                    isWaitingToContinue = true;
                    updateWidget(false);
                    updateNotification();
                }
                break;

            case ServiceConstants.ACTION.AUDIOFOCUS_LOSS:
                PrintString.printLog("Intent", action);
                if (isNowPlaying()){
                    playPressed();
                    isWaitingToContinue = false;
                    updateWidget(false);
                    updateNotification();
                }
                break;

            case ServiceConstants.ACTION.PLAY_ACTION:
                PrintString.printLog("Intent", action);
                if (isAvailableToPlay || isNowPlaying()) playPressed();
                break;

            case ServiceConstants.ACTION.PREV_ACTION:
                if (isAvailableToPlay || isNowPlaying()) previousTrack();
                PrintString.printLog("Intent", action);
                break;

            case ServiceConstants.ACTION.NEXT_ACTION:
                PrintString.printLog("Intent", action);
                if (isAvailableToPlay || isNowPlaying()) nextTrack();
                break;

            case ServiceConstants.ACTION.BECOME_NOISY:
                PrintString.printLog("Intent", action);
                if (currentPlayer != null && isNowPlaying()) playPressed();
                break;

            case ServiceConstants.ACTION.CALLING_START:
                if (currentPlayer.exist() && isNowPlaying()){
                    playPressed();
                    isAvailableToPlay = false;
                    isWaitingToContinue = true;
                    updateNotification();
                    updateWidget(false);
                }
                PrintString.printLog("Intent", action);
                break;

            case ServiceConstants.ACTION.CALLING_END:
                PrintString.printLog("Intent", action);
                isAvailableToPlay = true;
                if (isNowPlaying() && Settings.CONTINUE_AFTER_CALL && isWaitingToContinue){
                    isWaitingToContinue = false;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(3000);
                                Handler mainHandler = new Handler(getApplicationContext().getMainLooper());
                                Runnable myRunnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        if (!isNowPlaying()) playPressed();
                                        updateNotification();
                                        updateWidget(false);
                                    }
                                };
                                mainHandler.post(myRunnable);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
                break;

            case ServiceConstants.ACTION.HEADSET_PLUGGED:
                PrintString.printLog("Intent", action + " " + isPlaying + " " + isNowPlaying());

                if (isPlaying && !isNowPlaying()){
                    playPressed();
                    updateNotification();
                    updateWidget(false);
                }
                break;
        }
    }

    public void setSleepTimer(int minutes) {
        if (mTimer != null) mTimer.cancel();
        if (sleepTimerTask != null) sleepTimerTask.cancel();

        mTimer = new Timer();
        sleepTimerTask = new SleepTimerTask();
        mTimer.schedule(sleepTimerTask, minutes*60*1000);
    }

    public boolean isSleepTimer(){
        if (mTimer != null && sleepTimerTask != null) return true;
        else return false;
    }

    public void stopSleepTimer(){
        if (mTimer != null) mTimer.cancel();
        if (sleepTimerTask != null) sleepTimerTask.cancel();
        mTimer = null;
        sleepTimerTask = null;
    }
}