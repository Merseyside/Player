package com.merseyside.admin.player.LastFm;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.PopupMenu;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.merseyside.admin.player.ActivitesAndFragments.MainActivity;
import com.merseyside.admin.player.AdaptersAndItems.LastFmArtistsAdapter;
import com.merseyside.admin.player.AdaptersAndItems.LastFmTracksAdapter;
import com.merseyside.admin.player.Dialogs.AuthDialog;
import com.merseyside.admin.player.Dialogs.LastFmOptionsDialog;
import com.merseyside.admin.player.LastFm.LastFmEngine;
import com.merseyside.admin.player.R;
import com.merseyside.admin.player.Utilities.MySnackbar;
import com.merseyside.admin.player.Utilities.PlaybackManager;
import com.merseyside.admin.player.Utilities.Settings;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import de.umass.lastfm.Artist;
import de.umass.lastfm.ImageSize;
import de.umass.lastfm.Track;
import de.umass.lastfm.User;

/**
 * Created by Admin on 13.03.2017.
 */

public class LastFmFragment extends Fragment implements View.OnClickListener{
    private final static int RECENT_TRACK = 0;
    private final static int LOVED_TRACK = 1;
    private final static int TOP_TRACK = 2;
    private final static int WEEKLY_TRACK_CHART = 3;
    private final static int WEEKLY_ARTIST_CHART = 4;
    private final static int TOP_ARTIST = 5;
    private final static int ARTISTS_TRACKS = 10;
    private final static int SIMILAR_TRACKS = 11;

    private final String SIGN_IN = "https://www.last.fm/join";

    private ImageView header;
    private Settings settings;
    private LastFmEngine lastFmEngine;
    private AuthDialog authDialog;
    private ImageButton menu;
    private FloatingActionButton options_button;
    private ListView listView;
    private GridView gridView;
    private LastFmOptionsDialog optionsDialog;
    private ArrayList<Track> track_list;
    private LastFmTracksAdapter tracksAdapter;
    private int optionNum = -1;
    private LinearLayout footer;
    private Button prev_page, next_page;
    private TextView pages_tv, header_textview;
    private LastFmArtistsAdapter artistsAdapter;
    private ArrayList<Artist> artist_list;
    private String artist, track;
    private CircleImageView user_cover;
    private String userUrl;
    private String user, password;

    private boolean remember = true;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.lastfm_fragment, null);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        settings = new Settings(getActivity());

        header = (ImageView) getView().findViewById(R.id.lastfm_header);
        header.setImageBitmap(Settings.lastfm_header);

        menu = (ImageButton) getView().findViewById(R.id.settings);
        menu.setOnClickListener(this);

        user_cover = (CircleImageView) getView().findViewById(R.id.user_cover);
        user_cover.setOnClickListener(this);

        header_textview = (TextView) getView().findViewById(R.id.lastfm_textview);
        settings.setTextViewFont(header_textview, null);

        footer = (LinearLayout) getView().findViewById(R.id.lastfm_footer_layout);
        prev_page = (Button) getView().findViewById(R.id.prev_page);
        prev_page.setOnClickListener(this);
        next_page = (Button) getView().findViewById(R.id.next_page);
        next_page.setOnClickListener(this);
        pages_tv = (TextView) getView().findViewById(R.id.footer_textview);

        options_button = (FloatingActionButton) getView().findViewById(R.id.lastfm_options_button);
        options_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] options_array = getActivity().getResources().getStringArray(R.array.lastfm_options);
                ArrayList<String> array = new ArrayList<>();
                for (String opt : options_array){
                    array.add(opt);
                }
                optionsDialog = new LastFmOptionsDialog(getActivity(), array);
                optionsDialog.setLastFmOptionsListener(new LastFmOptionsDialog.LastFmOptionsListener() {
                    @Override
                    public void userPressedItem(int position) {
                        optionNum = position;
                        applyOption(position);
                    }
                });
                optionsDialog.show();
            }
        });

        listView = (ListView) getView().findViewById(R.id.lastfm_listview);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                if (tracksAdapter != null) openBrowser(tracksAdapter.getItem(position).getUrl());
                else if (artistsAdapter != null) openBrowser(artistsAdapter.getItem(position).getUrl());
            }
        });

        gridView = (GridView) getView().findViewById(R.id.lastfm_gridView);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                if (tracksAdapter != null) openBrowser(tracksAdapter.getItem(position).getUrl());
                else if (artistsAdapter != null) openBrowser(artistsAdapter.getItem(position).getUrl());
            }
        });

        lastFmEngine = MainActivity.getLastFmEngine();
        lastFmEngine.setLastFmEventListener(new LastFmEngine.LastFmEventListener() {
            @Override
            public void authComplete() {
                savePreferences(lastFmEngine.getUsername(), lastFmEngine.getPassword(), remember);
                if (isAdded()) setStartScreen();
            }
            @Override
            public void authError() {
                openAuthDialog();
            }
            @Override
            public void tracksLoaded(ArrayList<Track> tracks) {
                track_list = tracks;
                if (track_list == null || track_list.size()==0) track_list = null;
                artistsAdapter = null;
                artist_list = null;
                String pages = lastFmEngine.getCurrentTracksPage() + "/" + lastFmEngine.getTracksPagesCount();
                pages_tv.setText(pages);
                fillView();
            }

            @Override
            public void artistsLoaded(ArrayList<Artist> artists) {
                artist_list = artists;
                if (artist_list == null || artist_list.size()==0) artist_list = null;
                tracksAdapter = null;
                track_list = null;
                fillView();
            }

            @Override
            public void userInfoLoaded(User user) {
                setUserInfo(user);
            }
        });
        if (!lastFmEngine.isAuthInProgress()){
            if (!lastFmEngine.isAuth()) openAuthDialog();
            else setStartScreen();
        }
    }

    private void setStartScreen(){
        setUserInfo();
        switch (optionNum){
            case ARTISTS_TRACKS:
                header_textview.setText(getResources().getString(R.string.artist_tracks));
                if (header_textview.getText().length() >= 13) header_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_custom_font_size));
                getArtistsTracks();
                break;
            case SIMILAR_TRACKS:
                header_textview.setText(getResources().getString(R.string.similar_tracks));
                if (header_textview.getText().length() >= 13) header_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_custom_font_size));
                getSimilarTracks();
                break;
            default:
                applyOption(Integer.valueOf(Settings.LASTFM_START));
                break;
        }
    }

    private void setUserInfo(User user){
        userUrl = user.getUrl();
        MyTask task = new MyTask(user_cover);
        task.execute(user);
    }

    private void setUserInfo(){
        lastFmEngine.getUserInfo(getActivity());
    }


    public void getArtistsTracks(String artist) {
        this.artist = artist;
        this.track = null;
        optionNum = ARTISTS_TRACKS;
    }

    private void getArtistsTracks(){
        if (isAdded() && lastFmEngine.isAuth()){
            lastFmEngine.getArtistsTracks(artist);
            footer.setVisibility(View.GONE);
        }
    }

    private void getSimilarTracks(){
        if (isAdded() && lastFmEngine.isAuth()) {
            lastFmEngine.getSimilarTracks(artist, track);
            footer.setVisibility(View.GONE);
        }
    }

    public void getSimilarTracks(String artist, String track){
        this.artist = artist;
        this.track = track;
        optionNum = SIMILAR_TRACKS;
    }

    private void applyOption(int position){
        boolean error = false;
        switch(position){
            case RECENT_TRACK:
                if (!lastFmEngine.getRecentTracks()) error = true;
                else {
                    header_textview.setText(getString(R.string.lastfm_recent_tracks));
                    footer.setVisibility(View.VISIBLE);
                    artist_list = null;
                }
                break;
            case LOVED_TRACK:
                if (!lastFmEngine.getLovedTracks()) error = true;
                else {
                    header_textview.setText(getString(R.string.lastfm_loved_tracks));
                    footer.setVisibility(View.VISIBLE);
                    artist_list = null;
                }
                break;
            case TOP_TRACK:
                if (!lastFmEngine.getTopTracks()) error = true;
                else {
                    header_textview.setText(getString(R.string.lastfm_top_tracks));
                    footer.setVisibility(View.GONE);
                    artist_list = null;
                }
                break;
            case WEEKLY_TRACK_CHART:
                if (!lastFmEngine.getWeeklyTrackChart()) error = true;
                else {
                    header_textview.setText(getString(R.string.lastfm_weekly_track_chart));
                    footer.setVisibility(View.GONE);
                    artist_list = null;
                }
                break;
            case WEEKLY_ARTIST_CHART:
                if (!lastFmEngine.getWeeklyArtistChart()) error = true;
                else {
                    header_textview.setText(getString(R.string.lastfm_weekly_artist_chart));
                    footer.setVisibility(View.GONE);
                    track_list = null;
                }
                break;
            case TOP_ARTIST:
                if (!lastFmEngine.getTopArtists()) error = true;
                else {
                    header_textview.setText(getString(R.string.lastfm_top_artist));
                    footer.setVisibility(View.GONE);
                    track_list = null;
                }
                break;

        }
        if (header_textview.getText().length() >= 13) header_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_custom_font_size));
        if (error) new MySnackbar(getActivity(), listView, R.string.check_connection).show();
    }

    private void openBrowser(String url){
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    private void savePreferences(String user, String password, boolean remember){
        Settings.REMEMBER_PASS = remember;
        settings.savePreference(Settings.APP_PREFERENCES_REMEMBER_PASS, remember);
        settings.savePreference(Settings.APP_PREFERENCES_USERNAME, user);
        Settings.USERNAME = user;
        settings.savePreference(Settings.APP_PREFERENCES_PASSWORD, password);
        Settings.PASSWORD = password;
    }

    private void openAuthDialog(){
        authDialog = new AuthDialog(getActivity());
        authDialog.setUserAndPassword(user, password);
        authDialog.setCancelable(false);
        authDialog.setAuthDialogListener(new AuthDialog.AuthDialogListener() {
            @Override
            public void userPressedLogIn(String user1, String password1, boolean remember1) {
                remember = remember1;
                user = user1;
                password = password1;
                lastFmEngine.auth(user, password, true);
            }

            @Override
            public void userPressedCancel(String user1, String password1) {
                user = user1;
                password = password1;
                closeFragment();
            }

            @Override
            public void userPressedSignIn() {
                openBrowser(SIGN_IN);
            }
        });
        authDialog.show();
    }

    private void fillView(){
        if (track_list != null){
            if (Settings.LASTFM_VIEW.equals(Settings.LIST_VIEW)) {
                listView.setVisibility(View.VISIBLE);
                gridView.setVisibility(View.GONE);
                tracksAdapter = new LastFmTracksAdapter(getActivity(), R.layout.lastfm_track_listview, track_list);
                listView.setAdapter(tracksAdapter);
            } else {
                listView.setVisibility(View.GONE);
                gridView.setVisibility(View.VISIBLE);
                tracksAdapter = new LastFmTracksAdapter(getActivity(), R.layout.lastfm_track_gridview, track_list);
                gridView.setAdapter(tracksAdapter);
            }
        } else if(artist_list != null){
            if (Settings.LASTFM_VIEW.equals(Settings.LIST_VIEW)){
                listView.setVisibility(View.VISIBLE);
                gridView.setVisibility(View.GONE);
                artistsAdapter = new LastFmArtistsAdapter(getActivity(), R.layout.lastfm_artist_listview, artist_list);
                listView.setAdapter(artistsAdapter);
            } else {
                listView.setVisibility(View.GONE);
                gridView.setVisibility(View.VISIBLE);
                artistsAdapter = new LastFmArtistsAdapter(getActivity(), R.layout.lastfm_artist_gridview, artist_list);
                gridView.setAdapter(artistsAdapter);
            }
        } else {
            if (tracksAdapter != null) {
                tracksAdapter.clear();
                tracksAdapter.notifyDataSetChanged();
            }
            if (artistsAdapter != null) {
                artistsAdapter.clear();
                artistsAdapter.notifyDataSetChanged();
            }
        }
    }

    private void closeFragment(){
        getActivity().getSupportFragmentManager().popBackStack();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.settings:
                showMenu(view);
                break;
            case R.id.prev_page:
                if (!lastFmEngine.prevPage()) new MySnackbar(getActivity(), listView, R.string.first_page).show();
                break;
            case R.id.next_page:
                if (!lastFmEngine.nextPage()) new MySnackbar(getActivity(), listView, R.string.last_page).show();
                break;
            case R.id.user_cover:
                if (userUrl != null && !userUrl.equals("")) openBrowser(userUrl);
        }
    }

    private void showMenu(View v){
        PopupMenu popupMenu = new PopupMenu(getActivity(), v);
        popupMenu.inflate(R.menu.lastfm_menu);

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.list_view:
                        settings.savePreference(Settings.APP_PREFERENCES_LASTFM_VIEW, Settings.LIST_VIEW);
                        Settings.LASTFM_VIEW = Settings.LIST_VIEW;
                        fillView();
                        break;
                    case R.id.grid_view:
                        settings.savePreference(Settings.APP_PREFERENCES_LASTFM_VIEW, Settings.GRID_VIEW);
                        Settings.LASTFM_VIEW = Settings.GRID_VIEW;
                        fillView();
                        break;
                    case R.id.refresh:
                        applyOption(optionNum);
                        break;
                    case R.id.log_out:
                        lastFmEngine.logOut();
                        tracksAdapter.clear();
                        tracksAdapter.notifyDataSetChanged();
                        userUrl = null;
                        openAuthDialog();
                        break;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private class MyTask extends AsyncTask<User, Void, Bitmap> {

        ImageView view;
        public MyTask(ImageView view){
            this.view = view;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(User... user) {
            for (User o : user) {
                try {
                    return getBitmapFromURL(o.getImageURL(ImageSize.LARGE));
                } catch (RuntimeException ignored){return null;}
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            imageViewAnimatedChange(getActivity(), view, bitmap);
        }

        public void imageViewAnimatedChange(Context c, final ImageView v, final Bitmap new_image) {
            final Animation anim_in  = AnimationUtils.loadAnimation(c, android.R.anim.fade_in);
            v.setImageBitmap(new_image);
            anim_in.setAnimationListener(new Animation.AnimationListener() {
                @Override public void onAnimationStart(Animation animation) {}
                @Override public void onAnimationRepeat(Animation animation) {}
                @Override public void onAnimationEnd(Animation animation) {}
            });
            v.startAnimation(anim_in);
        }

        Bitmap getBitmapFromURL(String src) {
            try {
                URL url = null;
                try {
                    url = new URL(src);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                HttpURLConnection connection = null;
                try {
                    connection = (HttpURLConnection) url.openConnection();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return myBitmap;
            } catch (IOException e) {
                // Log exception
                return null;
            }
        }
    }
}
