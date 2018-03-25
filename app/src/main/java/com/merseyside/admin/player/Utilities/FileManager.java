package com.merseyside.admin.player.Utilities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.view.View;
import com.merseyside.admin.player.ActivitesAndFragments.Player_Fragment;
import com.merseyside.admin.player.AdaptersAndItems.DirectoriesItem;
import com.merseyside.admin.player.AdaptersAndItems.SQLItem;
import com.merseyside.admin.player.AdaptersAndItems.Track;
import com.merseyside.admin.player.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Admin on 06.12.2016.
 */

public class FileManager {
    private DBHelper dbHelper;
    private Context context;
    private M3UParser plParser;
    public FileManager(Context context){
        this.context = context;
        dbHelper = new DBHelper(context);
    }
    public ArrayList<SQLItem> getItemsFromTable(String table) throws SQLiteException{
        if (table.equals(DBHelper.TABLE_MEMORY_NAME)) {
            return getItemsFromMemory();
        }
        else {
            Player_Fragment.Type type;
            if (table.equals(DBHelper.TABLE_STREAMS_NAME)) type = Player_Fragment.Type.STREAM;
            else type = Player_Fragment.Type.PLAYLIST;
            return getItemsFromPlaylistAndStream(table, type);
        }
    }

    public int getCountOfItems(String table){
        return getItemsFromTable(table).size();
    }

    public void refreshMemory(){
        ArrayList<SQLItem> items = getItemsFromTable(DBHelper.TABLE_MEMORY_NAME);
        for (SQLItem item : items){
            refreshItem(DBHelper.TABLE_MEMORY_NAME, item);
        }
    }

    public void refreshMemory(SQLItem item){
        refreshItem(DBHelper.TABLE_MEMORY_NAME, item);
    }

    public void refreshPlaylist(SQLItem item){
        ArrayList<Track> tracks = getTracksFromPlaylistAndMemory(DBHelper.TABLE_PLAYLIST_NAME, item.getName());
        boolean changed = false;
        for (Track track : tracks){
            File file = new File(track.getPath());
            if (!file.exists()) {
                tracks.remove(track);
                changed = true;
            }
        }
        if (changed) {
            plParser = new M3UParser(new File(item.getUrl()), context);
            plParser.changePlaylist(tracks);
        }
    }

    private void refreshItem(String table, SQLItem item){
        ArrayList<Track> tracks = getTracksFromPlaylistAndMemory(table, item.getName());
        plParser = new M3UParser(new File(item.getUrl()), context);
        plParser.addTracksFromFolder(new File(item.getPlaylist()), new File(item.getUrl()));
        ArrayList<SQLItem> items = new ArrayList<>();
        items.add(item);
        ArrayList<Track> tracks2 = getTracks(items, item.getName());
        ArrayList<Track> tracks3 = new ArrayList<>();
        try {
            for (Track track : tracks2) {
                boolean found = false;
                for (Track track1 : tracks) {
                    if (track1.getPath().equals(track.getPath())) {
                        tracks3.add(track1);
                        tracks.remove(track1);
                        found = true;
                        break;
                    } else {
                        tracks3.add(track);
                        found = true;
                        break;
                    }
                }
                if (!found) tracks3.add(track);
            }
        } catch (NullPointerException ignored){}
        plParser.changePlaylist(tracks3);
    }

    private ArrayList<SQLItem> getItemsFromPlaylistAndStream(String tableName, Player_Fragment.Type TYPE) throws SQLiteException{
        ArrayList<SQLItem> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c;
        c = db.query(tableName, null, null, null, null, null, null);
        if (c.moveToFirst()) {
            // определяем номера столбцов по имени в выборке
            do {
                SQLItem o;
                Integer idColIndex = c.getColumnIndex("_id");
                Integer nameColIndex = c.getColumnIndex(DBHelper.NAME_COLUMN);
                Integer urlColIndex = c.getColumnIndex(DBHelper.URL_COLUMN);
                o = new SQLItem(c.getString(idColIndex), c.getString(nameColIndex), c.getString(urlColIndex), TYPE);
                list.add(o);

            } while (c.moveToNext());
        }
        c.close();
        return list;
    }
    private ArrayList<SQLItem> getItemsFromMemory() {
        ArrayList<SQLItem> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(DBHelper.TABLE_MEMORY_NAME, null, null, null, null, null, null);
        if (c.moveToFirst()) {
            do {
                SQLItem o;
                Integer idColIndex = c.getColumnIndex("_id");
                Integer nameColIndex = c.getColumnIndex(DBHelper.NAME_COLUMN);
                Integer urlColIndex = c.getColumnIndex(DBHelper.URL_COLUMN);
                Integer picColIndex = c.getColumnIndex(DBHelper.PIC_COLUMN);
                Integer playlistUrlColIndex = c.getColumnIndex(DBHelper.FOLDER_COLUMN);
                File file = new File(c.getString(playlistUrlColIndex));
                if (!file.exists()){
                    deleteFromDBById(DBHelper.TABLE_MEMORY_NAME, c.getString(idColIndex));
                }
                o = new SQLItem(c.getString(idColIndex), c.getString(nameColIndex), c.getString(urlColIndex), Player_Fragment.Type.MEMORY, c.getString(picColIndex), c.getString(playlistUrlColIndex));
                list.add(o);
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    public ArrayList<Track> getTracksFromPlaylistAndMemory(String table, String folder) throws NullPointerException{
            ArrayList<SQLItem> sql = getItemsFromTable(table);
            return getTracks(sql, folder);
    }

    private ArrayList<Track> getTracks(ArrayList<SQLItem> sql, String folder) throws NullPointerException{
        try {
            for (SQLItem item : sql) {
                if (item.getName().equals(folder)) {
                    File file = new File(item.getUrl());
                    if (!file.exists()) throw new NullPointerException();
                    plParser = new M3UParser(file, context);
                    return new ArrayList<>(plParser.getTracksList());
                }
            }
        }catch(NullPointerException e){
            e.printStackTrace();
        }
        return null;
    }

    public void saveStream(String table, String name, String url, String id, View editUrl){
        ContentValues cv = new ContentValues();
        SQLiteDatabase db =  dbHelper.getWritableDatabase();
        cv.put("name", name);
        if (table.equals(DBHelper.TABLE_STREAMS_NAME)){
            Pattern pattern;
            Matcher matcher;
            String PATTERN_COMPILE = "^https?://.+\\..+$";
            pattern = Pattern.compile(PATTERN_COMPILE);
            matcher = pattern.matcher(url);
            if (!matcher.matches()) {
                new MySnackbar(context, editUrl, R.string.url_not_matches, true).show();
                return;
            }
            cv.put("url", url);
        }
        if (name.equals("")){
            new MySnackbar(context, editUrl, R.string.name_empty).show();
            return;
        }
        PrintString.printLog("Stream", name + " " + url + " id = '" + id + "'");
        try {
            if (id.equals("")) {
                db.insert(table, null, cv);
            }
            else db.update(table, cv, "_id = ?", new String[]{String.valueOf(id)});
        }
        catch(NullPointerException e)
        {
            PrintString.printLog("Stream", "tut");
        }
    }

    public String getStreamURL(String name){
        ArrayList<SQLItem> items = getItemsFromPlaylistAndStream(DBHelper.TABLE_STREAMS_NAME, Player_Fragment.Type.STREAM);
        for (SQLItem item : items){
            if (item.getName().equals(name)) return item.getUrl();
        }
        return null;
    }

    public Bitmap getCover(String image_url) throws IllegalArgumentException{
        MediaMetadataRetriever retriver = new MediaMetadataRetriever();
        Bitmap bmp;
        try {
            retriver.setDataSource(image_url);
            byte[] data = retriver.getEmbeddedPicture();
            bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
        }catch(NullPointerException e){
            return null;
        }
        catch (IllegalArgumentException e){
            return null;
        }
        return bmp;
    }

    public void deleteTrack(Track track){
        File file = new File(track.getPlaylistPath());
        M3UParser parser = new M3UParser(file, context);
        parser.deleteTrack(track);
    }

    public ArrayList<DirectoriesItem> getFavouriteDirectories(){
        ArrayList<DirectoriesItem> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(DBHelper.TABLE_FAVOURITE_DIRS_NAME, null, null, null, null, null, null);
        if (c.moveToFirst()) {
            do {
                DirectoriesItem o;
                Integer idColIndex = c.getColumnIndex("_id");
                Integer nameColIndex = c.getColumnIndex(DBHelper.FOLDER_NAME_COLUMN);
                Integer urlColIndex = c.getColumnIndex(DBHelper.FOLDER_PATH_COLUMN);
                o = new DirectoriesItem(c.getString(idColIndex), c.getString(nameColIndex), c.getString(urlColIndex));
                list.add(o);
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    public boolean addFavouriteToDB(String name, String path){
        ArrayList<DirectoriesItem> items = getFavouriteDirectories();
        boolean found = false;
        for (DirectoriesItem item : items){
            if (path.equals(item.getPath())){
                found = true;
                break;
            }
        }
        if (found) return false;
        else {
            SQLiteDatabase db =  dbHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(DBHelper.FOLDER_NAME_COLUMN, name);
            cv.put(DBHelper.FOLDER_PATH_COLUMN, path);
            db.insert(DBHelper.TABLE_FAVOURITE_DIRS_NAME, null, cv);
        }
        return true;
    }

    public boolean removeFavouriteFromDB(String path){
        ArrayList<DirectoriesItem> items = getFavouriteDirectories();
        boolean found = false;
        String id = "";
        for (DirectoriesItem item : items){
            if (path.equals(item.getPath())){
                found = true;
                id = item.getId();
                break;
            }
        }
        if (!found) return false;
        else {
            SQLiteDatabase db =  dbHelper.getWritableDatabase();
            db.delete(DBHelper.TABLE_FAVOURITE_DIRS_NAME, "_id = " + String.valueOf(id), null);
        }
        return true;
    }

    public boolean findFavouriteDirectory(String path){
        ArrayList<DirectoriesItem> items = getFavouriteDirectories();
        for (DirectoriesItem item : items){
            if (path.equals(item.getPath())) return true;
        }
        return false;
    }

    public ArrayList<Track> getAllTracks(){
        ArrayList<Track> list = new ArrayList<>();
        ArrayList<SQLItem> memory = getItemsFromTable(DBHelper.TABLE_MEMORY_NAME);
        for (SQLItem item : memory){
            try {
                list.addAll(getTracksFromPlaylistAndMemory(DBHelper.TABLE_MEMORY_NAME, item.getName()));
            } catch(NullPointerException ignored){}
        }
        ArrayList<SQLItem> playlists = getItemsFromTable(DBHelper.TABLE_PLAYLIST_NAME);
        for (SQLItem item : playlists){
            try {
                list.addAll(getTracksFromPlaylistAndMemory(DBHelper.TABLE_PLAYLIST_NAME, item.getName()));
            } catch (NullPointerException ignored){}
        }
        return list;
    }

    public void setTrack(Track track) {
        File file = new File(track.getPlaylistPath());

        M3UParser parser = new M3UParser(file, context);
        ArrayList<Track> list = parser.getTracksList();
        PrintString.printLog("MegamixCreator", track.getPlaylistPath() + "Size: " + list.size());
        for (int i = 0; i<list.size();i++){
            if (track.getPath().equals(list.get(i).getPath())){
                list.set(i, track);
                PrintString.printLog("MegamixCreator", "found");
                break;
            }
        }
        parser.changePlaylist(list);
    }

    public boolean setPlaylistAndFolder(String table, String name, ArrayList<Track> list){
        ArrayList<SQLItem> items;
        items = getItemsFromTable(table);
        for (int i = 0; i<items.size();i++){
            if (items.get(i).getName().equals(name)){
                M3UParser parser = new M3UParser(new File(items.get(i).getUrl()), context);
                return parser.changePlaylist((ArrayList<Track>) list.clone());
            }
        }
        return false;
    }

    public ArrayList<Track> getRatedTracks(){
        ArrayList<Track> list = getAllTracks();
        ArrayList<Track> result_list = new ArrayList<>();
        for (Track track : list){
            if (track.getRating() != 0) result_list.add(track);
        }
        Track.setSortBy(Track.RATING);
        Collections.sort(result_list);
        return result_list;
    }

    public ArrayList<Track> getMegamixTracks(){
        ArrayList<Track> list = getAllTracks();
        ArrayList<Track> result_list = new ArrayList<>();
        for (Track track : list){
            if (track.isMegamixTrack()) result_list.add(track);
        }
        return result_list;
    }

    public ArrayList<Track> getCommentedTracks(){
        ArrayList<Track> list = getAllTracks();
        ArrayList<Track> result_list = new ArrayList<>();
        for (Track track : list){
            if (!track.getComment().equals("null")) result_list.add(track);
        }
        return result_list;
    }

    public ArrayList<Track> getRecentlyAddedTracks(){
        ArrayList<Track> list = new ArrayList<>();
        ArrayList<SQLItem> items = getItemsFromMemory();
        for (SQLItem item : items){
            list.addAll(getTracksFromPlaylistAndMemory(DBHelper.TABLE_MEMORY_NAME, item.getName()));
        }

        ArrayList<Track> result_list = new ArrayList<>();
        for (Track track : list) {
            if (track.getDate() > Settings.getDayOfYear() - Settings.RECENTLY_ADDED) result_list.add(track);
        }
        Track.setSortBy(Track.DATE);
        Collections.sort(result_list);
        return result_list;
    }

    public void deleteFromDBById(String table, String id){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(table, "_id = " + String.valueOf(id), null);
    }
}
