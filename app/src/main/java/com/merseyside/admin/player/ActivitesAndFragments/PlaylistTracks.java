package com.merseyside.admin.player.ActivitesAndFragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.merseyside.admin.player.AdaptersAndItems.Item;
import com.merseyside.admin.player.AdaptersAndItems.ItemAdapter;
import com.merseyside.admin.player.AdaptersAndItems.Track;
import com.merseyside.admin.player.Dialogs.AddToPlaylistDialog;
import com.merseyside.admin.player.Dialogs.CommentDialog;
import com.merseyside.admin.player.Dialogs.ShareDialog;
import com.merseyside.admin.player.Dialogs.TracksDialog;
import com.merseyside.admin.player.LastFm.LastFmEngine;
import com.merseyside.admin.player.LastFm.LastFmFragment;
import com.merseyside.admin.player.MegamixLibrary.MegamixCreator;
import com.merseyside.admin.player.R;
import com.merseyside.admin.player.Utilities.DBHelper;
import com.merseyside.admin.player.Utilities.FileManager;
import com.merseyside.admin.player.Utilities.InfoForPlayer;
import com.merseyside.admin.player.Utilities.M3UParser;
import com.merseyside.admin.player.Utilities.MySnackbar;
import com.merseyside.admin.player.Utilities.MySwipeRefreshLayout;
import com.merseyside.admin.player.Utilities.PrintString;
import com.merseyside.admin.player.Utilities.Settings;
import com.woxthebox.draglistview.DragListView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class PlaylistTracks extends Fragment implements View.OnClickListener, ItemAdapter.ItemClickListener, RecyclerView.OnChildAttachStateChangeListener{

    private RelativeLayout search_layout;
    private DragListView mDragListView;
    private MySwipeRefreshLayout mRefreshLayout;

    private ImageButton search, clear, menu;
    private EditText search_et;
    private TextView name_tw, count_tw, total_duration;
    private String path;
    private String playlistName;
    public static ArrayList<Item> files;
    private M3UParser plParser;
    private FragmentTransaction fTrans;
    private FloatingActionButton fab;
    private ProgressDialog progress;
    private ItemAdapter listAdapter;
    boolean isDragged = false;
    boolean isNotificated;
    private ArrayList<Track> list;
    private ImageView image;
    private CircleImageView image_circle;

    private Player_Fragment.Type TYPE;
    private FileManager manager;
    private SQLiteDatabase db;
    private Settings settings;
    /*Fragments*/
    private Info info;
    private Player_Fragment player_fragment;
    private LastFmFragment lastFmFragment;

    private String table;
    private ImageView header;

    private final String ID_KEY = "id";
    private final String TABLE_KEY = "table";
    private final String IMAGE_KEY = "image";

    private String id, image_url;

    private boolean isAnimationEnded = false;
    private static final int REQUEST_PATH = 1;

    public void putArguments(HashMap<String, String> data){
        Bundle bundle = new Bundle();
        for (Map.Entry<String, String> entry : data.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            bundle.putString(key, value);
        }
        this.setArguments(bundle);
        data.clear();
    }

    public void ParentFragment(){
        player_fragment = MainActivity.get_player_fragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings = new Settings(getActivity());
        info = new Info();
        ParentFragment();
        isNotificated = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.playlist_drag, container, false);
        files = new ArrayList<>();
        mRefreshLayout = (MySwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        mRefreshLayout.setEnabled(false);
        mDragListView = (DragListView) view.findViewById(R.id.drag_list_view);
        mDragListView.getRecyclerView().setVerticalScrollBarEnabled(true);
        mDragListView.getRecyclerView().addOnChildAttachStateChangeListener(this);
        mDragListView.setDragListListener(new DragListView.DragListListenerAdapter() {
            @Override
            public void onItemDragStarted(int position) {}

            @Override
            public void onItemDragEnded(int fromPosition, int toPosition) {
                isDragged = true;
                listAdapter.orderChanged();
            }
        });

        mRefreshLayout.setScrollingView(mDragListView.getRecyclerView());
        mRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity(), R.color.black));
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mRefreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mRefreshLayout.setRefreshing(false);
                    }
                }, 2000);
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        manager = new FileManager(getActivity());
        if (savedInstanceState != null){
            id = savedInstanceState.getString(ID_KEY);
            table = savedInstanceState.getString(TABLE_KEY);
            image_url = savedInstanceState.getString(IMAGE_KEY);
        } else {
            id = this.getArguments().getString(ID_KEY);
            table = this.getArguments().getString(TABLE_KEY);
            image_url = this.getArguments().getString(IMAGE_KEY);
        }

        header = (ImageView) getView().findViewById(R.id.tracks_header);
        PrintString.printLog("lifeCycle", Settings.getScreenHeight() + " " + Settings.getScreenWidth());
        header.setImageBitmap(Settings.tracks_header);

        name_tw = (TextView)getView().findViewById(R.id.playlistName);
        settings.setTextViewFont(name_tw, null);
        count_tw = (TextView)getView().findViewById(R.id.countOfTracks);
        total_duration = (TextView)getView().findViewById(R.id.totalDuration);
        search = (ImageButton)getView().findViewById(R.id.search_btn);
        search.setOnClickListener(this);
        search_layout = (RelativeLayout)getView().findViewById(R.id.search_layout);
        search_et = (EditText)getView().findViewById(R.id.search_et);
        clear = (ImageButton)getView().findViewById(R.id.clear_button);
        clear.setOnClickListener(this);
        menu = (ImageButton) getView().findViewById(R.id.settings);
        menu.setOnClickListener(this);
        fab = (FloatingActionButton) getView().findViewById(R.id.tracks_add_button);
        search_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                PrintString.printLog("EditText", "beforeTextChanged" + i + " " + i1 + " " + i2);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                PrintString.printLog("EditText", "onTextChanged" + i + " " + i1 + " " + i2);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                PrintString.printLog("EditText", "afterTextChanged");
                fill(filterList(list, search_et.getText().toString()));
            }
        });
        setupPlaylist();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        new Thread(new Runnable() {
            @Override
            public void run() {
                getList(path);
                if (isAnimationEnded) fill(list);
            }
        }).start();
    }

    private void setupPlaylist(){
        switch (table) {
            case DBHelper.ALL_TRACKS:
                path = "";
                playlistName = getString(R.string.all_tracks);
                fab.setVisibility(View.GONE);
                TYPE = Player_Fragment.Type.MEMORY;
                break;
            case DBHelper.RATED:
                path = "";
                playlistName = getString(R.string.rated);
                fab.setVisibility(View.GONE);
                TYPE = Player_Fragment.Type.MEMORY;
                break;
            case DBHelper.MEGAMIX:
                path = "";
                playlistName = getString(R.string.megamix_tracks);
                fab.setVisibility(View.GONE);
                TYPE = Player_Fragment.Type.MEMORY;
                break;
            case DBHelper.COMMENT:
                path = "";
                playlistName = getString(R.string.commented_tracks);
                fab.setVisibility(View.GONE);
                TYPE = Player_Fragment.Type.MEMORY;
                break;
            case DBHelper.RECENTLY_ADDED:
                path = "";
                playlistName = getString(R.string.recently_added);
                fab.setVisibility(View.GONE);
                TYPE = Player_Fragment.Type.MEMORY;
                break;
            default:
                String selection = "_id = ?";
                String[] selectionArgs = new String[]{id};
                db = Settings.getDbHelper().getWritableDatabase();
                Cursor c = db.query(table, null, selection, selectionArgs, null, null, null);
                if (c.moveToFirst()) {
                    do {
                        int nameColIndex = c.getColumnIndex(DBHelper.NAME_COLUMN);
                        int urlColIndex = c.getColumnIndex(DBHelper.URL_COLUMN);
                        playlistName = c.getString(nameColIndex);
                        path = c.getString(urlColIndex);
                    } while (c.moveToNext());
                }
                c.close();

                if (!table.equals(DBHelper.TABLE_MEMORY_NAME)) {
                    TYPE = Player_Fragment.Type.PLAYLIST;
                    fab.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            switch (view.getId()) {
                                case R.id.tracks_add_button: {
                                    Intent intent = new Intent(getActivity(), FileChooserActivity.class);
                                    intent.putExtra("caller_activity", Settings.CALLER_ACTIVITY_ADD_TRACKS_TO_PLAYLIST);
                                    startActivityForResult(intent, REQUEST_PATH);
                                    break;
                                }
                            }
                        }
                    });
                } else {
                    TYPE = Player_Fragment.Type.MEMORY;
                    fab.setVisibility(View.GONE);
                }
                break;
        }
        name_tw.setText(playlistName);
        if (playlistName.length() >= 13) name_tw.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_custom_font_size));

        image_circle = (CircleImageView) getView().findViewById(R.id.picture_circle);
        image = (ImageView) getView().findViewById(R.id.picture);
        image.setVisibility(View.INVISIBLE);

        if (image_url == null) image_url = "";
        switch (table) {
            case DBHelper.TABLE_MEMORY_NAME:
                if (image_url.equals("")) {
                    image_circle.setVisibility(View.INVISIBLE);
                    image.setVisibility(View.VISIBLE);
                    image.setImageResource(settings.getAttributeId(settings.getThemeByString(), R.attr.theme_dependent_folder_icon));
                }
                else {
                    try {
                        image_circle.setVisibility(View.VISIBLE);
                        image.setVisibility(View.INVISIBLE);
                        MediaMetadataRetriever retriver = new MediaMetadataRetriever();
                        retriver.setDataSource(image_url);
                        byte[] data = retriver.getEmbeddedPicture();
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        image_circle.setImageBitmap(bitmap);
                    } catch (IllegalArgumentException ignored) {}
                }
                break;
            case DBHelper.TABLE_PLAYLIST_NAME:
                image_circle.setImageResource(settings.getAttributeId(settings.getThemeByString(), R.attr.theme_dependent_playlist_cover));
                break;
            default:
                image_circle.setVisibility(View.INVISIBLE);
                image.setVisibility(View.VISIBLE);

                switch (table) {
                    case DBHelper.ALL_TRACKS:
                        image.setImageResource(R.drawable.note);
                        break;
                    case DBHelper.MEGAMIX:
                        image.setImageResource(R.drawable.wave);
                        break;
                    case DBHelper.RATED:
                        image.setImageResource(R.drawable.star_empty);
                        break;
                    case DBHelper.COMMENT:
                        image.setImageResource(R.drawable.comment);
                        break;
                    case DBHelper.RECENTLY_ADDED:
                        image.setImageResource(R.drawable.clock);
                        break;
                }
                image.setColorFilter(ContextCompat.getColor(getActivity(), settings.getAttributeId(settings.getThemeByString(), R.attr.theme_dependent_accent_color)));
                break;
        }
    }

    private String getStringDuration(long total){
        int hours = (int)total/3600;
        total%=3600;
        int minutes = (int) total / 60;
        int seconds = (int) total % 60;
        String timeString;
        if (hours == 0)
        timeString = String.format("%d:%02d", minutes, seconds);
        else timeString = String.format("%d:%02d:%02d",hours, minutes, seconds);
        return timeString;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isDragged && search_et.getText().length() == 0){
            try {
                plParser.changePlaylist(listAdapter.getAll());
            } catch (NullPointerException ignored){}
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ID_KEY, id);
        outState.putString(TABLE_KEY, table);
        outState.putString(IMAGE_KEY, image_url);
    }

    private void getList(String path){
        File file = new File(path);
        if (file.exists()) {
            plParser = new M3UParser(file, getActivity());
            list = plParser.getTracksList();
            if (list == null){
                return;
            }
            if (plParser.isErrored() && !isNotificated) {
                PrintString.printLog("LastError", plParser.getLastErr());
                new MySnackbar(getActivity(), fab, R.string.tracks_not_displaying, true).show();
                isNotificated = true;
            }
        }
        else {
            switch (table) {
                case DBHelper.ALL_TRACKS:
                    list = manager.getAllTracks();
                    playlistName = DBHelper.ALL_TRACKS;
                    break;
                case DBHelper.RATED:
                    list = manager.getRatedTracks();
                    playlistName = DBHelper.RATED;
                    break;
                case DBHelper.MEGAMIX:
                    list = manager.getMegamixTracks();
                    playlistName = DBHelper.MEGAMIX;
                    break;
                case DBHelper.COMMENT:
                    list = manager.getCommentedTracks();
                    playlistName = DBHelper.COMMENT;
                    break;
                case DBHelper.RECENTLY_ADDED:
                    list = manager.getRecentlyAddedTracks();
                    playlistName = DBHelper.COMMENT;
                    break;
                default:
                    Toast.makeText(getActivity(), "Uncorrect playlist path", Toast.LENGTH_SHORT).show();
                    return;
            }
        }

        if (list == null) {
            PrintString.printLog("LastError", plParser.getLastErr());
            return;
        }
    }

    private void fill(ArrayList<Track> list) {
        try {
            Iterator<Track> listIterator = list.iterator();
            while (listIterator.hasNext()) break;
            if (Settings.TRACKS_VIEW.equals(Settings.GRID_VIEW)) setupGridVerticalRecyclerView(list);
            else setupListRecyclerView(list);
            setTotalDurationAndCount(list);

        } catch(NullPointerException e) {
            e.printStackTrace();
            new MySnackbar(getActivity(), fab, R.string.no_tracks).show();
            return;
        }
    }

    private ArrayList<Track> filterList(ArrayList<Track> list, String filter) {
        int i = 0;
        ArrayList<Track> newList = list == null ? null : (ArrayList<Track>) list.clone();
        Track track;
        try {
            while (i != newList.size()) {
                track = newList.get(i);
                if (track.getName() != null && track.getName().toLowerCase().contains(filter.toLowerCase())) {
                    i++;
                    continue;
                }
                if (track.getArtist() != null && track.getArtist().toLowerCase().contains(filter.toLowerCase())) {
                    i++;
                    continue;
                }
                newList.remove(track);
            }
        } catch (NullPointerException ignored){}
        return newList;
    }

    private void setTotalDurationAndCount(ArrayList<Track> list){
        long total = 0;
        for (Track track : list){
            total += Integer.valueOf(track.getDuration());
        }
        final long total1 = total;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    PrintString.printLog("Track", listAdapter.getCount() + "");
                    count_tw.setText(String.valueOf(getResources().getString(R.string.tracks) + ": " + listAdapter.getCount()));
                }catch (NullPointerException ignored){}
                total_duration.setText(getResources().getText(R.string.total_duration) + " " + getStringDuration(total1));
            }
        });
    }

    private void deleteTrack(Track track, int position) throws NullPointerException{
        if (!plParser.deleteTrack(track))
            PrintString.printLog("Parser", plParser.getLastErr());
        else {
            listAdapter.removeItem(position);
            listAdapter.notifyDataSetChanged();
            list = listAdapter.getAll();
            setTotalDurationAndCount(list);
        }
    }

    private void setupListRecyclerView(final ArrayList<Track> tracks_list) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDragListView.setLayoutManager(new LinearLayoutManager(getActivity()));
                ArrayList<Pair<Long, Track>> valid_list = getValidList(tracks_list);
                listAdapter = new ItemAdapter(getActivity(), valid_list, R.layout.track_listview, R.id.drag_image, false, true);
                listAdapter.setOnItemClickListener(PlaylistTracks.this);
                mDragListView.setAdapter(listAdapter, true);
                mDragListView.setCanDragHorizontally(false);
                mDragListView.setDragEnabled(false);
                mDragListView.setCustomDragItem(null);
                listAdapter.setLayoutManager(mDragListView.getRecyclerView().getLayoutManager());
            }
        });
    }

    private void setupGridVerticalRecyclerView(final ArrayList<Track> tracks_list) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDragListView.setLayoutManager(new GridLayoutManager(getContext(), Settings.ROW_COUNT));
                ArrayList<Pair<Long, Track>> valid_list = getValidList(tracks_list);
                listAdapter = new ItemAdapter(getActivity(), valid_list, R.layout.track_gridview, R.id.grid_view_layout, true, false);
                listAdapter.setOnItemClickListener(PlaylistTracks.this);
                mDragListView.setAdapter(listAdapter, true);
                mDragListView.setCanDragHorizontally(true);
                mDragListView.setDragEnabled(false);
                mDragListView.setCustomDragItem(null);
                listAdapter.setLayoutManager(mDragListView.getRecyclerView().getLayoutManager());
            }
        });
    }

    public static ArrayList<Pair<Long, Track>> getValidList(ArrayList<Track> list){
        int i = 0;
        ArrayList<Pair<Long, Track>> valid_list = new ArrayList<>();
        for (Track track : list){
            valid_list.add(new Pair<>(Long.valueOf(i), track));
            i++;
        }
        return valid_list;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        // See which child activity is calling us back.
        if (requestCode == REQUEST_PATH){
            if (resultCode == Activity.RESULT_OK) {
                if (files.size()!=0) {
                    progress = new ProgressDialog(getActivity(), R.style.DialogStyle);
                    progress.setTitle(getString(R.string.please_wait));
                    progress.setMessage(getString(R.string.applying_changes));
                    progress.setCancelable(false);
                    progress.show();
                    new Thread(new Runnable() {
                        @Override
                        public void run()
                        {
                            plParser.addItemsToPlaylist(files);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run()
                                {
                                    progress.dismiss();
                                    getList(path);
                                    fill(list);
                                }
                            });
                        }
                    }).start();
                }
            }
        }
    }

    private void startInfoFragment(int position) {
        settings.hideKeyboard(getActivity());
        fTrans = getFragmentManager().beginTransaction();
        fTrans.setCustomAnimations(R.anim.slide_right_to_left, R.anim.slide_in_left,R.anim.slide_left_to_right, R.anim.slide_in_right);
        final Track o = listAdapter.getItem(position);
        info.setTrack(o);
        fTrans.replace(R.id.main_fragment,info);
        fTrans.addToBackStack(null);
        fTrans.commit();
    }

    private void playMemoryAndPlaylist(int position){
        settings.hideKeyboard(getActivity());
        listAdapter.deleteOnItemCliickListener();
        fTrans = getActivity().getSupportFragmentManager().beginTransaction();
        fTrans.setCustomAnimations(R.anim.slide_up_to_down, R.anim.slide_in_right);
        startPlayback(position);
        fTrans.replace(R.id.main_fragment, player_fragment);
        fTrans.commit();
    }

    private boolean startPlayback(int position){
        final InfoForPlayer ifp = new InfoForPlayer(TYPE, playlistName, listAdapter.getAll(), position);
        player_fragment.setInfo(ifp);
        if (player_fragment.isValidToStartPlayback()) {
            player_fragment.startPlay();
            return true;
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.search_btn:
                if (search_layout.getVisibility() == View.GONE) search_layout.setVisibility(View.VISIBLE);
                else search_layout.setVisibility(View.GONE);
                break;
            case R.id.clear_button:
                search_et.setText("");
                break;
            case R.id.settings:
                showMenu(view);
                break;
        }
    }

    @Override
    public void itemClicked(int position) {
        playMemoryAndPlaylist(position);
    }

    @Override
    public void itemLongClicked(final int position) {
        final Track track = listAdapter.getItem(position);
        TracksDialog cdd=new TracksDialog(getActivity(), new TracksDialog.MyDialogListener() {
            @Override
            public void userSelectedDelete() {
                try {
                    deleteTrack(track, position);
                } catch (NullPointerException ignored){
                    plParser = new M3UParser(new File(track.getPlaylistPath()), getActivity());
                    deleteTrack(track, position);
                }
            }
            @Override
            public void userSelectedInfo() {
                startInfoFragment(position);
            }

            @Override
            public void userSelectedOrder() {
                ArrayList<Track> list = new ArrayList<>();
                list.add(track);
                player_fragment.addOrder(list);
                new MySnackbar(getActivity(), fab, R.string.successfully_added).show();
            }

            @Override
            public void userSelectedPlay() {
                if (!startPlayback(position)) playMemoryAndPlaylist(position);
            }

            @Override
            public void userSelectedToPlaylist() {
                manager = new FileManager(getActivity());
                if (manager.getCountOfItems(DBHelper.TABLE_PLAYLIST_NAME) != 0){
                    AddToPlaylistDialog dialog = new AddToPlaylistDialog(getActivity(), new AddToPlaylistDialog.MyAddToPlaylistDialogListener() {
                        @Override
                        public void userSelectedPlaylist(String name,  String url) {
                            File file = new File(url);
                            if (file.exists()){
                                M3UParser parser = new M3UParser(file, getActivity());
                                ArrayList<Track> tracks = new ArrayList<>();
                                tracks.add(track);
                                if (!parser.addTracksToPlaylist(tracks)) {
                                    new MySnackbar(getActivity(), fab, R.string.cant_write).show();
                                } else new MySnackbar(getActivity(), fab, R.string.successfully_added).show();
                            }
                        }
                    });
                    dialog.show();
                }
                else new MySnackbar(getActivity(), fab, R.string.playlists_not_found, true).show();
            }

            @Override
            public void userSelectedShare() {
                ShareDialog dialog = new ShareDialog(getActivity(), track.cloneTrack());
                dialog.show();
            }

            @Override
            public void userSelectedMegamixCreator() {
                if (track.getType() == Track.MEMORY_TRACK) {
                    if (Settings.checkExternalPath(track.getPlaylistPath())) {
                        MegamixCreator megamixCreator = MainActivity.get_creator();
                        ArrayList<Track> list = new ArrayList<>();
                        for (Track track : listAdapter.getAll()){
                            if (track.getType() != Track.INTERNET_TRACK) list.add(track);
                        }
                        megamixCreator.setTracks(list, list.indexOf(listAdapter.getItem(position)), table, playlistName);
                        fTrans = getActivity().getSupportFragmentManager().beginTransaction();
                        if (Settings.ANIMATION) fTrans.setCustomAnimations(R.anim.slide_right_to_left, R.anim.slide_in_left,R.anim.slide_left_to_right, R.anim.slide_in_right);
                        fTrans.replace(R.id.main_fragment, megamixCreator);
                        fTrans.addToBackStack(null);
                        fTrans.commit();
                    } else new MySnackbar(getActivity(), fab, R.string.permission_denied, true).show();
                } else new MySnackbar(getActivity(), fab, R.string.megamix_memory_only).show();
            }

            @Override
            public void userSelectedCommentTrack() {
                if (Settings.checkExternalPath(track.getPlaylistPath()) && track.getType() == Track.MEMORY_TRACK) {
                    CommentDialog dialog = new CommentDialog(getActivity(), track.getComment(), new CommentDialog.CommentDialogListener() {
                        @Override
                        public void commentSaved(String comment) {
                            track.setComment(comment);
                            manager.setTrack(track);
                        }
                    });
                    dialog.show();
                }
            }

            @Override
            public void userSelectedSimilarTracks() {
                lastFmFragment = new LastFmFragment();
                lastFmFragment.getSimilarTracks(track.getArtist(), track.getName());
                fTrans = getActivity().getSupportFragmentManager().beginTransaction();
                if (Settings.ANIMATION) fTrans.setCustomAnimations(R.anim.slide_right_to_left, R.anim.slide_in_left,R.anim.slide_left_to_right, R.anim.slide_in_right);
                fTrans.replace(R.id.main_fragment, lastFmFragment);
                fTrans.addToBackStack(null);
                fTrans.commit();

            }

            @Override
            public void userSelectedLoveTrack() {
                LastFmEngine lastFmEngine = MainActivity.getLastFmEngine();
                if (lastFmEngine.loveTrack(track.getArtist(), track.getName()))
                    new MySnackbar(getActivity(), name_tw, R.string.successfully_liked).show();
                else new MySnackbar(getActivity(), name_tw, R.string.error_love).show();
            }

            @Override
            public void userSelectedArtistsTracks() {
                lastFmFragment = new LastFmFragment();
                lastFmFragment.getArtistsTracks(track.getArtist());
                fTrans = getActivity().getSupportFragmentManager().beginTransaction();
                if (Settings.ANIMATION) fTrans.setCustomAnimations(R.anim.slide_right_to_left, R.anim.slide_in_left,R.anim.slide_left_to_right, R.anim.slide_in_right);
                fTrans.replace(R.id.main_fragment, lastFmFragment);
                fTrans.addToBackStack(null);
                fTrans.commit();
            }
        });
        cdd.show();
    }



    private void setDragEnableMode(boolean isListView){
        if (isListView && listAdapter!=null) {
            listAdapter.updateVisibility(true);
            listAdapter.notifyDataSetChanged();
        }
    }

    private void setDragDisableMode(boolean isListView){
        if (isListView && listAdapter!=null) {
            listAdapter.updateVisibility(false);
            listAdapter.notifyDataSetChanged();
        }
    }

    private void showMenuItems(PopupMenu popupMenu){
        if ((mDragListView.isDragEnabled())) {
            Settings.hideOption(R.id.enable_dragging, popupMenu);
            Settings.showOption(R.id.disable_dragging, popupMenu);
        } else {
            Settings.hideOption(R.id.disable_dragging, popupMenu);
            Settings.showOption(R.id.enable_dragging, popupMenu);
        }

        if (Settings.TRACKS_VIEW.equals(Settings.GRID_VIEW)) {
            Settings.hideOption(R.id.grid_view, popupMenu);
            Settings.showOption(R.id.list_view, popupMenu);
        } else {
            Settings.hideOption(R.id.list_view, popupMenu);
            Settings.showOption(R.id.grid_view, popupMenu);
        }
    }

    protected void showMenu(View v){
        PopupMenu popupMenu = new PopupMenu(getActivity(), v);
        popupMenu.inflate(R.menu.tracks_menu);
        final boolean view = Settings.TRACKS_VIEW.equals(Settings.GRID_VIEW);

        showMenuItems(popupMenu);

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.list_view:
                        settings.savePreference(Settings.APP_PREFERENCES_TRACKS_VIEW, Settings.LIST_VIEW);
                        Settings.TRACKS_VIEW = Settings.LIST_VIEW;
                        fill(list);
                        break;
                    case R.id.grid_view:
                        settings.savePreference(Settings.APP_PREFERENCES_TRACKS_VIEW, Settings.GRID_VIEW);
                        Settings.TRACKS_VIEW = Settings.GRID_VIEW;
                        fill(list);
                        break;
                    case R.id.disable_dragging:
                        mDragListView.setDragEnabled(false);
                        setDragDisableMode(!view);
                        break;
                    case R.id.enable_dragging:
                        mDragListView.setDragEnabled(true);
                        setDragEnableMode(!view);
                        break;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.enable_dragging).setVisible(mDragListView.isDragEnabled());
        menu.findItem(R.id.disable_dragging).setVisible(!mDragListView.isDragEnabled());
    }

    @Override
    public void onChildViewAttachedToWindow(View view) {
        listAdapter.itemAttached(view);
    }

    @Override
    public void onChildViewDetachedFromWindow(View view) {
        listAdapter.itemDetach(view);
    }

    @Override
    public Animation onCreateAnimation(int transit, final boolean enter, int nextAnim) {
        try {
            Animation nextAnimation = AnimationUtils.loadAnimation(getContext(), nextAnim);
            nextAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (enter) {
                        isAnimationEnded = true;
                        if (list != null) fill(list);
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            return nextAnimation;
        } catch (Resources.NotFoundException ignored){}
        return null;
    }
}
