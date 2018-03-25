package com.merseyside.admin.player.AdaptersAndItems;

import com.merseyside.admin.player.ActivitesAndFragments.Player_Fragment;

/**
 * Created by 1 on 06.08.2016.
 */
public class SQLItem implements Comparable {

    private String id;
    private String name;
    private Player_Fragment.Type type;
    private String url;
    private String path_to_pic;
    private String playlist_url;
    private static boolean sort = true;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Player_Fragment.Type getType() {
        return type;
    }

    public void setType(Player_Fragment.Type type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPic() {
        return path_to_pic;
    }

    public void setPic(String path_to_pic) {
        this.path_to_pic = path_to_pic;
    }

    public String getPlaylist() {
        return playlist_url;
    }

    public void setPlaylist(String playlist_url) {
        this.playlist_url = playlist_url;
    }

    public SQLItem(String id, String name, String url, Player_Fragment.Type type)
    {
        this.id = id;
        this.name = name;
        this.url = url;
        this.type = type;
    }

    public SQLItem(String id, String name, String url, Player_Fragment.Type type, String path, String playlist_url)
    {
        this.id = id;
        this.name = name;
        this.url = url;
        this.type = type;
        path_to_pic = path;
        this.playlist_url = playlist_url;
    }

    @Override
    public int compareTo(Object o) {
        SQLItem item = (SQLItem) o;
        return sort ? getName().compareTo(item.getName()) : getName().compareTo(item.getName()) * -1;
    }

    public static void isAscendingSort(boolean asc){
        sort = asc;
    }
}
