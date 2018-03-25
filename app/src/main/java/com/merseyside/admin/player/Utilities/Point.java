package com.merseyside.admin.player.Utilities;

/**
 * Created by Admin on 13.01.2017.
 */

public class Point {
    private int x, y;
    Point(int w, int h) {
        x = w;
        y = h;
    }

    public int getWidth(){
        return x;
    }

    public int getHeight(){
        return y;
    }
}
