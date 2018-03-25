package com.merseyside.admin.player.LastFm;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import com.merseyside.admin.player.R;
import com.merseyside.admin.player.Utilities.FirebaseEngine;
import com.merseyside.admin.player.Utilities.Settings;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import de.umass.lastfm.Artist;
import de.umass.lastfm.Authenticator;
import de.umass.lastfm.Caller;
import de.umass.lastfm.Chart;
import de.umass.lastfm.PaginatedResult;
import de.umass.lastfm.Playlist;
import de.umass.lastfm.Session;
import de.umass.lastfm.Track;
import de.umass.lastfm.User;
import de.umass.lastfm.cache.FileSystemCache;

/**
 * Created by Admin on 12.03.2017.
 */

public class LastFmEngine {
    private enum TASK{RECENT_TRACKS, LOVED_TRACKS, TOP_TRACKS, WEEKLY_TRACK_CHART, WEEKLY_ARTIST_CHART, TOP_ARTISTS, ARTISTS_TRACKS, SIMILAR_TRACKS, LOVE, SCROBBLE, NOW_PLAYING}
    private AuthTask authTask;
    private TracksTask tracksTask;
    private Context context;
    private String key = "ad53640c1a31ced5c4f79199c30a319c";
    private String secret = "5b3a05f4f4db1762ff319bf69e166ccb";
    private String user;
    private String password;
    private ProgressDialog progress;
    private LastFmEventListener lastFmEventListener;
    private Session session;
    private boolean isAuth, authInProgress;
    private Settings settings;
    private int tracksPagesCount, currentTracksPage;
    private TASK currentTask;
    private ArtistsTask artistsTask;
    private LoveAndScrobbleTask loveAndScrobbleTask;

    public class AuthTask extends AsyncTask<Void, Void, Boolean> {
        private boolean showDialog;

        public AuthTask(boolean showDialog){
            this.showDialog = showDialog;
        }
        @Override
        protected Boolean doInBackground(Void... voids) {

            session = Authenticator.getMobileSession(user, password, key, secret);
            try {
                Playlist.create("example playlist", "description", session);
            } catch (NullPointerException ignored){
                return false;
            }
            return true;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            authInProgress = true;
            if (showDialog) {
                progress = new ProgressDialog(context, R.style.DialogStyle);
                progress.setTitle(context.getString(R.string.please_wait));
                progress.setMessage(context.getString(R.string.authorization_in_progress));
                progress.setCancelable(false);
                progress.show();
            }
        }

        @Override
        protected void onPostExecute(Boolean v) {
            super.onPostExecute(v);
            authInProgress = false;
            if (showDialog) progress.dismiss();
            isAuth = v;
            if (lastFmEventListener != null) {
                if (v) {
                    lastFmEventListener.authComplete();
                    FirebaseEngine.logEvent(context, "LAST_FM_AUTH", null);
                }
                else lastFmEventListener.authError();
            }
        }
    }

    public interface LastFmEventListener{
        void authComplete();
        void authError();
        void tracksLoaded(ArrayList<Track> tracks);
        void artistsLoaded(ArrayList<Artist> artists);
        void userInfoLoaded(User user);
    }
    public LastFmEngine(Context context){
        this.context = context;
        Caller.getInstance().setCache(new FileSystemCache(new
                File(context.getCacheDir().getAbsolutePath())));
        settings = new Settings(context);
        if (Settings.REMEMBER_PASS && !Settings.USERNAME.equals("") && !Settings.PASSWORD.equals("")){
            user = Settings.USERNAME;
            password = Settings.PASSWORD;
            auth(user, password, false);
        }
    }

    public void auth(String user, String password, boolean showProgress){
        this.user = user;
        this.password = password;
        if (settings.isOnline()) {
            authTask = new AuthTask(showProgress);
            authTask.execute();
        } else if (lastFmEventListener != null)lastFmEventListener.authError();
    }

    public String getUsername(){
        return user;
    }

    public String getPassword(){
        return password;
    }

    public void setLastFmEventListener(LastFmEventListener lastFmEventListener){
        this.lastFmEventListener = lastFmEventListener;
    }

    public boolean isAuth(){
        return isAuth;
    }

    public boolean isAuthInProgress(){
        return authInProgress;
    }

    public boolean getRecentTracks(){
        currentTask = TASK.RECENT_TRACKS;
        currentTracksPage = 1;
        return getTracks();
    }

    public boolean getLovedTracks(){
        currentTask = TASK.LOVED_TRACKS;
        currentTracksPage = 1;
        return getTracks();
    }

    public boolean getTopTracks(){
        currentTask = TASK.TOP_TRACKS;
        currentTracksPage = 1;
        return getTracks();
    }

    public boolean getWeeklyTrackChart(){
        currentTask = TASK.WEEKLY_TRACK_CHART;
        currentTracksPage = 1;
        return getTracks();
    }

    public boolean getWeeklyArtistChart(){
        currentTask = TASK.WEEKLY_ARTIST_CHART;
        return getArtists();
    }

    public boolean getTopArtists(){
        currentTask = TASK.TOP_ARTISTS;
        return getArtists();
    }

    private boolean getArtists(){
        if (settings.isOnline()){
            artistsTask = new ArtistsTask(currentTask);
            artistsTask.execute();
            return true;
        } else return false;
    }

    private boolean getTracks(){
        if (settings.isOnline()) {
            tracksTask = new TracksTask(currentTask);
            tracksTask.execute();
            return true;
        } else return false;
    }

    private boolean getTracks(String artist, String track){
        if (settings.isOnline()) {
            tracksTask = new TracksTask(currentTask, track, artist);
            tracksTask.execute();
            return true;
        } else return false;
    }

    public boolean getArtistsTracks(String artist){
        if (settings.isOnline()) {
            currentTask = TASK.ARTISTS_TRACKS;
            currentTracksPage = 1;
            getTracks(artist, null);
            return true;
        } else return false;
    }

    public boolean getSimilarTracks(String artist, String track){
        if (settings.isOnline()) {
            currentTask = TASK.SIMILAR_TRACKS;
            currentTracksPage = 1;
            getTracks(artist, track);
            return true;
        } else return false;
    }

    public boolean nextPage(){
        if (tracksPagesCount >= currentTracksPage+1) {
            currentTracksPage++;
            getTracks();
        }
        else return false;
        return true;
    }

    public boolean prevPage(){
        if (tracksPagesCount-1 > 0){
            currentTracksPage--;
            getTracks();
        }
        else return false;
        return true;
    }

    public boolean loveTrack(String artist, String track){
        if (isAuth && settings.isOnline()){
            loveAndScrobbleTask = new LoveAndScrobbleTask(TASK.LOVE, artist, track);
            loveAndScrobbleTask.execute();
            return true;
        }
        return false;
    }

    public boolean scrobbleTrack(String artist, String track){
        if (isAuth && settings.isOnline()){
            loveAndScrobbleTask = new LoveAndScrobbleTask(TASK.SCROBBLE, artist, track);
            loveAndScrobbleTask.execute();
            return true;
        }
        return false;
    }

    public boolean nowPlayingTrack(String artist, String track){
        if (isAuth && settings.isOnline()){
            if (loveAndScrobbleTask != null) loveAndScrobbleTask.cancel(true);
            loveAndScrobbleTask = new LoveAndScrobbleTask(TASK.NOW_PLAYING, artist, track);
            loveAndScrobbleTask.execute();
            return true;
        }
        return false;
    }

    public void getUserInfo(final Activity activity){
        new Thread(new Runnable() {
            @Override
            public void run() {
                final User user = User.getInfo(session);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        lastFmEventListener.userInfoLoaded(user);
                    }
                });

            }
        }).start();
    }

    public int getTracksPagesCount(){
        return tracksPagesCount;
    }

    public int getCurrentTracksPage(){
        return currentTracksPage;
    }

    public class TracksTask extends AsyncTask<Void, Void, ArrayList<Track>> {
        private TASK task;
        private String artist;
        private String track;
        public TracksTask(TASK task){
            this.task = task;
        }

        public TracksTask(TASK task, String track, String artist){
            this.task = task;
            this.artist = artist;
            this.track = track;
        }

        @Override
        protected ArrayList<Track> doInBackground(Void... voids) {
            ArrayList<Track> tracks = null;
            PaginatedResult<Track> result = null;
            Chart<Track> chart;
            switch (task){
                case RECENT_TRACKS:
                    result = User.getRecentTracks(user, currentTracksPage, 20, key);
                    tracksPagesCount = result.getTotalPages();
                    break;
                case LOVED_TRACKS:
                    result = User.getLovedTracks(user, currentTracksPage, key);
                    tracksPagesCount = result.getTotalPages();
                    break;
                case TOP_TRACKS:
                    tracks = new ArrayList<>(User.getTopTracks(user, key));
                    tracksPagesCount = 1;
                    break;
                case WEEKLY_TRACK_CHART:
                    chart = User.getWeeklyTrackChart(user, 50, key);
                    tracks = new ArrayList<>(chart.getEntries());
                    break;
                case ARTISTS_TRACKS:
                    tracks = new ArrayList<>(Artist.getTopTracks(artist, key));
                    break;
                case SIMILAR_TRACKS:
                    try {
                        tracks = (ArrayList<Track>) Track.getSimilar(artist, track, key);
                        tracksPagesCount = 1;
                    } catch (ClassCastException ignored){}
            }
            if (result != null && !result.isEmpty()) {
                tracks = new ArrayList<>(result.getPageResults());
            }
            return tracks;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = new ProgressDialog(context, R.style.DialogStyle);
            progress.setTitle(context.getString(R.string.please_wait));
            progress.setMessage(context.getString(R.string.receiving_information));
            progress.setCancelable(false);
            progress.show();
        }

        @Override
        protected void onPostExecute(ArrayList<Track> v) {
            super.onPostExecute(v);
            progress.dismiss();
            lastFmEventListener.tracksLoaded(v);
        }
    }

    public class ArtistsTask extends AsyncTask<Void, Void, ArrayList<Artist>> {
        private TASK task;
        public ArtistsTask(TASK task){
            this.task = task;
        }

        @Override
        protected ArrayList<Artist> doInBackground(Void... voids) {
            ArrayList<Artist> artists = null;
            Chart<Artist> chart;
            switch (task) {
                case WEEKLY_ARTIST_CHART:
                    chart = User.getWeeklyArtistChart(user, 50, key);
                    artists = new ArrayList<>(chart.getEntries());
                    break;

                case TOP_ARTISTS:
                    artists = new ArrayList<>(User.getTopArtists(user, key));
                    break;
            }
            return artists;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = new ProgressDialog(context, R.style.DialogStyle);
            progress.setTitle(context.getString(R.string.please_wait));
            progress.setMessage(context.getString(R.string.receiving_information));
            progress.setCancelable(false);
            progress.show();
        }

        @Override
        protected void onPostExecute(ArrayList<Artist> v) {
            super.onPostExecute(v);
            progress.dismiss();
            lastFmEventListener.artistsLoaded(v);
        }
    }

    public void logOut(){
        settings.savePreference(Settings.APP_PREFERENCES_USERNAME, "");
        settings.savePreference(Settings.APP_PREFERENCES_PASSWORD, "");
        settings.savePreference(Settings.APP_PREFERENCES_REMEMBER_PASS, false);
        Settings.REMEMBER_PASS = false;
        Settings.PASSWORD = "";
        Settings.USERNAME = "";
        session = null;
        isAuth = false;
    }

    public class LoveAndScrobbleTask extends AsyncTask<Void, Void, Void> {
        private TASK task;
        private String artist, track;

        public LoveAndScrobbleTask(TASK task, String artist, String track){
            this.task = task;
            this.artist = artist;
            this.track = track;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            switch (task) {
                case LOVE:
                    Track.love(artist, track, session);
                    break;
                case SCROBBLE:
                    int now = (int) (System.currentTimeMillis() / 1000);
                    Track.scrobble(artist, track, now, session);
                    break;
                case NOW_PLAYING:
                    Track.updateNowPlaying(artist, track, session);
                    break;
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
        }
    }
}
