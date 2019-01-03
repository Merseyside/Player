package com.merseyside.admin.player.AdaptersAndItems;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.merseyside.admin.player.Utilities.PrintString;
import com.merseyside.admin.player.Utilities.Settings;

import java.io.Serializable;

public class Track implements Comparable, Cloneable, Parcelable {
    public static final int POSITION = 0;
    public static final int RATING = 1;
    public static final int DATE = 2;

    public static final int MEMORY_TRACK = 0;
    public static final int INTERNET_TRACK = 1;
    private static int SORT_BY = POSITION;
    private int position;
    private String name;
    private String artist;
    private String duration;
    private String startPoint = "null";
    private String endPoint = "null";
    private String path;
    private String rating = "0";
    private String album;
    private int type;
    private String playlistPath;
    private boolean isMegamixTrack;
    private String crossfade = "0";
    private String fading = "0";
    private String increase = "0";
    private String comment = "null";
    private int date = 0;
    private int transition;
    private int transit_duration;

    public int getTransit_duration() {
        return transit_duration;
    }

    public void setTransit_duration(int transit_duration) {
        this.transit_duration = transit_duration;
    }

    public void setTransition(int transition){
        this.transition = transition;
    }

    public int getTransition(){
        return transition;
    }

    public int getDate(){
        return date;
    }

    public String getComment(){
        return comment;
    }

    public void setComment(String comment){
        if (comment.equals("")) comment = "null";
        this.comment = comment;
    }

    public int getIncrease() {
        return Integer.valueOf(increase);
    }

    public void setIncrease(String increase) {
        this.increase = increase;
    }

    public void setIncrease(int increase) {
        this.increase = String.valueOf(increase);
    }

    public boolean isMegamixTrack(){
        return isMegamixTrack;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getName() {
        return name;
    }

    public String getArtist() {
        return artist;
    }

    public String getDuration(){
        if (isMegamixTrack) return String.valueOf(getDurationLong()/1000);
        else return duration;
    }

    public long getDurationForPlayer() {
        if (isMegamixTrack) return Long.valueOf(endPoint);
        else return Long.valueOf(duration) * 1000;
    }

    public long getDurationLong() throws NumberFormatException{
        if (isMegamixTrack) return Long.valueOf(endPoint) - Long.valueOf(startPoint);
        else return Long.valueOf(duration) * 1000;
    }

    public String getStartPoint() {
        return startPoint;
    }

    public String getEndPoint() {
        return endPoint;
    }

    public String getPath() {
        return path;
    }

    public String getPlaylistPath(){
        return playlistPath;
    }

    public int getRating() {
        return Integer.valueOf(rating);
    }

    public String getAlbum() {
        return album;
    }

    public int getType() {
        return type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setDuration(int duration){
        this.duration = String.valueOf(duration/1000);
    }

    public void setDuration(String duration) {

        int dur = Integer.valueOf(duration)/1000;
        this.duration = String.valueOf(dur);
    }


    public void setPath(String path) {
        this.path = path;
    }

    public void setRating(int rating) {
        PrintString.printLog("lifeCycle", rating + " rating");
        this.rating = String.valueOf(rating);
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getFading(){
        if (isMegamixTrack) return Integer.valueOf(fading);
        else return 0;
    }

    public void set_start_point(int point){
        startPoint = String.valueOf(point);
    }

    public void set_end_point(int point){
        endPoint = String.valueOf(point);
    }

    public void setFading(int f){
        fading = String.valueOf(f);
    }

    public int getCrossfadeDuration(){
        return Integer.valueOf(crossfade);
    }

    public void setCrossfadeDuration(int crossfadeDuration){
        crossfade = String.valueOf(crossfadeDuration);
    }

    public void setMegamixTrack(boolean b){
        isMegamixTrack = b;
    }

    public Bitmap getCover(){
        MediaMetadataRetriever retriver = new MediaMetadataRetriever();
        Bitmap bmp;
        try {
            retriver.setDataSource(getPath());
            byte[] data = retriver.getEmbeddedPicture();
            bmp = BitmapFactory.decodeByteArray(data, 0,data.length);
        } catch (RuntimeException e) {
            return Settings.track;
        }catch (OutOfMemoryError e){
            return null;
        }
        return bmp;
    }

    public Track(int type, String name, String artist, String duration, String path, String playlistPath) {
        this.type = type;
        this.name = name;
        this.artist = artist;
        this.duration = duration;
        this.path = path;
        this.playlistPath = playlistPath;
        isMegamixTrack = false;
    }

    public Track(int type, String name, String artist, String duration, String path, String playlistPath, String rating, String startPoint,
                 String endPoint, String crossfade, String fading, String increase, String comment, String date, String transition, String trans_dur) {
        this.type = type;
        this.name = name;
        this.artist = artist;
        this.duration = duration;
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.path = path;
        this.rating = rating;
        this.crossfade = crossfade;
        this.increase = increase;
        this.fading = fading;
        this.playlistPath = playlistPath;
        isMegamixTrack = !startPoint.equals("null") && !endPoint.equals("null");
        this.comment = comment;
        this.date = Integer.valueOf(date);
        this.transition = Integer.valueOf(transition);
        this.transit_duration = Integer.valueOf(trans_dur);
        Log.d("Transition", trans_dur);
    }

    private Track(Parcel in) {
        name = in.readString();
        artist = in.readString();
        duration = in.readString();
        startPoint = in.readString();
        endPoint = in.readString();
        path = in.readString();
        rating = in.readString();
        album = in.readString();
        type = in.readInt();
        playlistPath = in.readString();
        crossfade = in.readString();
        fading = in.readString();
        increase = in.readString();
        comment = in.readString();
        date = in.readInt();
        isMegamixTrack = !startPoint.equals("null");
    }

    public static void setSortBy(int sort)
    {
        SORT_BY = sort;
    }

    @Override
    public int compareTo(Object o) {
        Track track = (Track) o;
        if (SORT_BY == POSITION) {
            return position - track.position;
        } else if(SORT_BY == RATING){
            return track.getRating() - getRating();
        } else if(SORT_BY == DATE){
            return track.getDate() - getDate();
        }
        return 0;
    }

    public Track cloneTrack(){
        try {
            return (Track)this.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(artist);
        parcel.writeString(duration);
        parcel.writeString(startPoint);
        parcel.writeString(endPoint);
        parcel.writeString(path);
        parcel.writeString(rating);
        parcel.writeString(album);
        parcel.writeInt(type);
        parcel.writeString(playlistPath);
        parcel.writeString(crossfade);
        parcel.writeString(fading);
        parcel.writeString(increase);
        parcel.writeString(comment);
        parcel.writeInt(date);
    }

    public static final Parcelable.Creator<Track> CREATOR = new Parcelable.Creator<Track>() {
        public Track createFromParcel(Parcel in) {
            return new Track(in);
        }

        public Track[] newArray(int size) {
            return new Track[size];
        }
    };
}
