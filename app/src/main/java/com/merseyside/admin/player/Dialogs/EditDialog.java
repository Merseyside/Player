package com.merseyside.admin.player.Dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.merseyside.admin.player.ActivitesAndFragments.ChooseFragment;
import com.merseyside.admin.player.R;
import com.merseyside.admin.player.Utilities.DBHelper;
import com.merseyside.admin.player.Utilities.FileManager;
import com.merseyside.admin.player.Utilities.MySnackbar;
import com.merseyside.admin.player.Utilities.Settings;

/**
 * Created by Admin on 25.01.2017.
 */

public class EditDialog extends Dialog implements View.OnClickListener{

    private Settings settings;
    private Context context;

    private SQLiteDatabase db;
    private String table, action, id;

    private EditText editName, editUrl;
    private TextView url;
    private Button save;
    private EditDialogListener editDialogListener;
    private Resources resources;

    public interface EditDialogListener{
        public void userSaved();
    }

    public EditDialog(Context context, String table, String action, String id, Resources resources, EditDialogListener editDialogListener) {
        super(context);
        this.context = context;
        this.table = table;
        this.action = action;
        this.id = id;
        this.editDialogListener = editDialogListener;
        this.resources = resources;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(resources.getString(R.string.dialog_edit));
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.edit_dialog);
        settings = new Settings(context);

        editName = (EditText) findViewById(R.id.text_name);
        save = (Button)findViewById(R.id.save_button);
        save.setOnClickListener(this);
        editUrl = (EditText)findViewById(R.id.text_url);
        url = (TextView)findViewById(R.id.url_tv);
        if (table.equals(DBHelper.TABLE_PLAYLIST_NAME) ||  table.equals(DBHelper.TABLE_MEMORY_NAME)) {
            editUrl.setVisibility(View.GONE);
            url.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        db =  ChooseFragment.dbHelper.getWritableDatabase();
        switch(action)
        {
            case "add":
            {
                break;
            }

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
                                int urlColIndex = c.getColumnIndex("url");
                                editName.setText(c.getString(nameColIndex));
                                editUrl.setText(c.getString(urlColIndex));
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
        switch(view.getId())
        {
            case R.id.save_button:
            {
                String name = editName.getText().toString();
                String url = editUrl.getText().toString();
                FileManager manager = new FileManager(getContext());
                manager.saveStream(table, name, url, id, editUrl);
                new MySnackbar(context, editUrl, R.string.successfully_added).show();
                settings.hideKeyboard((Activity)context);
                editDialogListener.userSaved();
                dismiss();
                break;
            }
        }
    }
}
