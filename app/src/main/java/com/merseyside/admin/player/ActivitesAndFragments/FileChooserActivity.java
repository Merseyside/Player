package com.merseyside.admin.player.ActivitesAndFragments;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.merseyside.admin.player.AdaptersAndItems.DirectoriesItem;
import com.merseyside.admin.player.AdaptersAndItems.FileArrayAdapter;
import com.merseyside.admin.player.Dialogs.InfoDialog;
import com.merseyside.admin.player.R;
import com.merseyside.admin.player.Utilities.DBHelper;
import com.merseyside.admin.player.Utilities.ExternalStorage;
import com.merseyside.admin.player.AdaptersAndItems.Item;
import com.merseyside.admin.player.Utilities.FileManager;
import com.merseyside.admin.player.Utilities.MySnackbar;
import com.merseyside.admin.player.Utilities.Settings;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FileChooserActivity extends AppCompatActivity implements View.OnClickListener {

    private File currentDir;
    private FileArrayAdapter adapter;
    private ProgressDialog progress;
    private FileManager manager;

    static Integer DIRECTORY_ICON;
    static final Integer FILE_ICON = R.drawable.file;

    private ImageButton check_all, cancel, accept, up_button;
    private CheckBox favourite;
    private ListView lv;
    private TextView parent;
    Button multi_button;

    private Pattern pattern;
    private boolean isPattern;

    private RelativeLayout layout;

    public static boolean MULTI_CHOOSE_ENABLE = false;
    public static boolean MULTI_CHOOSE_ALLOWED = false;

    private Intent intent;
    ExternalStorage storage;
    private File sdCard, externalSdCard;

    private boolean isAdded = false;
    private Settings settings;
    private ArrayList<DirectoriesItem> favourites;
    ArrayList<String> folders = new ArrayList<>();

    private static final String ALLOWED_CHARACTERS ="0123456789qwertyuiopasdfghjklzxcvbnm";

    private static String getRandomString(final int sizeOfRandomString) {
        final Random random=new Random();
        final StringBuilder sb=new StringBuilder(sizeOfRandomString);
        for(int i=0;i<sizeOfRandomString;++i)
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        return sb.toString();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        intent = getIntent();
        isPattern = false;
        caller_activity(intent.getStringExtra("caller_activity"));

        setContentView(R.layout.file_chooser);
        settings = new Settings(getApplicationContext());
        DIRECTORY_ICON = settings.getAttributeId(settings.getThemeByString(), R.attr.theme_dependent_folder_icon);
        manager = new FileManager(getApplicationContext());

        layout = (RelativeLayout) findViewById(R.id.file_chooser_layout);
        layout.setBackground(getResources().getDrawable(settings.getAttributeId(settings.getThemeByString(), R.attr.theme_dependent_windowBackground)));

        lv = (ListView)findViewById(R.id.explorer_lv);
        lv.setItemsCanFocus(true);

        check_all = (ImageButton) findViewById(R.id.check_all);
        check_all.setOnClickListener(this);
        check_all.setColorFilter(settings.getColor(settings.getAttributeId(settings.getThemeByString(), R.attr.theme_dependent_tint_color)));

        if (!MULTI_CHOOSE_ALLOWED) check_all.setEnabled(false);
        check_all.setVisibility(View.GONE);

        cancel = (ImageButton) findViewById(R.id.cancel_but);
        cancel.setOnClickListener(this);
        cancel.setVisibility(View.GONE);
        cancel.setColorFilter(settings.getColor(settings.getAttributeId(settings.getThemeByString(), R.attr.theme_dependent_tint_color)));

        accept = (ImageButton) findViewById(R.id.accept_but);
        accept.setOnClickListener(this);
        accept.setVisibility(View.GONE);
        accept.setColorFilter(settings.getColor(settings.getAttributeId(settings.getThemeByString(), R.attr.theme_dependent_tint_color)));

        multi_button = (Button) findViewById(R.id.multi_button);
        multi_button.setOnClickListener(this);
        switch(this.intent.getStringExtra("caller_activity")){
            case Settings.CALLER_ACTIVITY_ADD_TRACKS_TO_PLAYLIST:
                multi_button.setText(getString(R.string.choose_tracks));
                break;

            case Settings.CALLER_ACTIVITY_ADD_FOLDERS:
                multi_button.setText(getString(R.string.choose_folders));
                break;

            default:
                multi_button.setVisibility(View.GONE);
                break;
        }

        up_button = (ImageButton) findViewById(R.id.up_but);
        up_button.setOnClickListener(this);

        favourite = (CheckBox) findViewById(R.id.favourite_cb);
        favourite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                try {
                    if (isChecked) {
                        if (manager.addFavouriteToDB(currentDir.getName(), currentDir.getPath()))
                        new MySnackbar(getApplicationContext(), lv, R.string.successfully_added).show();
                    } else {
                        if (manager.removeFavouriteFromDB(currentDir.getPath()))
                        new MySnackbar(getApplicationContext(), lv, R.string.successfully_removed).show();
                    }
                }catch (NullPointerException e){}
            }
        });

        storage = new ExternalStorage();

        parent = (TextView) findViewById(R.id.parent_directory);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Item o = adapter.getItem(position);
                if (!MULTI_CHOOSE_ENABLE) {
                    if (o.getImage().equals(DIRECTORY_ICON)) {
                        currentDir = new File(o.getPath());
                        fill(currentDir);
                    } else {
                        onFileClick(o);
                    }
                } else {
                    CheckBox cb = (CheckBox) view.findViewById(R.id.FAA_checkBox);

                    if (cb.isChecked()) cb.setChecked(false);
                    else cb.setChecked(true);

                    adapter.changeSelected(position);
                }
            }
        });

        lv.setLongClickable(true);
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
                //Do some
                if (MULTI_CHOOSE_ALLOWED) {
                    EnableMultiChoose();
                    CheckBox cb = (CheckBox)v.findViewById(R.id.FAA_checkBox);
                    cb.setChecked(true);
                    adapter.setSelected(position);
                }
                return true;
            }
        });

        if (Settings.isExternalStorageReadable()) {
            externalSdCard = Settings.getExternalLocation();
            sdCard = Settings.getSdcardLocation();

            fillStart();
        }

        InfoDialog dialog;
        ArrayList<String> info = new ArrayList<>();
        if (this.intent.getStringExtra("caller_activity").equals(Settings.CALLER_ACTIVITY_ADD_EXTERNAL_PLAYLIST)&&Settings.PLAYLIST_INFO_DIALOG) {
            info.add(getString(R.string.add_playlist_dialog_information));
            dialog = new InfoDialog(this, getString(R.string.information), info, true);
            dialog.setInfoDialogListener(new InfoDialog.InfoDialogListener() {
                @Override
                public void checkboxClicked(boolean isChecked) {
                    if (isChecked) Settings.PLAYLIST_INFO_DIALOG = false;
                    else Settings.PLAYLIST_INFO_DIALOG = true;
                    settings.savePreference(Settings.APP_PREFERENCES_SHOW_PLAYLIST_INFO_DIALOG, !isChecked);
                }
            });
            dialog.show();
        } else if (Settings.FOLDERS_INFO_DIALOG){
            info.add(getString(R.string.add_folders_dialog_information));
            dialog = new InfoDialog(this, getString(R.string.information), info, true);
            dialog.setInfoDialogListener(new InfoDialog.InfoDialogListener() {
                @Override
                public void checkboxClicked(boolean isChecked) {
                    if (isChecked) Settings.FOLDERS_INFO_DIALOG = false;
                    else Settings.FOLDERS_INFO_DIALOG = true;
                    settings.savePreference(Settings.APP_PREFERENCES_SHOW_FOLDERS_INFO_DIALOG, !isChecked);
                }
            });
            dialog.show();
        }

    }

    private void fillStart(){
        ArrayList<Item> dir = new ArrayList<Item>();
        dir.add(new Item("SDCARD", null, null, externalSdCard.getAbsolutePath(), DIRECTORY_ICON));
        dir.add(new Item("PHONE", null, null, sdCard.getAbsolutePath(), DIRECTORY_ICON));
        favourites = manager.getFavouriteDirectories();
        for (DirectoriesItem item : favourites){
            dir.add(new Item(item.getName(), null, null, item.getPath(), DIRECTORY_ICON));
        }
        up_button.setVisibility(View.GONE);
        parent.setVisibility(View.GONE);
        favourite.setVisibility(View.GONE);
        multi_button.setVisibility(View.GONE);
        adapter = new FileArrayAdapter(this, R.layout.file_view, dir);
        lv.setAdapter(adapter);
    }

    private String getInternalDirectoryPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    private void fill(File f) {
        this.setTitle(getString(R.string.current_dir) + " " + f.getName());
        ArrayList<Item> dir;
        if (f.getAbsolutePath().equals(getInternalDirectoryPath())) up_button.setVisibility(View.GONE);
        else {
            up_button.setVisibility(View.VISIBLE);
            parent.setVisibility(View.VISIBLE);
            if (MULTI_CHOOSE_ALLOWED) multi_button.setVisibility(View.VISIBLE);
            if (!currentDir.getPath().equals(externalSdCard.getAbsolutePath()) && !currentDir.getPath().equals(sdCard.getAbsolutePath())){
                if (manager.findFavouriteDirectory(currentDir.getPath())) favourite.setChecked(true);
                else favourite.setChecked(false);
                favourite.setVisibility(View.VISIBLE);
            }
            else {
                favourite.setChecked(false);
                favourite.setVisibility(View.GONE);
            }
        }
        dir = getListFilesFromPath(f);
        adapter = new FileArrayAdapter(this, R.layout.file_view, dir);
        lv.setAdapter(adapter);
    }

    private void onFileClick(Item o) {
        Intent intent = new Intent();
        intent.putExtra("GetPath", currentDir.toString());
        intent.putExtra("GetFileName", o.getName());
        if (!this.intent.getStringExtra("caller_activity").equals(Settings.CALLER_ACTIVITY_ADD_EXTERNAL_PLAYLIST)) PlaylistTracks.files.add(o);
        setResult(RESULT_OK, intent);
        finish();
    }

    public void EnableMultiChoose() {
        if (MULTI_CHOOSE_ALLOWED)
        {
            accept.setVisibility(View.VISIBLE);
            cancel.setVisibility(View.VISIBLE);
            check_all.setVisibility(View.VISIBLE);
            up_button.setVisibility(View.GONE);
            parent.setVisibility(View.GONE);
            favourite.setVisibility(View.GONE);
            multi_button.setVisibility(View.GONE);
            MULTI_CHOOSE_ENABLE = true;
            for (int i = 0; i < lv.getChildCount(); i++) {
                View view = lv.getChildAt(i);
                view.findViewById(R.id.FAA_fd_Icon1).setVisibility(View.GONE);
                view.findViewById(R.id.FAA_checkBox).setVisibility(View.VISIBLE);
            }
        }
        return;
    }

    public void DisableMultiChoose() {
        for (int i = 0; i < lv.getChildCount(); i++) {
            View view = lv.getChildAt(i);
            view.findViewById(R.id.FAA_fd_Icon1).setVisibility(View.VISIBLE);
            view.findViewById(R.id.FAA_checkBox).setVisibility(View.GONE);
            CheckBox cb = (CheckBox) view.findViewById(R.id.FAA_checkBox);
            cb.setChecked(false);

        }
        adapter.misCheckAll();
        MULTI_CHOOSE_ENABLE = false;
        accept.setVisibility(View.GONE);
        cancel.setVisibility(View.GONE);
        check_all.setVisibility(View.GONE);
        up_button.setVisibility(View.VISIBLE);
        parent.setVisibility(View.VISIBLE);
        multi_button.setVisibility(View.VISIBLE);
        try {
            if (currentDir != null && !currentDir.getPath().equals(externalSdCard.getAbsolutePath()) && !currentDir.getPath().equals(sdCard.getAbsolutePath()))
                favourite.setVisibility(View.VISIBLE);
        } catch (NullPointerException ignored){}
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.check_all:
                if (MULTI_CHOOSE_ALLOWED) {
                    EnableMultiChoose();
                    adapter.checkAll();
                    for (int i = 0; i < lv.getChildCount(); i++) {
                        View view = lv.getChildAt(i);
                        CheckBox cb = (CheckBox) view.findViewById(R.id.FAA_checkBox);
                        cb.setChecked(true);
                    }
                }
                break;
            case R.id.cancel_but:
                DisableMultiChoose();
                break;
            case R.id.up_but:
                try {
                    if ((currentDir.getName().equalsIgnoreCase(sdCard.getName())) || (currentDir.getName().equalsIgnoreCase(externalSdCard.getName()))) {
                        fillStart();
                    } else {
                        currentDir = new File(currentDir.getParent());
                        fill(currentDir);
                    }
                } catch (NullPointerException ignored){}
                break;

            case R.id.accept_but: {
                final List<Item> buf = new ArrayList<>();
                progress = new ProgressDialog(this, R.style.DialogStyle);
                progress.setTitle(getString(R.string.please_wait));
                progress.setMessage(getString(R.string.applying_changes));
                progress.setCancelable(false);
                progress.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for(Item item : adapter.getAll()){
                            if (item.getSelected()){
                                if (intent.getStringExtra("caller_activity").equals(Settings.CALLER_ACTIVITY_ADD_FOLDERS)) {
                                    if (checkFolderExistsMusic(new File(item.getPath()))) buf.add(item);
                                }
                                else {
                                    if (item.getImage().equals(DIRECTORY_ICON)) {
                                        buf.addAll(getListFilesFromPath(new File(item.getPath())));
                                    } else buf.add(item);
                                }
                            }
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run()
                            {
                                sendListToFragment(buf);
                                DisableMultiChoose();
                                buf.clear();
                                progress.dismiss();
                            }
                        });
                    }
                }).start();

                break;
            }

            case R.id.multi_button:
                EnableMultiChoose();
                break;
        }
    }

    public void caller_activity(String activity) {
        String PATTERN_COMPILE;
        if (activity.equals(Settings.CALLER_ACTIVITY_ADD_EXTERNAL_PLAYLIST)) {
            PATTERN_COMPILE = "^.+\\.m3u$";
            MULTI_CHOOSE_ALLOWED = false;
        }
        else if (activity.equals(Settings.CALLER_ACTIVITY_ADD_TRACKS_TO_PLAYLIST)){
            PATTERN_COMPILE = Settings.FORMATS_PATTERN;
            MULTI_CHOOSE_ALLOWED = true;
        }
        else {
            MULTI_CHOOSE_ALLOWED = true;
            return;
        }
        isPattern = true;
        pattern = Pattern.compile(PATTERN_COMPILE);
    }

    private ArrayList<Item> getListFilesFromPath(File file) {
        File[] dirs = file.listFiles();

        ArrayList<Item> dir = new ArrayList<Item>();
        ArrayList<Item> fls = new ArrayList<Item>();
        try {
            for (File ff : dirs) {
                Date lastModDate = new Date(ff.lastModified());
                DateFormat formater = DateFormat.getDateTimeInstance();
                String date_modify = formater.format(lastModDate);
                if (ff.isDirectory()) {
                    File[] fbuf = ff.listFiles();
                    int buf = 0;
                    if (fbuf != null) {
                        buf = fbuf.length;
                    } else buf = 0;
                    String num_item = String.valueOf(buf);
                    if (buf % 10 == 1) num_item = num_item + " " + getString(R.string.item);
                    else num_item = num_item + " " + getString(R.string.items);

                    dir.add(new Item(ff.getName(), num_item, date_modify, ff.getAbsolutePath(), DIRECTORY_ICON));
                } else {
                    if (isPattern) {
                        Matcher matcher = pattern.matcher(ff.getName());
                        if (matcher.matches())
                            fls.add(new Item(ff.getName(), ff.length() + getString(R.string.byte1), date_modify, ff.getAbsolutePath(), FILE_ICON));
                    }
                }
            }
        } catch (Exception ignored) {}
        Collections.sort(dir);
        Collections.sort(fls);
        if (MULTI_CHOOSE_ENABLE) dir.clear();
        dir.addAll(fls);
        return dir;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    @Override
    public void onBackPressed() {
        if (isAdded)
            setResult(RESULT_OK, intent);
        else setResult(RESULT_CANCELED, intent);
        DisableMultiChoose();
         super.onBackPressed();
    }

    private boolean checkFolderExistsMusic(File file){
        File[] dirs = file.listFiles();
        String PATTERN_COMPILE = Settings.FORMATS_PATTERN;
        Pattern pattern;
        Matcher matcher;

        pattern = Pattern.compile(PATTERN_COMPILE);
        try {
            for (File ff : dirs) {
                matcher = pattern.matcher(ff.getName());
                if (matcher.matches()){
                    return true;
                }
            }
        }
        catch(Exception ignored) {}
        return false;
    }

    private void sendListToFragment(List<Item> buf) {
        if (intent.getStringExtra("caller_activity").equals(Settings.CALLER_ACTIVITY_ADD_TRACKS_TO_PLAYLIST)) {
            PlaylistTracks.files.addAll(buf);
            Toast.makeText(this, getResources().getString(R.string.added) + " " + buf.size() + " " +
                    getResources().getString(R.string.tracks_file), Toast.LENGTH_SHORT).show();
            if (buf.size()>0) isAdded = true;
            return;
        }
        else if (intent.getStringExtra("caller_activity").equals(Settings.CALLER_ACTIVITY_ADD_FOLDERS)) {
            SQLiteDatabase db =  MemoryFragment.dbHelper.getWritableDatabase();
            for(Item o : buf) {
                isAdded = true;
                String path = findCover(new File(o.getPath()));
                String path_to_playlist;
                File file  = new File(getApplicationContext().getFilesDir(), o.getName() + ".m3u");
                if (file.exists()){
                    file = new File(getApplicationContext().getFilesDir(), o.getName() + "_" + getRandomString(2) + ".m3u");
                }
                try {
                    file.createNewFile();
                    path_to_playlist = file.getAbsolutePath();
                } catch (IOException e) {
                    Toast.makeText(this, "Can't add folder. Check permissions", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_CANCELED, intent);
                    e.printStackTrace();
                    return;
                }
                ContentValues cv = new ContentValues();
                cv.put(DBHelper.NAME_COLUMN, o.getName());
                cv.put(DBHelper.URL_COLUMN, path_to_playlist);
                cv.put(DBHelper.PIC_COLUMN, path);
                cv.put(DBHelper.FOLDER_COLUMN, o.getPath());
                db.insert(DBHelper.TABLE_MEMORY_NAME, null, cv);
                folders.add(path_to_playlist);
            }
            Toast.makeText(this, getResources().getString(R.string.added) + " " +
                    buf.size() + " " + getResources().getString(R.string.memory), Toast.LENGTH_SHORT).show();
            intent.putExtra("folder", folders);
            setResult(RESULT_OK, intent);
        }
    }

    private String findCover(File file) {
        File[] dirs = file.listFiles();
        for (File ff : dirs) {
            try {
                String path = ff.getAbsolutePath();
                MediaMetadataRetriever retriver = new MediaMetadataRetriever();
                retriver.setDataSource(path);
                byte[] data = retriver.getEmbeddedPicture();
                if (data != null) {
                    return path;
                }
            } catch (Exception ignored) {}
        }
        return "";
    }
}

