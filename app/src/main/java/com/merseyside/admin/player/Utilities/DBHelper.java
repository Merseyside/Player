package com.merseyside.admin.player.Utilities;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Admin on 06.06.2016.
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "player.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_STREAMS_NAME = "stream";
    public static final String TABLE_PLAYLIST_NAME = "playlist";
    public static final String TABLE_MEMORY_NAME = "memory";
    public static final String TABLE_FAVOURITE_DIRS_NAME = "directories";

    public static final String ALL_TRACKS = "all_tracks";
    public static final String RATED = "highly_rated";
    public static final String MEGAMIX = "megamix_tracks";
    public static final String COMMENT = "commented_tracks";
    public static final String RECENTLY_ADDED = "recently_added";

    public static final String NAME_COLUMN = "name";
    public static final String URL_COLUMN = "url";
    public static final String PIC_COLUMN = "picture";
    public static final String FOLDER_COLUMN = "folder_url";

    public static final String FOLDER_NAME_COLUMN = "folder_name";
    public static final String FOLDER_PATH_COLUMN = "folder_path";

    public static final String SCRIPT_STREAM = "create table " + TABLE_STREAMS_NAME +
            " (_id integer primary key autoincrement, "
            + NAME_COLUMN + ", "
            + URL_COLUMN + ");";
    public static final String SCRIPT_PLAYLIST = "create table " + TABLE_PLAYLIST_NAME +
            " (_id integer primary key autoincrement, "
            + NAME_COLUMN + ", "
            + URL_COLUMN + ");";

    public static final String SCRIPT_MEMORY = "create table " + TABLE_MEMORY_NAME +
            " (_id integer primary key autoincrement, "
            + NAME_COLUMN + ", "
            + URL_COLUMN + ", "
            + FOLDER_COLUMN + ", "
            + PIC_COLUMN + ");";

    public static final String SCRIPT_FAVOURITE_DIRS = "create table " + TABLE_FAVOURITE_DIRS_NAME +
            " (_id integer primary key autoincrement, "
            + FOLDER_NAME_COLUMN + ", "
            + FOLDER_PATH_COLUMN + ");";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                    int version) {
        super(context, name, factory, version);
    }

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                    int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SCRIPT_PLAYLIST);
        db.execSQL(SCRIPT_STREAM);
        db.execSQL(SCRIPT_MEMORY);
        db.execSQL(SCRIPT_FAVOURITE_DIRS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
