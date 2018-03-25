package com.merseyside.admin.player.MegamixLibrary;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.merseyside.admin.player.AdaptersAndItems.Track;
import com.merseyside.admin.player.AdaptersAndItems.Transition;
import com.merseyside.admin.player.AdaptersAndItems.TransitionCreator;
import com.merseyside.admin.player.Dialogs.AddToPlaylistDialog;
import com.merseyside.admin.player.Dialogs.InfoDialog;
import com.merseyside.admin.player.Dialogs.TransitionsDialog;
import com.merseyside.admin.player.MegamixLibrary.MarkerView;
import com.merseyside.admin.player.MegamixLibrary.SamplePlayer;
import com.merseyside.admin.player.MegamixLibrary.WaveformView;
import com.merseyside.admin.player.MegamixLibrary.soundfile.SoundFile;
import com.merseyside.admin.player.R;
import com.merseyside.admin.player.Utilities.DBHelper;
import com.merseyside.admin.player.Utilities.FileManager;
import com.merseyside.admin.player.Utilities.FirebaseEngine;
import com.merseyside.admin.player.Utilities.M3UParser;
import com.merseyside.admin.player.Utilities.MySnackbar;
import com.merseyside.admin.player.Utilities.Point;
import com.merseyside.admin.player.Utilities.PrintString;
import com.merseyside.admin.player.Utilities.Settings;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Created by Admin on 30.01.2017.
 */

public class MegamixCreator extends android.support.v4.app.Fragment implements View.OnClickListener, MarkerView.MarkerListener,
        WaveformView.WaveformListener{
    private AlertDialog mAlertDialog;
    private ProgressDialog mProgressDialog;
    private SoundFile mSoundFile, mNextSoundFile, mPrevSoundFile, mCurrentSoundFile;
    private File mFile;
    private String mFilename;
    private WaveformView mWaveformView;
    private MarkerView mStartMarker, mEndMarker;
    private TextView mInfo;
    private String mInfoContent;
    private String mCaption = "";
    private int mWidth, mMaxPos, mStartPos, mEndPos;
    private int mLastDisplayedEndPos, mOffset, mOffsetGoal,mFlingVelocity, mPlayStartMsec, mPlayEndMsec, mLastDisplayedStartPos;
    private Handler mHandler;
    private boolean mIsPlaying;
    private SamplePlayer mPlayer;
    private boolean mTouchDragging, mKeyDown, mStartVisible, mEndVisible,  mLoadingKeepGoing;
    private long mWaveformTouchStartMsec, mLoadingLastUpdateTime;
    private float mDensity, mTouchStart;
    private int mMarkerLeftInset, mMarkerRightInset, mMarkerTopOffset, mMarkerBottomOffset, mTouchInitialOffset, mTouchInitialStartPos, mTouchInitialEndPos;
    private Thread mLoadSoundFileThread, mRecordAudioThread, mSaveSoundFileThread;
    private DownloadFileTask asyncTaskNext, asyncTaskPrev;
    private int currentPosition, prevTrackCrossfade, nextTrackIncrease;
    private Settings settings;
    private String tableName, itemName;
    private int crossfade, fading, increase, transition, transition_duration;

    private static final int CROSSFADE_MAX_LENGHT = Settings.CROSSFADE_AND_INCREASE_DURATION;
    private static final int INCREASE_MAX_LENGHT = Settings.CROSSFADE_AND_INCREASE_DURATION;

    private TextView name_text, artist_text, comment_text, mStartText, mEndText;
    private CheckBox crossfade_cb, fading_cb, increase_cb, transition_cb;
    private ImageButton  mPlayButton, mRewindButton, mFfwdButton, nextTrack, prevTrack, info;
    private Button save, save_to_playlist, save_original;
    private ImageView cover;

    private FileManager manager;

    private Bitmap default_cover;

    private Track track;
    private ArrayList<Track> tracks;

    private MyTask task;

    private Animation animBlink;

    public static boolean CROSSFADE_MODE = false;
    public static boolean FADING_MODE = false;
    public static boolean INCREASE_MODE = false;
    public static boolean FILLING_MODE = false;
    public static boolean TRANSITION_MODE = false;

    private final String TRACKSARRAY_KEY = "tracks_array";
    private final String POSITION_KEY = "position";
    private final String TABLENAME_KEY = "tablename";
    private final String ITEMNAME_KEY = "itemname";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.megamix_creator, null);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onDestroyCreator();
        PrintString.printLog("lifeCycle", "Megamix onCreate");
        manager = new FileManager(getActivity());
        settings = new Settings(getActivity());
        default_cover = BitmapFactory.decodeResource(getResources(), settings.getAttributeId(settings.getThemeByString(), R.attr.theme_dependent_track_cover));
    }

    public void setTracks(ArrayList<Track> tracks, int position, String table, String name){
        this.tracks = tracks;
        this.currentPosition = position;
        tableName = table;
        itemName = name;
    }

    private void switchOnCrossfadeMode(){
        if (!FILLING_MODE) {
            CROSSFADE_MODE = true;
            if (FADING_MODE) {
                FADING_MODE = false;
                fading_cb.setChecked(false);
            } else if (INCREASE_MODE) {
                INCREASE_MODE = false;
                increase_cb.setChecked(false);
            } else if (TRANSITION_MODE){
                TRANSITION_MODE = false;
                transition_cb.setChecked(false);
            }
            mInfo.setText(getResources().getString(R.string.choose_crossfade));
            mInfo.startAnimation(animBlink);
        }
    }

    private void switchOffCrossfadeMode(){
        fading_cb.setChecked(false);
        if (!INCREASE_MODE && !FADING_MODE && !TRANSITION_MODE) mInfo.setText(mCaption);
        CROSSFADE_MODE = false;
        mInfo.clearAnimation();
    }

    private void switchOnFadingMode(){
        if (!FILLING_MODE) {
            FADING_MODE = true;
            if (CROSSFADE_MODE) {
                CROSSFADE_MODE = false;
                crossfade_cb.setChecked(false);
            } else if (INCREASE_MODE) {
                INCREASE_MODE = false;
                increase_cb.setChecked(false);
            } else if (TRANSITION_MODE){
                TRANSITION_MODE = false;
                transition_cb.setChecked(false);
            }
            mInfo.setText(getResources().getString(R.string.choose_fading));
            mInfo.startAnimation(animBlink);
        }
    }

    private void switchOffFadingMode(){
        FADING_MODE = false;
        if (!CROSSFADE_MODE && !INCREASE_MODE && !TRANSITION_MODE) mInfo.setText(mCaption);
        mInfo.clearAnimation();
    }

    private void switchOnIncreaseMode(){
        if (!FILLING_MODE) {
            INCREASE_MODE = true;
            if (FADING_MODE) {
                FADING_MODE = false;
                fading_cb.setChecked(false);
            } else if (CROSSFADE_MODE) {
                CROSSFADE_MODE = false;
                crossfade_cb.setChecked(false);
            } else if (TRANSITION_MODE){
                TRANSITION_MODE = false;
                transition_cb.setChecked(false);
            }
            mInfo.setText(getResources().getString(R.string.choose_increase));
            mInfo.startAnimation(animBlink);
        }
    }

    private void switchOffIncreaseMode(){
        INCREASE_MODE = false;
        if (!CROSSFADE_MODE && !FADING_MODE && !TRANSITION_MODE) mInfo.setText(mCaption);
        mInfo.clearAnimation();
    }

    private void switchOffTransitionMode(){
        TRANSITION_MODE = false;
        if (!CROSSFADE_MODE && !FADING_MODE && !INCREASE_MODE) mInfo.setText(mCaption);
        mInfo.clearAnimation();
    }

    private void switchOnTransitionMode(){
        if (!FILLING_MODE) {
            TRANSITION_MODE = true;
            if (FADING_MODE) {
                FADING_MODE = false;
                fading_cb.setChecked(false);
            } else if (CROSSFADE_MODE) {
                CROSSFADE_MODE = false;
                crossfade_cb.setChecked(false);
            } else if (INCREASE_MODE){
                INCREASE_MODE = false;
                increase_cb.setChecked(false);
            }
            mInfo.setText(getResources().getString(R.string.choose_transition_duration));
            mInfo.startAnimation(animBlink);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        PrintString.printLog("lifeCycle", "onSaveInstance");
        outState.putParcelableArrayList(TRACKSARRAY_KEY, tracks);
        outState.putInt(POSITION_KEY, currentPosition);
        outState.putString(TABLENAME_KEY, tableName);
        outState.putString(ITEMNAME_KEY, itemName);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        PrintString.printLog("lifeCycle", "onActivityCreated");
        if (savedInstanceState != null){
            if (tracks == null || tracks.size()==0){
                tracks = savedInstanceState.getParcelableArrayList(TRACKSARRAY_KEY);
                currentPosition = savedInstanceState.getInt(POSITION_KEY);
                tableName = savedInstanceState.getString(TABLENAME_KEY);
                itemName = savedInstanceState.getString(ITEMNAME_KEY);
            }
        }

        if (Settings.SHOW_MEGAMIX_WARNING_DIALOG) {
            ArrayList<String> warning_message = new ArrayList<>();
            warning_message.add(getResources().getString(R.string.megamix_warning));
            InfoDialog dialog = new InfoDialog(getActivity(), getResources().getString(R.string.warning), warning_message, true);
            dialog.setInfoDialogListener(new InfoDialog.InfoDialogListener() {
                @Override
                public void checkboxClicked(boolean isChecked) {
                    if (isChecked) Settings.SHOW_MEGAMIX_WARNING_DIALOG = false;
                    else Settings.SHOW_MEGAMIX_WARNING_DIALOG = true;
                    settings.savePreference(Settings.APP_PREFERENCES_SHOW_MEGAMIX_WARNING_DIALOG, !isChecked);

                }
            });
            dialog.show();
        }

        name_text = (TextView) getView().findViewById(R.id.info_title);
        artist_text = (TextView) getView().findViewById(R.id.info_artist);
        comment_text = (TextView) getView().findViewById(R.id.info_comment);

        animBlink = AnimationUtils.loadAnimation(getActivity(), R.anim.blink_animation);

        crossfade_cb = (CheckBox) getView().findViewById(R.id.crossfade_cb);
        crossfade_cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    switchOnCrossfadeMode();
                    crossfade_cb.setBackground(settings.getTintedDrawable(getResources(), R.drawable.crossfade, R.color.white));
                }
                else{
                    switchOffCrossfadeMode();
                    mWaveformView.setCrossfade(0);
                    crossfade = 0;
                    updateDisplay();
                    fading_cb.setEnabled(false);
                    crossfade_cb.setBackground(settings.getTintedDrawable(getResources(), R.drawable.crossfade, R.color.grey));
                    fading_cb.setBackground(settings.getTintedDrawable(getResources(), R.drawable.fading, R.color.grey));
                }
            }
        });


        fading_cb = (CheckBox) getView().findViewById(R.id.fading_cb);
        fading_cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked){
                    switchOnFadingMode();
                    fading_cb.setBackground(settings.getTintedDrawable(getResources(), R.drawable.fading, R.color.white));
                } else {
                    switchOffFadingMode();
                    fading = 0;
                    mWaveformView.setFading(0);
                    updateDisplay();
                    fading_cb.setBackground(settings.getTintedDrawable(getResources(), R.drawable.fading, R.color.grey));
                }
            }
        });

        increase_cb = (CheckBox) getView().findViewById(R.id.increase_cb);
        increase_cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked){
                    switchOnIncreaseMode();
                    increase_cb.setBackground(settings.getTintedDrawable(getResources(), R.drawable.increase, R.color.white));
                } else {
                    switchOffIncreaseMode();
                    increase = 0;
                    mWaveformView.setIncrease(0);
                    updateDisplay();
                    increase_cb.setBackground(settings.getTintedDrawable(getResources(), R.drawable.increase, R.color.grey));
                }
            }
        });

        transition_cb = (CheckBox) getView().findViewById(R.id.transition_cb);
        transition_cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked){
                    switchOnTransitionMode();
                    transition_cb.setBackground(settings.getTintedDrawable(getResources(), R.drawable.transition, R.color.white));
                    if (!FILLING_MODE) {
                        TransitionsDialog transitionsDialog = new TransitionsDialog(getActivity(), new TransitionsDialog.MyTransitionDialogListener() {
                            @Override
                            public void userSelectedTransition(int t) {
                                transition = t;
                            }

                            @Override
                            public void userDoNotChooseAnything() {
                                transition_cb.setChecked(false);
                                transition = 0;
                                switchOffTransitionMode();
                            }
                        });
                        transitionsDialog.setCancelable(false);
                        transitionsDialog.show();
                    }
                } else {
                    switchOffTransitionMode();
                    transition_cb.setBackground(settings.getTintedDrawable(getResources(), R.drawable.transition, R.color.grey));
                    transition = 0;
                    mWaveformView.setTransition(0);
                    updateDisplay();
                }
            }
        });

        save = (Button) getView().findViewById(R.id.save);
        save.setOnClickListener(this);
        save_to_playlist = (Button) getView().findViewById(R.id.save_to_playlist);
        save_to_playlist.setOnClickListener(this);
        save_original = (Button) getView().findViewById(R.id.save_original);
        save_original.setOnClickListener(this);

        cover = (ImageView) getView().findViewById(R.id.info_cover);
        nextTrack = (ImageButton) getView().findViewById(R.id.next_track);
        nextTrack.setOnClickListener(this);
        prevTrack = (ImageButton) getView().findViewById(R.id.prev_track);
        prevTrack.setOnClickListener(this);
        info = (ImageButton) getView().findViewById(R.id.megamix_info);
        info.setOnClickListener(this);
        fill(tracks.get(currentPosition));
    }

    private void fill(Track track){

        FILLING_MODE = true;
        this.track = track;
        if (task!= null) task.cancel(true);
        task = new MyTask(cover);
        task.execute(track);
        stopAllTasks();

        if (currentPosition-1>=0){
            prevTrackCrossfade = tracks.get(currentPosition-1).getCrossfadeDuration();
        } else prevTrackCrossfade = 0;

        if (currentPosition+1 < tracks.size()){
            nextTrackIncrease = tracks.get(currentPosition+1).getIncrease();
        } else nextTrackIncrease = 0;

        onCreateCreator();
        cover.setImageBitmap(default_cover);
        name_text.setText(track.getName());
        artist_text.setText(track.getArtist());
        comment_text.setText(getResources().getString(R.string.comment_dialog) + ": " + track.getComment().replace("null", ""));


        if (track.isMegamixTrack()) {
            if (track.getCrossfadeDuration() > 0) {
                crossfade_cb.setChecked(true);
                crossfade = track.getCrossfadeDuration();
                fading_cb.setEnabled(true);

                if (track.getFading() > 0) {
                    fading_cb.setChecked(true);
                    fading = track.getFading();
                } else fading =0;

            } else {
                crossfade_cb.setChecked(false);
                crossfade = 0;
            }
            if (track.getIncrease() > 0) {
                increase_cb.setChecked(true);
                increase = track.getIncrease();
            } else {
                increase_cb.setChecked(false);
                increase = 0;
            }
            if (track.getTransition() != 0){
                transition = track.getTransition();
                transition_duration = track.getTransit_duration();
                transition_cb.setChecked(true);
            } else {
                transition = 0;
                transition_duration = 0;
                transition_cb.setChecked(false);
            }
        } else {
            crossfade = 0;
            fading = 0;
            increase = 0;
            transition = 0;
            transition_duration = 0;
            fading_cb.setEnabled(false);
            fading_cb.setChecked(false);
            crossfade_cb.setChecked(false);
            increase_cb.setChecked(false);
            transition_cb.setChecked(false);
        }
        FILLING_MODE = false;

        HashMap<String, String> map = new HashMap<>();
        map.put("name", track.getName());
        map.put("artist", track.getArtist());
        FirebaseEngine.logEvent(getActivity(), "MEGAMIX", map);
    }

    private class MyTask extends AsyncTask<Track, Void, Bitmap> {

        private ImageView view;
        private int width=0, height=0;
        MyTask(ImageView view){
            this.view = view;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            PrintString.printLog("MegamixCreator", "W = " + view.getWidth() + "H = " + view.getHeight());
        }

        @Override
        protected Bitmap doInBackground(Track... sqlItemses) {

            for (Track o : sqlItemses) {
                MediaMetadataRetriever retriver = new MediaMetadataRetriever();
                retriver.setDataSource(o.getPath());
                try {
                    byte[] data = retriver.getEmbeddedPicture();
                    if (width == 0 || height ==0){
                        return Settings.decodeSampledBitmapFromData(data, (300), (300), data.length);
                    } else {
                        return Settings.decodeSampledBitmapFromData(data, width, height, data.length);
                    }
                } catch (NullPointerException e){
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (bitmap != null)
            view.setImageBitmap(bitmap);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }



    @Override
    public void onPause() {
        super.onPause();
        PrintString.printLog("MegamixCreator", "onPause");
        settings.freeMemory();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopAllTasks();
        handlePause();
        PrintString.printLog("MegamixCreator", "onStop");
    }

    private void onButtonClicked(final int id){
        switch (id) {
            case R.id.save_original:
                saveOriginal();
                manager.setPlaylistAndFolder(tableName, itemName, tracks);
                new MySnackbar(getActivity(), mPlayButton, R.string.successfully_saved).show();
                if (currentPosition != tracks.size()-1) {
                    currentPosition++;
                    if (tracks.get(currentPosition).getType() == Track.INTERNET_TRACK){
                        onButtonClicked(id);
                        return;
                    } else {
                        loadNextPrevTrack(true);
                        fill(tracks.get(currentPosition));
                    }
                }
                else new MySnackbar(getActivity(), mPlayButton, R.string.no_more_tracks, true).show();

                break;
            case R.id.save: {
                if (applySettingsToTrack()) {
                    manager.setPlaylistAndFolder(tableName, itemName, tracks);
                    new MySnackbar(getActivity(), mPlayButton, R.string.successfully_saved).show();
                    if (currentPosition != tracks.size()-1) {
                        currentPosition++;
                        if (tracks.get(currentPosition).getType() == Track.INTERNET_TRACK){
                            onButtonClicked(id);
                            return;
                        } else {
                            loadNextPrevTrack(true);
                            fill(tracks.get(currentPosition));
                        }
                    }
                    else new MySnackbar(getActivity(), mPlayButton, R.string.no_more_tracks, true).show();
                }
                break;
            }
            case R.id.save_to_playlist: {
                if (manager.getCountOfItems(DBHelper.TABLE_PLAYLIST_NAME) != 0) {
                    AddToPlaylistDialog dialog = new AddToPlaylistDialog(getActivity(), new AddToPlaylistDialog.MyAddToPlaylistDialogListener() {
                        @Override
                        public void userSelectedPlaylist(String name, String url) {
                            if (applySettingsToTrack()) {
                                File file = new File(url);
                                if (file.exists()) {
                                    M3UParser parser = new M3UParser(file, getActivity());
                                    ArrayList<Track> tracks1 = new ArrayList<>();
                                    tracks1.add(track);
                                    if (!parser.addTracksToPlaylist(tracks1)) {
                                        new MySnackbar(getActivity(), mPlayButton, R.string.cant_write).show();
                                        return;
                                    } else new MySnackbar(getActivity(), mPlayButton, R.string.successfully_added).show();
                                    if (currentPosition != tracks.size() - 1) {
                                        currentPosition++;
                                        if (tracks.get(currentPosition).getType() == Track.INTERNET_TRACK) {
                                            onButtonClicked(id);
                                            return;
                                        } else {
                                            loadNextPrevTrack(true);
                                            fill(tracks.get(currentPosition));
                                        }
                                    } else new MySnackbar(getActivity(), mPlayButton, R.string.no_more_tracks, true).show();
                                }
                            }
                        }
                    });
                    dialog.show();
                    break;
                } else new MySnackbar(getActivity(), mPlayButton, R.string.playlists_not_found, true).show();
            }
            case R.id.next_track:{
                if (currentPosition != tracks.size() - 1) {
                    if (mSoundFile == null || !Settings.LOAD_NEXT_TRACK || asyncTaskNext == null || (Settings.LOAD_NEXT_TRACK && mNextSoundFile!=null)) {
                        currentPosition++;
                        if (tracks.get(currentPosition).getType() == Track.INTERNET_TRACK) {
                            onButtonClicked(id);
                            return;
                        } else {
                            loadNextPrevTrack(true);
                            fill(tracks.get(currentPosition));
                        }
                    } else new MySnackbar(getActivity(), mPlayButton, R.string.megamix_please_wait, true).show();
                } else new MySnackbar(getActivity(), mPlayButton, R.string.no_more_tracks, true).show();
                break;
            }

            case R.id.prev_track:{
                if (currentPosition > 0) {
                    if (mSoundFile == null || !Settings.LOAD_PREV_TRACK || asyncTaskPrev == null || (Settings.LOAD_PREV_TRACK && mPrevSoundFile!=null)) {
                        currentPosition--;
                        if (tracks.get(currentPosition).getType() == Track.INTERNET_TRACK){
                            onButtonClicked(id);
                            return;
                        } else {
                            loadNextPrevTrack(false);
                            fill(tracks.get(currentPosition));
                        }

                    } else new MySnackbar(getActivity(), mPlayButton, R.string.megamix_please_wait, true).show();
                } else new MySnackbar(getActivity(), mPlayButton, R.string.no_more_tracks, true).show();
                break;
            }

            case R.id.megamix_info:{
                ArrayList<String> strs = new ArrayList<>();
                strs.add(getString(R.string.megamix_info_information));
                InfoDialog info = new InfoDialog(getActivity(), getString(R.string.megamix_info_title), strs);
                info.show();
                break;
            }
        }
    }

    private void loadNextPrevTrack(boolean isNext){
        try {
            stopAllTasks();
            if (isNext) {
                if (Settings.LOAD_PREV_TRACK) mPrevSoundFile = mCurrentSoundFile;
                if (Settings.LOAD_NEXT_TRACK) {
                    mCurrentSoundFile = mNextSoundFile == null ? null : mNextSoundFile;
                    mNextSoundFile = null;
                }
            } else {
                if (Settings.LOAD_NEXT_TRACK) {
                    mNextSoundFile = mCurrentSoundFile;
                }
                if (Settings.LOAD_PREV_TRACK){
                    mCurrentSoundFile = mPrevSoundFile == null ? null : mPrevSoundFile;
                    mPrevSoundFile = null;
                }
            }
        } catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    private void stopAllTasks(){
        if (asyncTaskPrev!= null){
            asyncTaskPrev.cancel(true);
            asyncTaskPrev= null;
        }
        if (asyncTaskNext!= null){
            asyncTaskNext.cancel(true);
            asyncTaskNext= null;
        }
    }

    @Override
    public void onClick(View view) {
        onButtonClicked(view.getId());
    }

    private boolean applySettingsToTrack(){
        try {
            int start_point = Float.valueOf(mStartText.getText().toString()).intValue() * 1000;
            int end_point = Float.valueOf(mEndText.getText().toString()).intValue() * 1000;

            if (start_point!=0 || end_point != Integer.valueOf(Settings.getMetadata(track.getPath(), MediaMetadataRetriever.METADATA_KEY_DURATION))){
                if (end_point - start_point > Settings.SILENCE_END_DURATION) {
                    track.setMegamixTrack(true);
                    if (crossfade_cb.isChecked()) {
                        track.setCrossfadeDuration(crossfade);
                    } else track.setCrossfadeDuration(0);
                    if (increase_cb.isChecked()) {
                        track.setIncrease(increase);
                    } else track.setIncrease(0);
                    track.setFading(fading);
                    track.set_start_point(start_point);
                    track.set_end_point(end_point);
                    if (transition_duration != 0) {
                        track.setTransition(transition);
                        track.setTransit_duration(transition_duration);
                    } else {
                        track.setTransition(0);
                        track.setTransit_duration(0);
                    }
                    tracks.set(currentPosition, track);
                    return true;
                } else new MySnackbar(getActivity(), mPlayButton, R.string.total_duration_not_match,  true).show();
            }
            else new MySnackbar(getActivity(), mPlayButton, R.string.track_not_changed,  true).show();
            return false;
        } catch (NumberFormatException ignored){
            return false;
        }
    }

    private void saveOriginal(){
        track.setMegamixTrack(false);
        track.setFading(0);
        track.setCrossfadeDuration(0);
        track.setIncrease(0);
        track.set_start_point(0);
        track.set_end_point(0);
        track.setTransition(0);
        track.setTransit_duration(0);
        tracks.set(currentPosition, track);
    }

    private void onCreateCreator(){
        if (track.getType() != Track.MEMORY_TRACK) {
            new MySnackbar(getActivity(), mPlayButton, R.string.only_memory_tracks, true).show();
            return;
        }
        mIsPlaying = false;

        mAlertDialog = null;
        mProgressDialog = null;

        mLoadSoundFileThread = null;
        mRecordAudioThread = null;
        mSaveSoundFileThread = null;

        mFilename = track.getPath();
        if (mPlayer!= null){
            if (mPlayer.isPlaying()) mPlayer.stop();
            mPlayer = null;
        }

        mSoundFile = null;
        mKeyDown = false;

        mHandler = new Handler();

        loadGui();

        mHandler.postDelayed(mTimerRunnable, 100);

        if (!mFilename.equals("record")) {
            loadFromFile();
        }
    }

    private void closeThread(Thread thread) {
        if (thread != null && thread.isAlive()) {
            try {
                thread.join();
            } catch (InterruptedException e) {
            }
        }
    }

    @Override
    public void onDestroy() {
        onDestroyCreator();
        super.onDestroy();
    }

    private void setMegamixSettings(){
        if (mWaveformView != null) {
            mWaveformView.setCrossfade(mWaveformView.millisecsToPixels(track.getCrossfadeDuration()));
            mWaveformView.setFading(mWaveformView.millisecsToPixels(track.getFading()));
            mWaveformView.setIncrease(mWaveformView.millisecsToPixels(track.getIncrease()));
            mWaveformView.setTransition(mWaveformView.millisecsToPixels(track.getTransit_duration()));
            mWaveformView.setPrevTrackCrossfade(mWaveformView.millisecsToPixels(prevTrackCrossfade));
            mWaveformView.setNextTrackCrossfade(mWaveformView.millisecsToPixels(nextTrackIncrease));
        }
    }

    protected void onDestroyCreator() {
        PrintString.printLog("lifeCycle", "onDestroy megamix");
        mPrevSoundFile = null;
        mSoundFile = null;
        mNextSoundFile = null;
        mCurrentSoundFile = null;
        mLoadingKeepGoing = false;
        closeThread(mLoadSoundFileThread);
        closeThread(mRecordAudioThread);
        closeThread(mSaveSoundFileThread);
        mLoadSoundFileThread = null;
        mRecordAudioThread = null;
        mSaveSoundFileThread = null;
        if(mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
        if(mAlertDialog != null) {
            mAlertDialog.dismiss();
            mAlertDialog = null;
        }
        stopAllTasks();
        increase = 0;
        crossfade = 0;
        transition = 0;
        transition_duration = 0;
        if (mPlayer != null) {
            if (mPlayer.isPlaying() || mPlayer.isPaused()) {
                mPlayer.stop();
            }
            mPlayer.release();
            mPlayer = null;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        final int saveZoomLevel = mWaveformView.getZoomLevel();
        super.onConfigurationChanged(newConfig);

        loadGui();

        mHandler.postDelayed(new Runnable() {
            public void run() {
                mStartMarker.requestFocus();
                markerFocus(mStartMarker);

                mWaveformView.setZoomLevel(saveZoomLevel);
                mWaveformView.recomputeHeights(mDensity);

                updateDisplay();
            }
        }, 500);
    }


    public void waveformDraw() {
        mWidth = mWaveformView.getMeasuredWidth();
        if (mOffsetGoal != mOffset && !mKeyDown)
            updateDisplay();
        else if (mIsPlaying) {
            updateDisplay();
        } else if (mFlingVelocity != 0) {
            updateDisplay();
        }
    }

    public void waveformTouchStart(float x) {
        mTouchDragging = true;
        mTouchStart = x;
        mTouchInitialOffset = mOffset;
        mFlingVelocity = 0;
        mWaveformTouchStartMsec = getCurrentTime();
    }

    public void waveformTouchMove(float x) {
        mOffset = trap((int)(mTouchInitialOffset + (mTouchStart - x)));
        updateDisplay();
    }

    public void waveformTouchEnd() {
        if (mSoundFile!= null) {
            mTouchDragging = false;
            mOffsetGoal = mOffset;
            try {
                int start_pos = mWaveformView.pixelsToMillisecs(mStartPos);
                int end_pos = mWaveformView.pixelsToMillisecs(mEndPos);

            PrintString.printLog("MegamixCreator", "MaxPos " + start_pos);
            PrintString.printLog("MegamixCreator", "MaxPos " + end_pos);
            long elapsedMsec = getCurrentTime() - mWaveformTouchStartMsec;
            int seekMsec = mWaveformView.pixelsToMillisecs((int) (mTouchStart + mOffset));
            PrintString.printLog("MegamixCreator", "elapsed " + elapsedMsec);

            if (CROSSFADE_MODE) {
                if (seekMsec > start_pos && seekMsec < end_pos) {
                    int crossfade = end_pos - seekMsec;
                    if (crossfade > CROSSFADE_MAX_LENGHT)crossfade = CROSSFADE_MAX_LENGHT;
                    if (end_pos - crossfade < start_pos + increase) return;
                    else this.crossfade = crossfade;

                    mWaveformView.setCrossfade(mWaveformView.millisecsToPixels(crossfade));
                    switchOffCrossfadeMode();
                    fading_cb.setEnabled(true);
                    updateDisplay();
                } else
                    new MySnackbar(getActivity(), mPlayButton, R.string.crossfade_does_not_match, true).show();
            } else if (FADING_MODE) {
                if (seekMsec < end_pos) {
                    int fading;
                    if (seekMsec < (end_pos - this.crossfade)) {
                        fading = this.crossfade;
                    } else fading = end_pos - seekMsec;
                    this.fading = fading;
                    mWaveformView.setFading(mWaveformView.millisecsToPixels(fading));
                    switchOffFadingMode();
                    updateDisplay();
                } else if (seekMsec >= end_pos) {
                    new MySnackbar(getActivity(), mPlayButton, R.string.fading_does_not_match, true).show();
                }
            } else if (INCREASE_MODE) {
                if (seekMsec > start_pos && seekMsec < end_pos) {
                    int increase = seekMsec - start_pos;
                    if (increase > INCREASE_MAX_LENGHT) increase = INCREASE_MAX_LENGHT;

                    if (start_pos + increase > end_pos - this.crossfade) return;
                    else this.increase = increase;
                    mWaveformView.setIncrease(mWaveformView.millisecsToPixels(increase));
                    switchOffIncreaseMode();
                    updateDisplay();
                } else {
                    new MySnackbar(getActivity(), mPlayButton, R.string.increase_does_not_match, true).show();
                }
            } else if (TRANSITION_MODE){
                if (seekMsec > start_pos && seekMsec < end_pos) {
                    int transition_duration = end_pos - seekMsec;
                    Transition transition = TransitionCreator.CreateTransition(this.transition, getActivity());
                    if (transition_duration > transition.getTurningPoint()) transition_duration = transition.getTurningPoint();

                    if (start_pos + increase > end_pos - transition_duration) return;
                    else this.transition_duration = transition_duration;
                    mWaveformView.setTransition(mWaveformView.millisecsToPixels(transition_duration));
                    switchOffTransitionMode();
                    updateDisplay();
                } else {
                    new MySnackbar(getActivity(), mPlayButton, R.string.transition_duration_does_not_match, true).show();
                }
            } else if (elapsedMsec < 300) {
                if (mIsPlaying) {
                    seekMsec = mWaveformView.pixelsToMillisecs(
                            (int) (mTouchStart + mOffset));
                    if (seekMsec >= mPlayStartMsec &&
                            seekMsec < mPlayEndMsec) {
                        mPlayer.seekTo(seekMsec);
                        PrintString.printLog("MegamixCreator", "seekTo");
                    } else {
                        PrintString.printLog("MegamixCreator", "pause");
                        handlePause();
                    }
                } else {
                    onPlay((int) (mTouchStart + mOffset));
                }
            }
            } catch (NullPointerException ignored){
                return;
            }
        }
    }

    public void waveformFling(float vx) {
        mTouchDragging = false;
        mOffsetGoal = mOffset;
        mFlingVelocity = (int)(-vx);
        updateDisplay();
    }

    public void waveformZoomIn() {
        mWaveformView.zoomIn();
        mStartPos = mWaveformView.getStart();
        mEndPos = mWaveformView.getEnd();
        mMaxPos = mWaveformView.maxPos();
        mOffset = mWaveformView.getOffset();
        mOffsetGoal = mOffset;
        updateDisplay();
    }

    public void waveformZoomOut() {
        mWaveformView.zoomOut();
        mStartPos = mWaveformView.getStart();
        mEndPos = mWaveformView.getEnd();
        mMaxPos = mWaveformView.maxPos();
        mOffset = mWaveformView.getOffset();
        mOffsetGoal = mOffset;
        updateDisplay();
    }

    public void markerDraw() {
    }

    public void markerTouchStart(MarkerView marker, float x) {
        mTouchDragging = true;
        mTouchStart = x;
        mTouchInitialStartPos = mStartPos;
        mTouchInitialEndPos = mEndPos;
    }

    public void markerTouchMove(MarkerView marker, float x) {
        float delta = x - mTouchStart;

        if (marker == mStartMarker) {
            mStartPos = trap((int)(mTouchInitialStartPos + delta));
            mEndPos = trap((int)(mTouchInitialEndPos + delta));
        } else {
            mEndPos = trap((int)(mTouchInitialEndPos + delta));
            if (mEndPos < mStartPos)
                mEndPos = mStartPos;
        }
        PrintString.printLog("MegamixCreator", mStartPos + " " + mEndPos);

        updateDisplay();
    }

    public void markerTouchEnd(MarkerView marker) {

        mTouchDragging = false;
        if (marker == mStartMarker) {
            setOffsetGoalStart();
        } else {
            setOffsetGoalEnd();
        }
    }

    public void markerLeft(MarkerView marker, int velocity) {
        mKeyDown = true;

        if (marker == mStartMarker) {
            int saveStart = mStartPos;
            mStartPos = trap(mStartPos - velocity);
            mEndPos = trap(mEndPos - (saveStart - mStartPos));
            setOffsetGoalStart();
        }

        if (marker == mEndMarker) {
            if (mEndPos == mStartPos) {
                mStartPos = trap(mStartPos - velocity);
                mEndPos = mStartPos;
            } else {
                mEndPos = trap(mEndPos - velocity);
            }

            setOffsetGoalEnd();
        }

        updateDisplay();
    }

    public void markerRight(MarkerView marker, int velocity) {
        mKeyDown = true;

        if (marker == mStartMarker) {
            int saveStart = mStartPos;
            mStartPos += velocity;
            if (mStartPos > mMaxPos)
                mStartPos = mMaxPos;
            mEndPos += (mStartPos - saveStart);
            if (mEndPos > mMaxPos)
                mEndPos = mMaxPos;

            setOffsetGoalStart();
        }

        if (marker == mEndMarker) {
            mEndPos += velocity;
            if (mEndPos > mMaxPos)
                mEndPos = mMaxPos;

            setOffsetGoalEnd();
        }

        updateDisplay();
    }

    public void markerEnter(MarkerView marker) {
    }

    public void markerKeyUp() {
        mKeyDown = false;
        updateDisplay();
    }

    public void markerFocus(MarkerView marker) {
        mKeyDown = false;
        if (marker == mStartMarker) {
            setOffsetGoalStartNoUpdate();
        } else {
            setOffsetGoalEndNoUpdate();
        }

        mHandler.postDelayed(new Runnable() {
            public void run() {
                updateDisplay();
            }
        }, 100);
    }

    private void loadGui() {
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mDensity = metrics.density;

        mMarkerLeftInset = (int)(46 * mDensity);
        mMarkerRightInset = (int)(48 * mDensity);
        mMarkerTopOffset = (int)(10 * mDensity);
        mMarkerBottomOffset = (int)(10 * mDensity);

        mStartText = (TextView)getView().findViewById(R.id.starttext);
        mStartText.addTextChangedListener(mTextWatcher);
        mEndText = (TextView)getView().findViewById(R.id.endtext);
        mEndText.addTextChangedListener(mTextWatcher);

        mPlayButton = (ImageButton)getView().findViewById(R.id.play);
        mPlayButton.setOnClickListener(mPlayListener);
        mRewindButton = (ImageButton)getView().findViewById(R.id.rew);
        mRewindButton.setOnClickListener(mRewindListener);
        mFfwdButton = (ImageButton)getView().findViewById(R.id.ffwd);
        mFfwdButton.setOnClickListener(mFfwdListener);

        enableDisableButtons();

        mWaveformView = (WaveformView)getView().findViewById(R.id.waveform);
        mWaveformView.setListener(this);

        mInfo = (TextView)getView().findViewById(R.id.info);
        mInfo.setText(mCaption);

        mMaxPos = 0;
        mLastDisplayedStartPos = -1;
        mLastDisplayedEndPos = -1;

        if (mSoundFile != null && !mWaveformView.hasSoundFile()) {
            mWaveformView.setSoundFile(mSoundFile);
            mWaveformView.recomputeHeights(mDensity);
            mMaxPos = mWaveformView.maxPos();
        }

        mStartMarker = (MarkerView)getView().findViewById(R.id.startmarker);
        mStartMarker.setListener(this);
        mStartMarker.setAlpha(1f);
        mStartMarker.setFocusable(true);
        mStartMarker.setFocusableInTouchMode(true);
        mStartVisible = true;

        mEndMarker = (MarkerView)getView().findViewById(R.id.endmarker);
        mEndMarker.setListener(this);
        mEndMarker.setAlpha(1f);
        mEndMarker.setFocusable(true);
        mEndMarker.setFocusableInTouchMode(true);
        mEndVisible = true;

        updateDisplay();
    }

    private String getPhrase(){
        List<String> lines = Arrays.asList(getResources().getStringArray(R.array.tips));
        Random random = new Random();
        int rand = random.nextInt(lines.size());
        return lines.get(rand);
    }

    private void loadFromFile() {
        mFile = new File(mFilename);

        mLoadingLastUpdateTime = getCurrentTime();
        mLoadingKeepGoing = true;
        if (mCurrentSoundFile != null) mSoundFile = mCurrentSoundFile;
        if (mSoundFile == null) {
            stopAllTasks();
            mProgressDialog = new ProgressDialog(getActivity(), R.style.DialogStyle);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setTitle(R.string.tip);
            mProgressDialog.setMessage(getPhrase());
            mProgressDialog.setCancelable(true);
            mProgressDialog.setOnCancelListener( new DialogInterface.OnCancelListener() {
                                public void onCancel(DialogInterface dialog) {
                                    mLoadingKeepGoing = false;
                                    mSoundFile = null;
                                }
                            });
            mProgressDialog.show();
            TextView textView = (TextView) mProgressDialog.findViewById(android.R.id.message);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_size));
            textView.setTypeface(null, Typeface.ITALIC);

            final SoundFile.ProgressListener listener =
                    new SoundFile.ProgressListener() {
                        public boolean reportProgress(double fractionComplete) {
                            long now = getCurrentTime();
                            if (now - mLoadingLastUpdateTime > 100) {
                                mProgressDialog.setProgress(
                                        (int) (mProgressDialog.getMax() * fractionComplete));
                                mLoadingLastUpdateTime = now;
                            }
                            return mLoadingKeepGoing;
                        }
                    };

            mLoadSoundFileThread = new Thread() {
                public void run() {
                    try {
                        mSoundFile = SoundFile.create(mFile.getAbsolutePath(), listener);

                        if (mSoundFile == null) {
                            mProgressDialog.dismiss();
                            return;
                        }
                        //mPlayer = new SamplePlayer(mSoundFile);
                    } catch (final Exception e) {
                        mProgressDialog.dismiss();
                        e.printStackTrace();
                        mInfoContent = e.toString();
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                mInfo.setText(mInfoContent);
                            }
                        });
                        return;
                    }
                    mProgressDialog.dismiss();
                    if (mLoadingKeepGoing) {
                        Runnable runnable = new Runnable() {
                            public void run() {
                                finishOpeningSoundFile();
                            }
                        };
                        mHandler.post(runnable);
                    }
                }
            };
            mLoadSoundFileThread.start();
        } else {
            finishOpeningSoundFile();
        }
    }

    private void finishOpeningSoundFile() {
        mPlayer = new SamplePlayer(mSoundFile);
        mWaveformView.setSoundFile(mSoundFile);
        mWaveformView.recomputeHeights(mDensity);

        mMaxPos = mWaveformView.maxPos();
        mLastDisplayedStartPos = -1;
        mLastDisplayedEndPos = -1;

        mTouchDragging = false;

        mOffset = 0;
        mOffsetGoal = 0;
        mFlingVelocity = 0;
        resetPositions();
        if (mEndPos > mMaxPos)
            mEndPos = mMaxPos;

        mCaption =
                mSoundFile.getFiletype() + ", " +
                        mSoundFile.getSampleRate() + " Hz, " +
                        mSoundFile.getAvgBitrateKbps() + " kbps, " +
                        formatTime(mMaxPos) + " " +
                        getResources().getString(R.string.time_seconds);
        mInfo.setText(mCaption);

        if (!track.getStartPoint().equals("null")) mStartPos = mWaveformView.millisecsToPixels(Integer.valueOf(track.getStartPoint()));
        if (!track.getEndPoint().equals("null")) mEndPos = mWaveformView.millisecsToPixels(Integer.valueOf(track.getEndPoint()));

        setMegamixSettings();

        updateDisplay();
        if (mCurrentSoundFile == null) mCurrentSoundFile = mSoundFile;

        startAsyncTasks();
    }

    @Override
    public void onResume() {
        super.onResume();
        PrintString.printLog("MegamixCreator", "onResume");
        //startAsyncTasks();
    }

    private void startAsyncTasks(){
        if (Settings.LOAD_NEXT_TRACK && currentPosition < tracks.size()-1 && mNextSoundFile == null) {
            PrintString.printLog("MegamixCreator", "next");
            asyncTaskNext = new DownloadFileTask(true, currentPosition);
            asyncTaskNext.execute(tracks.get(currentPosition+1).getPath());
        }
        if (Settings.LOAD_PREV_TRACK && currentPosition > 0 && mPrevSoundFile == null){
            PrintString.printLog("MegamixCreator", "prev");
            asyncTaskPrev = new DownloadFileTask(false, currentPosition);
            asyncTaskPrev.execute(tracks.get(currentPosition-1).getPath());
        }
    }

    private synchronized void updateDisplay() {
        if (mIsPlaying) {
            int now = mPlayer.getCurrentPosition();
            int frames = mWaveformView.millisecsToPixels(now);
            mWaveformView.setPlayback(frames);
            setOffsetGoalNoUpdate(frames - mWidth / 2);
            if (now >= mPlayEndMsec) {
                handlePause();
            }
        }

        if (!mTouchDragging) {
            int offsetDelta;

            if (mFlingVelocity != 0) {
                offsetDelta = mFlingVelocity / 30;
                if (mFlingVelocity > 80) {
                    mFlingVelocity -= 80;
                } else if (mFlingVelocity < -80) {
                    mFlingVelocity += 80;
                } else {
                    mFlingVelocity = 0;
                }

                mOffset += offsetDelta;

                if (mOffset + mWidth / 2 > mMaxPos) {
                    mOffset = mMaxPos - mWidth / 2;
                    mFlingVelocity = 0;
                }
                if (mOffset < 0) {
                    mOffset = 0;
                    mFlingVelocity = 0;
                }
                mOffsetGoal = mOffset;
            } else {
                offsetDelta = mOffsetGoal - mOffset;

                if (offsetDelta > 10)
                    offsetDelta = offsetDelta / 10;
                else if (offsetDelta > 0)
                    offsetDelta = 1;
                else if (offsetDelta < -10)
                    offsetDelta = offsetDelta / 10;
                else if (offsetDelta < 0)
                    offsetDelta = -1;
                else
                    offsetDelta = 0;

                mOffset += offsetDelta;
            }
        }
        mWaveformView.setParameters(mStartPos, mEndPos, mOffset);
        mWaveformView.invalidate();

        /*mStartMarker.setContentDescription(
                getResources().getText(R.string.start_marker) + " " +
                        formatTime(mStartPos));
        mEndMarker.setContentDescription(
                getResources().getText(R.string.end_marker) + " " +
                        formatTime(mEndPos));*/

        int startX = mStartPos - mOffset - mMarkerLeftInset;
        if (startX + mStartMarker.getWidth() >= 0) {
            if (!mStartVisible) {
                mHandler.postDelayed(new Runnable() {
                    public void run() {
                        mStartVisible = true;
                        mStartMarker.setAlpha(1f);
                    }
                }, 0);
            }
        } else {
            if (mStartVisible) {
                mStartMarker.setAlpha(0f);
                mStartVisible = false;
            }
            startX = 0;
        }

        int endX = mEndPos - mOffset - mEndMarker.getWidth() + mMarkerRightInset;
        if (endX + mEndMarker.getWidth() >= 0) {
            if (!mEndVisible) {
                mHandler.postDelayed(new Runnable() {
                    public void run() {
                        mEndVisible = true;
                        mEndMarker.setAlpha(1f);
                    }
                }, 0);
            }
        } else {
            if (mEndVisible) {
                mEndMarker.setAlpha(0f);
                mEndVisible = false;
            }
            endX = 0;
        }

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(
                startX,
                mMarkerTopOffset,
                -mStartMarker.getWidth(),
                -mStartMarker.getHeight());
        mStartMarker.setLayoutParams(params);

        params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(
                endX,
                mWaveformView.getMeasuredHeight() - mEndMarker.getHeight() - mMarkerBottomOffset,
                -mStartMarker.getWidth(),
                -mStartMarker.getHeight());
        mEndMarker.setLayoutParams(params);
    }

    private Runnable mTimerRunnable = new Runnable() {
        public void run() {
            if (mStartPos != mLastDisplayedStartPos &&
                    !mStartText.hasFocus()) {
                mStartText.setText(formatTime(mStartPos));
                mLastDisplayedStartPos = mStartPos;
            }

            if (mEndPos != mLastDisplayedEndPos &&
                    !mEndText.hasFocus()) {
                mEndText.setText(formatTime(mEndPos));
                mLastDisplayedEndPos = mEndPos;
            }

            mHandler.postDelayed(mTimerRunnable, 100);
        }
    };

    private void enableDisableButtons() {
        if (mIsPlaying) {
            mPlayButton.setImageResource(R.drawable.pause);
            mPlayButton.setContentDescription(getResources().getText(R.string.stop));
        } else {
            mPlayButton.setImageResource(R.drawable.play);
            mPlayButton.setContentDescription(getResources().getText(R.string.play));
        }
    }

    private void resetPositions() {
        mStartPos = mWaveformView.secondsToPixels(0.0);
        mEndPos = mWaveformView.secondsToPixels(15.0);
    }

    private int trap(int pos) {
        if (pos < 0)
            return 0;
        if (pos > mMaxPos)
            return mMaxPos;
        return pos;
    }

    private void setOffsetGoalStart() {
        setOffsetGoal(mStartPos - mWidth / 2);
    }

    private void setOffsetGoalStartNoUpdate() {
        setOffsetGoalNoUpdate(mStartPos - mWidth / 2);
    }

    private void setOffsetGoalEnd() {
        setOffsetGoal(mEndPos - mWidth / 2);
    }

    private void setOffsetGoalEndNoUpdate() {
        setOffsetGoalNoUpdate(mEndPos - mWidth / 2);
    }

    private void setOffsetGoal(int offset) {
        setOffsetGoalNoUpdate(offset);
        updateDisplay();
    }

    private void setOffsetGoalNoUpdate(int offset) {
        if (mTouchDragging) {
            return;
        }

        mOffsetGoal = offset;
        if (mOffsetGoal + mWidth / 2 > mMaxPos)
            mOffsetGoal = mMaxPos - mWidth / 2;
        if (mOffsetGoal < 0)
            mOffsetGoal = 0;
    }

    private String formatTime(int pixels) {
        if (mWaveformView != null && mWaveformView.isInitialized()) {
            return formatDecimal(mWaveformView.pixelsToSeconds(pixels));
        } else {
            return "";
        }
    }

    private String formatDecimal(double x) {
        int xWhole = (int)x;
        int xFrac = (int)(100 * (x - xWhole) + 0.5);

        if (xFrac >= 100) {
            xWhole++;
            xFrac -= 100;
            if (xFrac < 10) {
                xFrac *= 10;
            }
        }

        if (xFrac < 10)
            return xWhole + ".0" + xFrac;
        else
            return xWhole + "." + xFrac;
    }

    private synchronized void handlePause() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.pause();
        }
        mWaveformView.setPlayback(-1);
        mIsPlaying = false;
        enableDisableButtons();
    }

    private synchronized void onPlay(int startPosition) {
        if (mIsPlaying) {
            handlePause();
            return;
        }

        if (mPlayer == null) {
            return;
        }

        try {
            mPlayStartMsec = mWaveformView.pixelsToMillisecs(startPosition);
            if (startPosition < mStartPos) {
                mPlayEndMsec = mWaveformView.pixelsToMillisecs(mStartPos);
            } else if (startPosition > mEndPos) {
                mPlayEndMsec = mWaveformView.pixelsToMillisecs(mMaxPos);
            } else {
                mPlayEndMsec = mWaveformView.pixelsToMillisecs(mEndPos);
            }
            mPlayer.setOnCompletionListener(new SamplePlayer.OnCompletionListener() {
                @Override
                public void onCompletion() {
                    handlePause();
                }
            });
            mIsPlaying = true;

            mPlayer.seekTo(mPlayStartMsec);
            mPlayer.start();
            updateDisplay();
            enableDisableButtons();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    private View.OnClickListener mPlayListener = new View.OnClickListener() {
        public void onClick(View sender) {
            onPlay(mStartPos);
        }
    };

    private View.OnClickListener mRewindListener = new View.OnClickListener() {
        public void onClick(View sender) {
            if (mIsPlaying) {
                int newPos = mPlayer.getCurrentPosition() - 5000;
                if (newPos < mPlayStartMsec)
                    newPos = mPlayStartMsec;
                mPlayer.seekTo(newPos);
            } else {
                mStartMarker.requestFocus();
                markerFocus(mStartMarker);
            }
        }
    };

    private View.OnClickListener mFfwdListener = new View.OnClickListener() {
        public void onClick(View sender) {
            if (mIsPlaying) {
                int newPos = 5000 + mPlayer.getCurrentPosition();
                if (newPos > mPlayEndMsec)
                    newPos = mPlayEndMsec;
                mPlayer.seekTo(newPos);
            } else {
                mEndMarker.requestFocus();
                markerFocus(mEndMarker);
            }
        }
    };

    private TextWatcher mTextWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start,
                                      int count, int after) {
        }

        public void onTextChanged(CharSequence s,
                                  int start, int before, int count) {
        }

        public void afterTextChanged(Editable s) {
            if (mStartText.hasFocus()) {
                try {
                    mStartPos = mWaveformView.secondsToPixels(
                            Double.parseDouble(
                                    mStartText.getText().toString()));
                    updateDisplay();
                } catch (NumberFormatException ignored) {}
            }
            if (mEndText.hasFocus()) {
                try {
                    mEndPos = mWaveformView.secondsToPixels(
                            Double.parseDouble(
                                    mEndText.getText().toString()));
                    updateDisplay();
                } catch (NumberFormatException e) {
                }
            }
        }
    };

    private long getCurrentTime() {
        return System.nanoTime() / 1000000;
    }

    public class DownloadFileTask extends AsyncTask<String, Void, SoundFile>{
        private boolean isNext;
        int currentTrack;

        DownloadFileTask(boolean isNext, int currentTrack){
            this.isNext = isNext;
            this.currentTrack = currentTrack;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected SoundFile doInBackground(String... params) {
            SoundFile file;
            try {
               file = SoundFile.create(params[0], null);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } catch (SoundFile.InvalidInputException e) {
                e.printStackTrace();
                return null;
            }
            return file;
        }

        @Override
        protected void onPostExecute(SoundFile file) {
            super.onPostExecute(file);
            if (isAdded()) {
                try {
                    if (isNext && this.currentTrack == currentPosition) {
                        mNextSoundFile = file;
                        new MySnackbar(getActivity(), mPlayButton, R.string.forward_track_loaded).show();
                    } else if (!isNext && this.currentTrack == currentPosition) {
                        mPrevSoundFile = file;
                        new MySnackbar(getActivity(), mPlayButton, R.string.prev_track_loaded).show();
                    }
                } catch (NullPointerException ignored) {}
            }
        }
    }
}
