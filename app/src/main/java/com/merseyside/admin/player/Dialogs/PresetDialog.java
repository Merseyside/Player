package com.merseyside.admin.player.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;

import com.merseyside.admin.player.AdaptersAndItems.PresetAdapter;
import com.merseyside.admin.player.R;
import com.merseyside.admin.player.Utilities.MySnackbar;

import java.util.ArrayList;

/**
 * Created by Admin on 24.01.2017.
 */

public class PresetDialog extends Dialog implements View.OnClickListener {
    public interface PresetDialogListener{
        void userPressedItem(int position);
    }

    private PresetDialogListener presetDialogListener;
    private ListView listView;
    private ArrayList<String> list;
    private PresetAdapter adapter;
    private Context context;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.preset_dialog);
        listView = (ListView) findViewById(R.id.choose_preset_listview);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                presetDialogListener.userPressedItem(i);
                dismiss();
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        if (list.size() != 0){
            adapter = new PresetAdapter(context, R.layout.preset_view, list);
            listView.setAdapter(adapter);
        } else {
            new MySnackbar(context, listView, R.string.no_playlists).show();
        }
    }

    public PresetDialog(Context context, ArrayList<String> list, PresetDialogListener presetDialogListener) {
        super(context);
        this.list = list;
        this.presetDialogListener = presetDialogListener;
        this.context = context;
    }

    @Override
    public void onClick(View view) {

    }
}
