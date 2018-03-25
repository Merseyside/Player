package com.merseyside.admin.player.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.merseyside.admin.player.ActivitesAndFragments.ChooseFragment;
import com.merseyside.admin.player.R;

/**
 * Created by Admin on 25.01.2017.
 */

public class AddPlaylistDialog extends Dialog implements View.OnClickListener{
    private Button save, add_foreign;
    private EditText editName;
    private SQLiteDatabase db;
    private String table, id, action;
    private PlaylistDialogListener playlistDialogListener;

    public interface PlaylistDialogListener {
        void addForeignPlaylist();
        void userSavedPlaylist(String filename);
    }


    public AddPlaylistDialog(Context context, String action, String table, String id, PlaylistDialogListener playlistDialogListener) {
        super(context);
        this.action = action;
        this.id = id;
        this.table = table;
        this.playlistDialogListener = playlistDialogListener;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.add_playlist_dialog);
        save = (Button) findViewById(R.id.AEP_save_playlist_button);
        save.setOnClickListener(this);
        editName = (EditText) findViewById(R.id.AEP_text_playlist_name);
        add_foreign = (Button) findViewById(R.id.addplaylist_add_button);
        add_foreign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playlistDialogListener.addForeignPlaylist();
                dismiss();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        db =  ChooseFragment.dbHelper.getWritableDatabase();

        switch(action)
        {
            case "edit":
            {
                try {
                    if (!id.isEmpty()) {
                        String selection = "_id = ?";
                        String[] selectionArgs = new String[]{id};
                        Cursor c = db.query(table, null, selection, selectionArgs, null, null, null);
                        if (c.moveToFirst()) {
                            do {
                                int nameColIndex = c.getColumnIndex("name");
                                editName.setText(c.getString(nameColIndex));
                            } while (c.moveToNext());
                        }
                        c.close();
                    }
                } catch (NullPointerException e) {

                }
                break;
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.AEP_save_playlist_button: {
                String filename = editName.getText().toString();
                if (!filename.equals("")) playlistDialogListener.userSavedPlaylist(filename);
                dismiss();
                break;
            }
        }
    }
}
