package com.merseyside.admin.player.ActivitesAndFragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.merseyside.admin.player.AdaptersAndItems.PlaylistItemsAdapter;
import com.merseyside.admin.player.AdaptersAndItems.Track;
import com.merseyside.admin.player.Dialogs.AddPlaylistDialog;
import com.merseyside.admin.player.Dialogs.EditDialog;
import com.merseyside.admin.player.MegamixLibrary.MegamixCreator;
import com.merseyside.admin.player.R;
import com.merseyside.admin.player.Utilities.DBHelper;
import com.merseyside.admin.player.Utilities.FileManager;
import com.merseyside.admin.player.Dialogs.PlaylistsDialog;
import com.merseyside.admin.player.AdaptersAndItems.SQLItem;
import com.merseyside.admin.player.Utilities.InfoForPlayer;
import com.merseyside.admin.player.Utilities.MySnackbar;
import com.merseyside.admin.player.Utilities.PrintString;
import com.merseyside.admin.player.Utilities.Settings;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Admin on 03.12.2016.
 */

public class PlaylistFragment extends ChooseFragment {

    private PlaylistTracks tracks;
    private ImageView header;
    private Settings settings;
    private ProgressDialog progress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        table = DBHelper.TABLE_PLAYLIST_NAME;
        manager = new FileManager(getActivity());
        TYPE = Player_Fragment.Type.PLAYLIST;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.choose_playlist, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        settings = new Settings(getActivity());

        header = (ImageView) getView().findViewById(R.id.playlist_header);
        PrintString.printLog("lifeCycle", Settings.getScreenHeight() + " " + Settings.getScreenWidth());
        header.setImageBitmap(Settings.playlist_header);

        header_textView = (TextView) getView().findViewById(R.id.playlists_textview);
        settings.setTextViewFont(header_textView, null);

        fab = (FloatingActionButton) getView().findViewById(R.id.playlist_add_button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddPlaylistDialog dialog = new AddPlaylistDialog(getActivity(), "add", table, null, new AddPlaylistDialog.PlaylistDialogListener() {

                    @Override
                    public void addForeignPlaylist() {
                        Intent intent = new Intent(getActivity(), FileChooserActivity.class);
                        intent.putExtra("caller_activity",  Settings.CALLER_ACTIVITY_ADD_EXTERNAL_PLAYLIST);
                        startActivityForResult(intent, REQUEST_PATH);
                    }

                    @Override
                    public void userSavedPlaylist(String filename) {
                        save(filename, "");
                        fillListView();
                    }
                });
                dialog.show();
            }
        });
        listView = (ListView)getView().findViewById(R.id.playlist_listview);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                SQLItem o = adapter.getItem(position);
                startTracksFragment(o.getId(), table, o.getPic());
            }
        });

        listView.setLongClickable(true);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View v, final int position, long id) {
                final SQLItem o = adapter.getItem(position);
                PlaylistsDialog cdd=new PlaylistsDialog(getActivity(), new PlaylistsDialog.MyDialogListener() {
                    @Override
                    public void userSelectedDelete() {
                        File file = new File(o.getUrl());
                        file.delete();
                        SQLiteDatabase db = dbHelper.getWritableDatabase();
                        db.delete(table, "_id = " + String.valueOf(o.getId()), null);
                        adapter.removeItem(position);
                        adapter.notifyDataSetChanged();
                        listView.invalidateViews();
                    }
                    @Override
                    public void userSelectedEdit() {
                        startEditorActivity(o.getId(), "edit");
                    }

                    @Override
                    public void userSelectedPlay() {
                        playMemoryAndPlaylist(manager.getTracksFromPlaylistAndMemory(DBHelper.TABLE_PLAYLIST_NAME, o.getName()), o.getName());
                    }

                    @Override
                    public void userSelectedOrder() {
                        ArrayList<Track> list = manager.getTracksFromPlaylistAndMemory(DBHelper.TABLE_PLAYLIST_NAME, o.getName());
                        ParentFragment();
                        if (list != null && list.size()!=0) {
                            player_fragment.addOrder(list);
                            new MySnackbar(getActivity(), listView, R.string.successfully_added).show();
                        } else new MySnackbar(getActivity(), listView, R.string.playlist_is_empty).show();
                    }

                    @Override
                    public void userSelectedMegamixCreator() {
                        ArrayList<Track> list = manager.getTracksFromPlaylistAndMemory(DBHelper.TABLE_PLAYLIST_NAME, o.getName());
                        if (list != null && list.size()!= 0 ) {
                            for (Track track : list){
                                if (track.getType() == Track.INTERNET_TRACK) list.remove(track);
                            }
                            if (list.size() != 0){
                                MegamixCreator megamixCreator = MainActivity.get_creator();
                                megamixCreator.setTracks(list, 0,  DBHelper.TABLE_PLAYLIST_NAME, o.getName());
                                fTrans = getActivity().getSupportFragmentManager().beginTransaction();
                                if (Settings.ANIMATION) fTrans.setCustomAnimations(R.anim.slide_right_to_left, R.anim.slide_in_left,R.anim.slide_left_to_right, R.anim.slide_in_right);
                                fTrans.replace(R.id.main_fragment, megamixCreator);
                                fTrans.addToBackStack(null);
                                fTrans.commit();
                            } else new MySnackbar(getActivity(), listView, R.string.megamix_memory_only).show();
                        } else new MySnackbar(getActivity(), listView, R.string.playlist_is_empty).show();
                    }

                    @Override
                    public void userSelectedRefresh() {
                        progress = new ProgressDialog(getActivity(), R.style.DialogStyle);
                        progress.setTitle(getString(R.string.please_wait));
                        progress.setMessage(getString(R.string.applying_changes));
                        progress.setCancelable(false);
                        progress.show();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                manager.refreshPlaylist(o);
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progress.dismiss();
                                    }
                                });
                            }
                        }).start();
                    }
                });
                cdd.show();
                return true;
            }
        });
    }

    public void ParentFragment(){
        player_fragment = MainActivity.get_player_fragment();
    }

    private void playMemoryAndPlaylist(ArrayList<Track> list, String playlistName){
        ParentFragment();
        fTrans = getActivity().getSupportFragmentManager().beginTransaction();
        if (Settings.ANIMATION) fTrans.setCustomAnimations(R.anim.slide_right_to_left, R.anim.slide_in_left,R.anim.slide_left_to_right, R.anim.slide_in_right);
        final InfoForPlayer ifp = new InfoForPlayer(TYPE, playlistName, list, 0);
        if (ifp.isValid()) {
            player_fragment.setInfo(ifp);
            fTrans.replace(R.id.main_fragment, player_fragment);
            fTrans.commit();
        } else new MySnackbar(getActivity(), fab, R.string.folder_is_empty,  true).show();
    }

    private void startTracksFragment(String id, String table, String image_url) {
        HashMap<String, String> data = new HashMap<String, String>();
        fTrans = getActivity().getSupportFragmentManager().beginTransaction();
        if (Settings.ANIMATION) fTrans.setCustomAnimations(R.anim.slide_right_to_left, R.anim.slide_in_left,R.anim.slide_left_to_right, R.anim.slide_in_right);
        data.put("table",table);
        data.put("image",image_url);
        data.put("id", id);
        tracks = new PlaylistTracks();
        tracks.putArguments(data);
        fTrans.replace(R.id.main_fragment, tracks);
        fTrans.addToBackStack(null);
        fTrans.commit();
    }

    @Override
    public void onStart() {
        super.onStart();
        fillListView();
    }

    @Override
    protected void showMenu(View v) {

    }

    private void fillListView(){
        list = manager.getItemsFromTable(DBHelper.TABLE_PLAYLIST_NAME);
        if (list.size() != 0){
            adapter = new PlaylistItemsAdapter(getActivity(), R.layout.playlist_list_view, list);
            listView.setAdapter(adapter);
        } else {
            new MySnackbar(getActivity(), listView, R.string.no_playlists).show();
        }
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == REQUEST_PATH){
            if (resultCode == Activity.RESULT_OK) {
                String name = data.getStringExtra("GetFileName");
                String path = data.getStringExtra("GetPath");
                String editUrl = path + "/" + name;
                save(name, editUrl);
            }
        }
    }

    private void save(String name, String editUrl){
        SQLiteDatabase db =  ChooseFragment.dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        File file = null;
        ArrayList<SQLItem> items = manager.getItemsFromTable(DBHelper.TABLE_PLAYLIST_NAME);
        boolean found = false;
        for (SQLItem item : items){
            if (item.getName().equals(name)){
                found = true;
                break;
            }
        }
        if (!found) {

            if (editUrl == null || editUrl.equals("")) {
                if (Settings.checkFilenameValid(name)) {
                    Context context = getActivity();
                    file = new File(context.getFilesDir(), name + ".m3u");
                    try {
                        file.createNewFile();
                        editUrl = file.toString();
                    } catch (IOException ignored) {
                        new MySnackbar(getActivity(), listView, R.string.not_correct_playlist_name, true).show();
                        return;
                    }
                } else {
                    new MySnackbar(getActivity(), listView, R.string.not_correct_playlist_name, true).show();
                    return;
                }
            }
            cv.put("name", name);
            cv.put("url", editUrl);
            db.insert(table, null, cv);
        } else new MySnackbar(getActivity(), listView, R.string.playlist_error).show();
    }

    public void startEditorActivity(String id, String action)
    {
        EditDialog dialog = new EditDialog(getActivity(), table, action, id, getResources(), new EditDialog.EditDialogListener() {
            @Override
            public void userSaved() {
                fillListView();
            }
        });
        dialog.show();
    }
}
