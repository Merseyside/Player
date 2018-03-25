package com.merseyside.admin.player.AdaptersAndItems;

/**
 * Created by Admin on 08.01.2017.
 */

public class DirectoriesItem {
    String name;
    String path;
    String id;

    public DirectoriesItem(String id, String name, String path){
        this.id = id;
        this.name = name;
        this.path = path;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }
}
