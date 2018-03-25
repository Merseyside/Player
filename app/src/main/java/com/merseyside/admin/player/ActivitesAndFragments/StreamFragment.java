package com.merseyside.admin.player.ActivitesAndFragments;

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
import com.merseyside.admin.player.Dialogs.EditDialog;
import com.merseyside.admin.player.R;
import com.merseyside.admin.player.Utilities.DBHelper;
import com.merseyside.admin.player.Utilities.InfoForPlayer;
import com.merseyside.admin.player.Dialogs.PlaylistsDialog;
import com.merseyside.admin.player.AdaptersAndItems.SQLItem;
import com.merseyside.admin.player.Utilities.MySnackbar;
import com.merseyside.admin.player.Utilities.PrintString;
import com.merseyside.admin.player.Utilities.Settings;

/**
 * Created by Admin on 03.12.2016.
 */

public class StreamFragment extends ChooseFragment {
    private ImageView header;
    private Settings settings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        table = DBHelper.TABLE_STREAMS_NAME;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.choose_stream, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        settings = new Settings(getActivity());

        header = (ImageView) getView().findViewById(R.id.stream_header);
        PrintString.printLog("lifeCycle", Settings.getScreenHeight() + " " + Settings.getScreenWidth());
        header.setImageBitmap(Settings.stream_header);

        header_textView = (TextView) getView().findViewById(R.id.stream_textview);
        settings.setTextViewFont(header_textView, null);

        player_fragment = MainActivity.get_player_fragment();
        fab = (FloatingActionButton) getView().findViewById(R.id.stream_add_button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fTrans = getActivity().getSupportFragmentManager().beginTransaction();
                switch(view.getId())
                {
                    case R.id.stream_add_button:
                    {
                        startEditorActivity("", "add");
                        break;
                    }
                    default:
                    {    }
                }
                fTrans.addToBackStack(null);
                fTrans.commit();
            }
        });
        listView = (ListView)getView().findViewById(R.id.stream_listview);
        getActivity().setTitle(R.string.stream);
        listView.setLongClickable(true);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View v, final int position, long id) {
                //Do some
                System.out.println("Здесь");
                final SQLItem o = adapter.getItem(position);
                PlaylistsDialog cdd=new PlaylistsDialog(getActivity(), new PlaylistsDialog.MyDialogListener() {
                    @Override
                    public void userSelectedDelete() {
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
                        playStream(position);
                    }

                    @Override
                    public void userSelectedOrder() {
                        new MySnackbar(getActivity(), listView, R.string.not_available, true).show();
                    }

                    @Override
                    public void userSelectedMegamixCreator() {
                        new MySnackbar(getActivity(), listView, R.string.not_available,  true).show();
                    }

                    @Override
                    public void userSelectedRefresh() {

                    }
                });
                cdd.show();
                return true;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                playStream(position);
            }
        });

    }

    private void playStream(int position){
        final SQLItem o = adapter.getItem(position);
        fTrans = getActivity().getSupportFragmentManager().beginTransaction();
        if (Settings.ANIMATION) fTrans.setCustomAnimations(R.anim.slide_up_to_down, R.anim.slide_in_right,R.anim.slide_right_to_left, R.anim.slide_up);
        InfoForPlayer infoForPlayer = new InfoForPlayer(Player_Fragment.Type.STREAM, o.getName(), o.getUrl());
        player_fragment.setInfo(infoForPlayer);
        fTrans.replace(R.id.main_fragment, player_fragment);
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

    private void fillListView() {
        list = manager.getItemsFromTable(DBHelper.TABLE_STREAMS_NAME);
        if (list.size() != 0){
            adapter = new PlaylistItemsAdapter(getActivity(), R.layout.playlist_list_view, list);
            listView.setAdapter(adapter);
        } else {
            new MySnackbar(getActivity(), listView, R.string.no_streams).show();
        }
    }

    @Override
    public void onClick(View view) {

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
