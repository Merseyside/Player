package com.merseyside.admin.player.Utilities;

import com.merseyside.admin.player.ActivitesAndFragments.Player_Fragment;
import com.merseyside.admin.player.AdaptersAndItems.Track;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Admin on 05.12.2016.
 */

public class InfoForPlayer implements Serializable {
    private Player_Fragment.Type type;
    private ArrayList<Track> playlist;
    private int position;
    private String url;
    private String item;
    private boolean isShuffle;
    private boolean isLooping;

    public InfoForPlayer(Player_Fragment.Type t, String item, String url){
        type = t;
        this.url = url;
        this.item = item;
    }

    public InfoForPlayer(Player_Fragment.Type t, String item, ArrayList<Track> list, int position){
        type = t;
        playlist = list;
        this.position = position;
        this.item = item;
        setPositions();
    }

    public InfoForPlayer(Player_Fragment.Type t, String item, ArrayList<Track> list, int position, boolean isShuffle, boolean isLooping, String url){
        type = t;
        playlist = list;
        this.position = position;
        this.item = item;
        this.isShuffle = isShuffle;
        this.isLooping = isLooping;
        this.url = url;
        setPositions();
    }

    public String getItem() {
        return item;
    }

    public boolean isShuffle() {
        return isShuffle;
    }

    public void setShuffle(boolean shuffle) {
        isShuffle = shuffle;
    }

    public boolean isLooping() {
        return isLooping;
    }

    public void setLooping(boolean looping) {
        isLooping = looping;
    }

    private void setPositions(){
        int i = 0;
        try {
            for (Track track : playlist) {
                track.setPosition(i);
                i++;
            }
        }catch (NullPointerException e){

        }
    }

    public Player_Fragment.Type getType() {
        return type;
    }

    public ArrayList<Track> getPlaylist() {
        return playlist;
    }

    public int getCurrentTrack_position() {
        return position;
    }

    public String getURL() {
        return url;
    }

    public boolean isValid(){
        if (type != null){
            if (type == Player_Fragment.Type.PLAYLIST || type == Player_Fragment.Type.MEMORY){
                if (item == null || item.equals("")) return false;
                if (playlist == null || playlist.size()==0) return false;
                if (position >= playlist.size()) return false;
            }
            else if (type == Player_Fragment.Type.STREAM){
                if (url == null || url.equals("")) return false;
                if (item == null || item.equals("")) return false;
            }
            else return false;
        }
        else return false;
        return true;
    }
}
