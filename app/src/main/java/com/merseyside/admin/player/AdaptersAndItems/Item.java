package com.merseyside.admin.player.AdaptersAndItems;

import android.os.Parcel;
import android.os.Parcelable;

public class Item implements Comparable<Item>, Parcelable {
    private String name;
    private String data;
    private String date;
    private String path;
    private Integer image;
    private boolean selected;

    public Item(String n, String d, String dt, String p, Integer img)
    {
        name = n;
        data = d;
        date = dt;
        path = p;
        image = img;
        selected = false;
    }

    public Item(String name, String path)
    {
        this.name = name;
        this.path = path;
    }

    protected Item(Parcel in) {
        name = in.readString();
        data = in.readString();
        date = in.readString();
        path = in.readString();
        selected = in.readByte() != 0;
    }

    public static final Creator<Item> CREATOR = new Creator<Item>() {
        @Override
        public Item createFromParcel(Parcel in) {
            return new Item(in);
        }

        @Override
        public Item[] newArray(int size) {
            return new Item[size];
        }
    };

    public String getName()
    {
        return name;
    }
    public String getData()
    {
        return data;
    }
    public String getDate()
    {
        return date;
    }
    public String getPath()
    {
        return path;
    }
    public boolean getSelected(){return selected;}
    public Integer getImage() {
        return image;
    }
    public void setSelected(boolean selected)
    {
        this.selected = selected;
    }
    public int compareTo(Item o) {
        if(this.name != null)
            return this.name.toLowerCase().compareTo(o.getName().toLowerCase());
        else
            throw new IllegalArgumentException();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(data);
        parcel.writeString(date);
        parcel.writeString(path);
        parcel.writeByte((byte) (selected ? 1 : 0));
    }
}
