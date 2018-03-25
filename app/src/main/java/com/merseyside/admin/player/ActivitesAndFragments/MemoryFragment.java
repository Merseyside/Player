package com.merseyside.admin.player.ActivitesAndFragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.merseyside.admin.player.AdaptersAndItems.PlaylistItemsAdapter;
import com.merseyside.admin.player.AdaptersAndItems.Track;
import com.merseyside.admin.player.Dialogs.EditDialog;
import com.merseyside.admin.player.MegamixLibrary.MegamixCreator;
import com.merseyside.admin.player.R;
import com.merseyside.admin.player.Utilities.DBHelper;
import com.merseyside.admin.player.Utilities.InfoForPlayer;
import com.merseyside.admin.player.Utilities.M3UParser;
import com.merseyside.admin.player.Dialogs.PlaylistsDialog;
import com.merseyside.admin.player.AdaptersAndItems.SQLItem;
import com.merseyside.admin.player.Utilities.MySnackbar;
import com.merseyside.admin.player.Utilities.PrintString;
import com.merseyside.admin.player.Utilities.Settings;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by Admin on 03.12.2016.
 */

public class MemoryFragment extends ChooseFragment {

    private PlaylistTracks tracks;
    private ProgressDialog progress;
    private Player_Fragment player_fragment;
    private ImageView header;
    private Settings settings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        table = DBHelper.TABLE_MEMORY_NAME;
        TYPE = Player_Fragment.Type.MEMORY;
        ParentFragment();
    }

    public void ParentFragment(){
        player_fragment = MainActivity.get_player_fragment();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        settings = new Settings(getActivity());

        header = (ImageView) getView().findViewById(R.id.memory_header);
        PrintString.printLog("lifeCycle", Settings.getScreenHeight() + " " + Settings.getScreenWidth());
        header.setImageBitmap(Settings.memory_header);
        super.onActivityCreated(savedInstanceState);
        header_textView = (TextView) getView().findViewById(R.id.memory_textview);
        settings.setTextViewFont(header_textView, null);

        menu = (ImageButton) getView().findViewById(R.id.settings);
        menu.setOnClickListener(this);

        fab = (FloatingActionButton) getView().findViewById(R.id.memory_add_button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.memory_add_button:
                        Intent intent = new Intent(getActivity(), FileChooserActivity.class);
                        intent.putExtra("table", table);
                        intent.putExtra("caller_activity", Settings.CALLER_ACTIVITY_ADD_FOLDERS);
                        startActivityForResult(intent, REQUEST_CODE);
                        break;
        }
            }
        });

        listView = (ListView) getView().findViewById(R.id.memory_listview);

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
                //Do some
                final SQLItem o = adapter.getItem(position);
                PlaylistsDialog cdd = new PlaylistsDialog(getActivity(), new PlaylistsDialog.MyDialogListener() {
                    @Override
                    public void userSelectedDelete() {
                        delete(o, position);
                    }

                    @Override
                    public void userSelectedEdit() {
                        startEditorActivity(o.getId(), "edit");
                    }

                    @Override
                    public void userSelectedPlay() {
                        playMemoryAndPlaylist(manager.getTracksFromPlaylistAndMemory(DBHelper.TABLE_MEMORY_NAME, o.getName()), o.getName());
                    }

                    @Override
                    public void userSelectedOrder() {
                        toOrder(o);
                    }

                    @Override
                    public void userSelectedMegamixCreator() {
                        megamix(o, position);
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
                                manager.refreshMemory(o);
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

        gridView = (GridView) getView().findViewById(R.id.gridView);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                SQLItem o = adapter.getItem(position);
                startTracksFragment(o.getId(), table, o.getPic());
            }
        });

        gridView.setLongClickable(true);
        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View v, final int position, long id) {
                //Do some
                final SQLItem o = adapter.getItem(position);
                PlaylistsDialog cdd = new PlaylistsDialog(getActivity(), new PlaylistsDialog.MyDialogListener() {
                    @Override
                    public void userSelectedDelete() {
                        delete(o, position);
                    }

                    @Override
                    public void userSelectedEdit() {
                        startEditorActivity(o.getId(), "edit");
                    }

                    @Override
                    public void userSelectedPlay() {
                        playMemoryAndPlaylist(manager.getTracksFromPlaylistAndMemory(DBHelper.TABLE_MEMORY_NAME, o.getName()), o.getName());
                    }

                    @Override
                    public void userSelectedOrder() {
                        toOrder(o);
                    }

                    @Override
                    public void userSelectedMegamixCreator() {
                        megamix(o, position);
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
                                manager.refreshMemory(o);
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

    private void delete(SQLItem item, int position){
        File file = new File(item.getUrl());
        if (file.delete()) {
            manager.deleteFromDBById(table, item.getId());
            adapter.removeItem(position);
            adapter.notifyDataSetChanged();
        }
    }

    private void toOrder(SQLItem item){
        ArrayList<Track> list = manager.getTracksFromPlaylistAndMemory(DBHelper.TABLE_MEMORY_NAME, item.getName());
        if (list.size()!=0) {
            ParentFragment();
            player_fragment.addOrder(list);
            new MySnackbar(getActivity(), listView, R.string.successfully_added).show();
        } else new MySnackbar(getActivity(), listView, R.string.folder_is_empty).show();
    }

    private void megamix(SQLItem item, int position){
        settings.freeMemory();
        ArrayList<Track> list = manager.getTracksFromPlaylistAndMemory(DBHelper.TABLE_MEMORY_NAME, item.getName());
        if (list.size()!= 0) {
            MegamixCreator megamixCreator = MainActivity.get_creator();
            megamixCreator.setTracks(list, position, DBHelper.TABLE_MEMORY_NAME, item.getName());
            fTrans = getActivity().getSupportFragmentManager().beginTransaction();
            if (Settings.ANIMATION) fTrans.setCustomAnimations(R.anim.slide_right_to_left, R.anim.slide_in_left,R.anim.slide_left_to_right, R.anim.slide_in_right);
            fTrans.replace(R.id.main_fragment, megamixCreator);
            fTrans.addToBackStack(null);
            fTrans.commit();
        }
    }

    private void playMemoryAndPlaylist(ArrayList<Track> list, String playlistName){
        fTrans = getActivity().getSupportFragmentManager().beginTransaction();
        if (Settings.ANIMATION) fTrans.setCustomAnimations(R.anim.slide_right_to_left, R.anim.slide_in_left,R.anim.slide_left_to_right, R.anim.slide_in_right);
        final InfoForPlayer ifp = new InfoForPlayer(TYPE, playlistName, list, 0);
        if (ifp.isValid()) {
            player_fragment.setInfo(ifp);
            fTrans.replace(R.id.main_fragment, player_fragment);
            fTrans.addToBackStack(null);
            fTrans.commit();
        } else new MySnackbar(getActivity(), fab, R.string.folder_is_empty, true).show();
    }

    @Override
    public void onStart() {
        super.onStart();
        fillView();
    }

    private void fillView() {
        list = manager.getItemsFromTable(DBHelper.TABLE_MEMORY_NAME);
        if (Settings.SORTING_ORDER) SQLItem.isAscendingSort(true);
        else SQLItem.isAscendingSort(false);
        Collections.sort(list);
        if (list.size() != 0) {
            if (Settings.MEMORY_VIEW.equals(Settings.LIST_VIEW)) {
                listView.setVisibility(View.VISIBLE);
                gridView.setVisibility(View.GONE);
                adapter = new PlaylistItemsAdapter(getActivity(), R.layout.playlist_list_view, list);
                listView.setAdapter(adapter);
            } else {
                listView.setVisibility(View.GONE);
                gridView.setVisibility(View.VISIBLE);
                adapter = new PlaylistItemsAdapter(getActivity(), R.layout.playlist_grid_view, list);
                gridView.setAdapter(adapter);
            }
        } else {
            Snackbar.make(listView, getResources().getString(R.string.no_folders), Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.choose_memory, null);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.settings:
                showMenu(view);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            final ArrayList<String> folder_name = data.getStringArrayListExtra("folder");
            if (resultCode == Activity.RESULT_OK) {
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                c = db.query(DBHelper.TABLE_MEMORY_NAME, null, null, null, null, null, null);

                progress = new ProgressDialog(getActivity(), R.style.DialogStyle);
                progress.setTitle(getString(R.string.please_wait));
                progress.setMessage(getString(R.string.applying_changes));
                progress.setCancelable(false);
                progress.show();
                new Thread(new Runnable() {
                    @Override
                    public void run()
                    {
                        if (c.moveToFirst()) {
                            do {
                                Integer urlColIndex = c.getColumnIndex(DBHelper.URL_COLUMN);
                                Integer folderColIndex = c.getColumnIndex(DBHelper.FOLDER_COLUMN);
                                String url = c.getString(urlColIndex);
                                String folder = c.getString(folderColIndex);
                                File path = new File(url);
                                File folder_file = new File(folder);
                                M3UParser plParser = new M3UParser(path, getActivity());
                                if (folder_name.contains(url)) plParser.addTracksFromFolder(folder_file, path);

                            } while (c.moveToNext());
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run()
                            {
                                progress.dismiss();
                            }
                        });
                    }
                }).start();
            }
        }
    }

    protected void startTracksFragment(String id, String table, String image_url) {
        HashMap<String, String> data = new HashMap<String, String>();
        fTrans = getActivity().getSupportFragmentManager().beginTransaction();
        if (Settings.ANIMATION) fTrans.setCustomAnimations(R.anim.slide_right_to_left, R.anim.slide_in_left,R.anim.slide_left_to_right, R.anim.slide_in_right);
        data.put("table", table);
        data.put("image", image_url);
        data.put("id", id);
        tracks = new PlaylistTracks();
        tracks.putArguments(data);
        fTrans.replace(R.id.main_fragment, tracks);
        fTrans.addToBackStack(null);
        fTrans.commit();
    }

    public void startEditorActivity(String id, String action){
        EditDialog dialog = new EditDialog(getActivity(), table, action, id, getResources(), new EditDialog.EditDialogListener() {
            @Override
            public void userSaved() {
                fillView();
            }
        });
        dialog.show();
    }

    private void showMenuItems(PopupMenu popupMenu){
        if (Settings.MEMORY_VIEW.equals(Settings.GRID_VIEW)) {
            Settings.hideOption(R.id.grid_view, popupMenu);
            Settings.showOption(R.id.list_view, popupMenu);
        } else {
            Settings.hideOption(R.id.list_view, popupMenu);
            Settings.showOption(R.id.grid_view, popupMenu);
        }

        if (Settings.SORTING_ORDER){
            Settings.showOption(R.id.descending, popupMenu);
            Settings.hideOption(R.id.ascending, popupMenu);
        } else {
            Settings.hideOption(R.id.descending, popupMenu);
            Settings.showOption(R.id.ascending, popupMenu);
        }
    }

    @Override
    protected void showMenu(View v){
        PopupMenu popupMenu = new PopupMenu(getActivity(), v);
        popupMenu.inflate(R.menu.fragment_menu);

        showMenuItems(popupMenu);

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.list_view:
                        settings.savePreference(Settings.APP_PREFERENCES_MEMORY_VIEW, Settings.LIST_VIEW);
                        Settings.MEMORY_VIEW = Settings.LIST_VIEW;
                        fillView();
                        break;
                    case R.id.grid_view:
                        settings.savePreference(Settings.APP_PREFERENCES_MEMORY_VIEW, Settings.GRID_VIEW);
                        Settings.MEMORY_VIEW = Settings.GRID_VIEW;
                        fillView();
                        break;
                    case R.id.ascending:
                        Settings.SORTING_ORDER = true;
                        settings.savePreference(Settings.APP_PREFERENCES_SORTING_ORDER, true);
                        fillView();
                        break;
                    case R.id.descending:
                        Settings.SORTING_ORDER = false;
                        settings.savePreference(Settings.APP_PREFERENCES_SORTING_ORDER, false);
                        fillView();
                        break;
                    case R.id.refresh:
                        progress = new ProgressDialog(getActivity(), R.style.DialogStyle);
                        progress.setTitle(getString(R.string.please_wait));
                        progress.setMessage(getString(R.string.applying_changes));
                        progress.setCancelable(false);
                        progress.show();

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                manager.refreshMemory();

                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progress.dismiss();
                                    }
                                });
                            }
                        }).start();
                        break;
                }
                return false;
            }
        });
        popupMenu.show();
    }
}
