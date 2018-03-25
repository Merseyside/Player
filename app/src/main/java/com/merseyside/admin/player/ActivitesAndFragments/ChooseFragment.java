package com.merseyside.admin.player.ActivitesAndFragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.merseyside.admin.player.AdaptersAndItems.PlaylistItemsAdapter;
import com.merseyside.admin.player.Utilities.DBHelper;
import com.merseyside.admin.player.Utilities.FileManager;
import com.merseyside.admin.player.AdaptersAndItems.SQLItem;
import com.merseyside.admin.player.Utilities.Settings;

import java.util.ArrayList;

/**
 * Created by Admin on 03.12.2016.
 */

public abstract class ChooseFragment extends android.support.v4.app.Fragment implements View.OnClickListener{
    protected ListView listView;
    protected GridView gridView;
    public static DBHelper dbHelper;
    protected String table;
    protected Cursor c;
    //private GestureDetector mGestureDetector;
    protected Player_Fragment.Type TYPE;
    protected PlaylistItemsAdapter adapter;
    protected ArrayList<SQLItem> list;
    protected Button add;
    final public static int REQUEST_CODE = 1;
    final public static int REQUEST_PATH = 1;
    protected FragmentTransaction fTrans;
    protected FloatingActionButton fab;
    protected FileManager manager;
    protected TextView header_textView;
    protected ImageButton menu;

    protected Player_Fragment player_fragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        manager = new FileManager(getActivity());
        dbHelper = Settings.getDbHelper();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    protected abstract void showMenu(View v);
}
