package com.merseyside.admin.player.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;

import com.merseyside.admin.player.AdaptersAndItems.PlaylistItemsAdapter;
import com.merseyside.admin.player.AdaptersAndItems.SQLItem;
import com.merseyside.admin.player.R;
import com.merseyside.admin.player.Utilities.DBHelper;
import com.merseyside.admin.player.Utilities.FileManager;
import com.merseyside.admin.player.Utilities.MySnackbar;

import java.util.ArrayList;

/**
 * Created by Admin on 07.01.2017.
 */

public class AddToPlaylistDialog extends Dialog implements View.OnClickListener {
    private ListView listView;
    private ArrayList<SQLItem> list;
    private PlaylistItemsAdapter adapter;
    private FileManager manager;
    private Context context;

    private MyAddToPlaylistDialogListener myAddToPlaylistDialogListener;

    public interface MyAddToPlaylistDialogListener{
        public void userSelectedPlaylist(String name, String url);
    }

    public AddToPlaylistDialog(Context context, MyAddToPlaylistDialogListener myAddToPlaylistDialogListener) {
        super(context);
        this.context = context;
        this.myAddToPlaylistDialogListener = myAddToPlaylistDialogListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.add_to_playlist_dialog);
        manager = new FileManager(context);
        listView = (ListView)findViewById(R.id.choose_playlist_listview);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                SQLItem o = adapter.getItem(position);
                myAddToPlaylistDialogListener.userSelectedPlaylist(o.getName(), o.getUrl());
                dismiss();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        list = manager.getItemsFromTable(DBHelper.TABLE_PLAYLIST_NAME);
        if (list.size() != 0){
            adapter = new PlaylistItemsAdapter(context, R.layout.playlist_list_view, list);
            listView.setAdapter(adapter);
        } else {
            new MySnackbar(context, listView, R.string.no_playlists).show();
        }
    }

    @Override
    public void onClick(View view) {

    }
}
