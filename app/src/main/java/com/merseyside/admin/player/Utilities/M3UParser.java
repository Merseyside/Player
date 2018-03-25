package com.merseyside.admin.player.Utilities;

import android.content.Context;
import android.media.MediaMetadataRetriever;

import com.merseyside.admin.player.ActivitesAndFragments.Player_Fragment;
import com.merseyside.admin.player.AdaptersAndItems.Item;
import com.merseyside.admin.player.AdaptersAndItems.Track;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Admin on 16.06.2016.
 */
public class M3UParser {

    private File file;

    private String lastErr;
    private boolean error;
    private Context context;

    private final String DEFAULT_ERROR = "ERROR";
    private final String NOT_PLAYLIST_ERROR = "ERROR: Not a playlist";
    private final String TRACK_IS_NULL_ERROR = "ERROR: Track is null";
    private final String FILE_NOT_FOUND_ERROR = "ERROR: File not found";
    private final String LINE_TWO_ERROR = "ERROR: Line 2 in getGroup() isn't correct";
    private final String CANT_FIND_GROUP_ERROR = "ERROR: Can't find group in get group";
    private final String CANT_WRITE_ERROR = "ERROR: Couldn't write to the file:";
    private final String CANT_FIND_MATCHES_ERROR = "ERROR: Can't find a matches";
    private final String NO_ERROR = "NO ERROR";

    public M3UParser(File file, Context context) {
        this.file = file;
        this.context = context;
    }

    public void LogFile()
    {
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            try {
                while((line = br.readLine()) != null)
                {
                    PrintString.printLog("LOG", line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        catch(FileNotFoundException e)
        {
            setLastErr(FILE_NOT_FOUND_ERROR, e);
        }
    }


    public ArrayList<Track> getTracksList() {
        ArrayList<Track> list = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            if(checkPlaylist(br.readLine())) {
                while ((line = br.readLine()) != null) {
                    if (line.equals("")) continue;
                    else {
                        String line2;
                        if((line2 = br.readLine()) != null) {
                            Track track = getGroup(line, line2);

                            if (track != null) {
                                list.add(track);
                            }
                            else {
                                setLastErr(TRACK_IS_NULL_ERROR, null);
                            }
                        }
                    }
                }
                br.close();
                return list;
            }
            else{
                setLastErr(NOT_PLAYLIST_ERROR, null);
                return null;
            }
        }
        catch (NullPointerException |IOException e) {
            setLastErr(DEFAULT_ERROR, e);
        }
        return null;
    }

    private Track getGroup(String line, String line2)
    {
        PrintString.printLog("Track", line);
        PrintString.printLog("Track", line2);
        String PATTERN_COMPILE;
        Pattern pattern;
        Matcher matcher;

        int type = 0;
        String duration = null;
        String name = null;
        String artist = null;
        String rating = null;
        String start = null;
        String end = null;
        String path = null;
        String crossfade = null;
        String fading = null;
        String increase = null;
        String comment = null;
        String date = null;
        String transition = "-1";
        String trans_dur = "0";
        Track track;

        if(!line2.equals("")) {
            PATTERN_COMPILE = "^http.+$";
            pattern = Pattern.compile(PATTERN_COMPILE);
            matcher = pattern.matcher(line2);
            if (matcher.find())
            {
                path = matcher.group();
                type = Track.INTERNET_TRACK;
                PrintString.printLog("regex", path + "\n");
            }
            else
            {
                type = Track.MEMORY_TRACK;
                path = line2;
            }
        }
        else {
            setLastErr(LINE_TWO_ERROR, null);
            return null;
        }

        PATTERN_COMPILE = "^#EXTINF:[0-9]+,";
        pattern = Pattern.compile(PATTERN_COMPILE);
        matcher = pattern.matcher(line);
        if(matcher.find())
        {
            String str  = "#EXTINF:";
            String str1 = ",";
            duration = matcher.group();
            String firstString = line.replace(duration, "");

            duration = duration.replace(str, "");
            duration = duration.replace(str1, "");
            duration = duration.replace(" ", "");
            PrintString.printLog("regex", duration);
            if (type == Track.MEMORY_TRACK )PATTERN_COMPILE = "^ARTIST:.+,TRACK";
            else PATTERN_COMPILE = "^.+-";
            pattern = Pattern.compile(PATTERN_COMPILE);
            matcher = pattern.matcher(firstString);
            PrintString.printLog("regex", "before " + firstString);
            if(matcher.find()) {
                artist = matcher.group();
                if (type == Track.MEMORY_TRACK) {
                    artist = artist.replace("ARTIST:", "");
                    artist = artist.replace(",TRACK", "");
                    firstString = firstString.replace("ARTIST:" + artist + ",", "");
                } else {
                    firstString = firstString.replace(artist , "");
                    artist = artist.replace("-", "");
                }

                if (artist.substring(0,1).equals(" ")) artist = artist.replaceFirst(" ", "");
                PrintString.printLog("regex", "artist = " + "'" + artist + "'");
                PrintString.printLog("regex", "before " + firstString);

                if (type == Track.MEMORY_TRACK )PATTERN_COMPILE = "^TRACK:.+,RATING";
                else PATTERN_COMPILE = "^.+,RATING";
                pattern = Pattern.compile(PATTERN_COMPILE);
                matcher = pattern.matcher(firstString);
                if (matcher.find()) {
                    name = matcher.group();
                    if (type == Track.MEMORY_TRACK) name = name.replace("TRACK:", "");
                    name = name.replace(",RATING", "");
                    if (name.substring(0,1).equals(" ")) name = name.replaceFirst(" ", "");
                    firstString = firstString.replace("TRACK:" + name, "");
                    PrintString.printLog("regex", "name = " + name);

                    PATTERN_COMPILE = "^,RATING:.,START_POINT";
                    pattern = Pattern.compile(PATTERN_COMPILE);
                    matcher = pattern.matcher(firstString);
                    PrintString.printLog("regex", "before " + firstString);
                }
                else {
                    name = firstString;
                    if (name.substring(0,1).equals(" ")) name = name.replaceFirst(" ", "");
                }
                if (matcher.find()){
                    rating = matcher.group();
                    rating = rating.replace(",RATING:", "");
                    rating = rating.replace(",START_POINT", "");
                    PrintString.printLog("regex", "rating = " + rating);
                    firstString = firstString.replace(",RATING:" + rating + ",", "");

                    PATTERN_COMPILE = "^START_POINT:.+,END_POINT";
                    pattern = Pattern.compile(PATTERN_COMPILE);
                    matcher = pattern.matcher(firstString);
                    PrintString.printLog("regex", "before " + firstString);
                    if (matcher.find()){
                        start = matcher.group();
                        start = start.replace("START_POINT:", "");
                        start = start.replace(",END_POINT", "");
                        PrintString.printLog("regex", "start = " + start);
                        firstString = firstString.replace("START_POINT:" + start + ",", "");
                        PrintString.printLog("regex", "before " + firstString);
                        PATTERN_COMPILE = "^END_POINT:.+,CROSSFADE";
                        pattern = Pattern.compile(PATTERN_COMPILE);
                        matcher = pattern.matcher(firstString);
                        if (matcher.find()){
                            end = matcher.group();
                            end = end.replace("END_POINT:", "");
                            end = end.replace(",CROSSFADE", "");
                            firstString = firstString.replace("END_POINT:" + end + ",", "");
                            PrintString.printLog("regex", "end = " + end);
                            PrintString.printLog("regex", "before " + firstString);
                            PATTERN_COMPILE = "^CROSSFADE:.+,FADING";
                            pattern = Pattern.compile(PATTERN_COMPILE);
                            matcher = pattern.matcher(firstString);
                            if (matcher.find()) {
                                crossfade = matcher.group();
                                crossfade = crossfade.replace("CROSSFADE:", "");
                                crossfade = crossfade.replace(",FADING", "");
                                firstString = firstString.replace("CROSSFADE:" + crossfade + ",", "");
                                PrintString.printLog("regex", "crossfade = " + crossfade);
                                PrintString.printLog("regex", "before " + firstString);

                                PATTERN_COMPILE = "^FADING:.+,INCREASE";
                                pattern = Pattern.compile(PATTERN_COMPILE);
                                matcher = pattern.matcher(firstString);
                                if (matcher.find()) {
                                    fading = matcher.group();
                                    fading = fading.replace("FADING:", "");
                                    fading = fading.replace(",INCREASE", "");
                                    firstString = firstString.replace("FADING:" + fading + ",", "");
                                    PrintString.printLog("regex", "fading = " + fading);

                                    PATTERN_COMPILE = "^INCREASE:.+,COMMENT";
                                    pattern = Pattern.compile(PATTERN_COMPILE);
                                    matcher = pattern.matcher(firstString);
                                    if (matcher.find()) {
                                        increase = matcher.group();
                                        increase = increase.replace("INCREASE:", "");
                                        increase = increase.replace(",COMMENT", "");
                                        firstString = firstString.replace("INCREASE:"+ increase + ",", "");
                                        PrintString.printLog("regex", "increase = " + increase);
                                        PrintString.printLog("regex", "before " + firstString);

                                        PATTERN_COMPILE = "^COMMENT:.+,DATE";
                                        pattern = Pattern.compile(PATTERN_COMPILE);
                                        matcher = pattern.matcher(firstString);
                                        if (matcher.find()){
                                            comment = matcher.group();
                                            comment = comment.replace("COMMENT:", "");
                                            comment = comment.replace(",DATE", "");
                                            firstString = firstString.replace("COMMENT:" + comment + ",", "");
                                            PrintString.printLog("regex", "comment = " + comment);

                                            PATTERN_COMPILE = "^DATE:.+,TRANSITION";
                                            pattern = Pattern.compile(PATTERN_COMPILE);
                                            matcher = pattern.matcher(firstString);
                                            if (matcher.find()){
                                                date = matcher.group();
                                                date = date.replace("DATE:", "");
                                                date = date.replace(",TRANSITION", "");
                                                firstString = firstString.replace("DATE:" + date + ",", "");

                                                PATTERN_COMPILE = "^TRANSITION:.+,TRANS_DUR";
                                                pattern = Pattern.compile(PATTERN_COMPILE);
                                                matcher = pattern.matcher(firstString);
                                                if (matcher.find()){
                                                    transition = matcher.group();
                                                    transition = transition.replace("TRANSITION:", "");
                                                    transition = transition.replace(",TRANS_DUR", "");
                                                    firstString = firstString.replace("TRANSITION:" + transition + ",", "");

                                                    PATTERN_COMPILE = "^TRANS_DUR:.+$";
                                                    pattern = Pattern.compile(PATTERN_COMPILE);
                                                    matcher = pattern.matcher(firstString);
                                                    if (matcher.find()){
                                                        trans_dur = matcher.group();
                                                        trans_dur = trans_dur.replace("TRANS_DUR:", "");
                                                    }
                                                }
                                            } else {
                                                PATTERN_COMPILE = "^DATE:.+$";
                                                pattern = Pattern.compile(PATTERN_COMPILE);
                                                matcher = pattern.matcher(firstString);
                                                if (matcher.find()) {
                                                    date = matcher.group();
                                                    date = date.replace("DATE:", "");
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (start != null && end !=null && rating != null)
            track = new Track(type, name, artist, duration, path, file.getAbsolutePath(), rating, start, end, crossfade, fading, increase, comment, date, transition, trans_dur);
            else track = new Track(type, name, artist, duration, path, file.getAbsolutePath());
            return track;

        }
        setLastErr(CANT_FIND_GROUP_ERROR, null);
        return null;
    }

    private boolean checkPlaylist(String line)
    {
        Pattern pattern;
        Matcher matcher;
        String PATTERN_COMPILE;
        PATTERN_COMPILE = "^#EXTM3U$";
        pattern = Pattern.compile(PATTERN_COMPILE);
        matcher = pattern.matcher(line);
        return matcher.find();
    }

    public boolean addTracksFromFolder(File from, File to)
    {
        file = to;
        if (to.exists()) {
            to.delete();
            try {
                to.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ArrayList<Item> list = new ArrayList<>();
        File[] dirs = from.listFiles();
        List<File> directoryListing = new ArrayList<>();
        directoryListing.addAll(Arrays.asList(dirs));
        Collections.sort(directoryListing, new SortFileName());

        String PATTERN_COMPILE;
        Pattern pattern;
        Matcher matcher;
        PATTERN_COMPILE = Settings.FORMATS_PATTERN;
        pattern = Pattern.compile(PATTERN_COMPILE);
        for(File o : directoryListing) {
            matcher = pattern.matcher(o.getName());
            if (matcher.matches())
            list.add(new Item(o.getName(), o.getAbsolutePath()));
        }
        addItemsToPlaylist(list);
        return false;
    }

    public boolean changePlaylist(ArrayList<Track> list){
        try {
            file.delete();
            file.createNewFile();
            addTracksToPlaylist(list);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean addTracksToPlaylist(ArrayList<Track> list) {
        BufferedWriter bw;
        try {
            bw = new BufferedWriter(new FileWriter(file, true));
        } catch (IOException e) {
            setLastErr(CANT_WRITE_ERROR, e);
            return false;
        }
        if (file.length() == 0){
            try {
                bw.write("#EXTM3U");
                bw.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for (Track o : list) {
            String path = o.getPath();
            String name = o.getName();
            String artist = o.getArtist();
            String duration = o.getDuration();
            String rating = String.valueOf(o.getRating());
            String startPoint = String.valueOf(o.getStartPoint());
            String endPoint = String.valueOf(o.getEndPoint());
            String crossfade = String.valueOf(o.getCrossfadeDuration());
            String fading = String.valueOf(o.getFading());
            String increase = String.valueOf(o.getIncrease());
            String comment = o.getComment();
            String date = String.valueOf(o.getDate());
            String transition = String.valueOf(o.getTransition());
            String trans_duration = String.valueOf(o.getTransit_duration());

            StringBuilder builder = new StringBuilder();
            if (name == null) name = o.getName();
            if (artist == null) artist = "";
            if (o.getType() != Track.INTERNET_TRACK)
            builder.append("#EXTINF:" + duration  + ",ARTIST:" + artist + ",TRACK:" + name + ",RATING:" + rating + ",START_POINT:" + startPoint + ",END_POINT:" + endPoint +
                    ",CROSSFADE:" + crossfade + ",FADING:" + fading + ",INCREASE:" + increase + ",COMMENT:" + comment + ",DATE:" + date + ",TRANSITION:" + transition + ",TRANS_DUR:" + trans_duration);
            else builder.append("#EXTINF:" + duration  + "," + artist + "-" + name + ",RATING:" + rating + ",START_POINT:" + startPoint + ",END_POINT:" + endPoint +
                    ",CROSSFADE:" + crossfade + ",FADING:" + fading + ",INCREASE:" + increase + ",COMMENT:" + comment + ",DATE:" + date + ",TRANSITION:" + transition + ",TRANS_DUR:" + trans_duration);

            String str1 = builder.toString();
            String str2 = path;

            try {
                bw.write(str1);
                bw.newLine();
                bw.write(str2);
                bw.newLine();
            } catch (IOException e) {

            }
        }
        try {
            if (bw != null) {
                bw.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        list.clear();
        return true;
    }

    public boolean addItemsToPlaylist(ArrayList<Item> list) {
        BufferedWriter bw;
        try {
            bw = new BufferedWriter(new FileWriter(file, true));
        } catch (IOException e) {
            setLastErr(CANT_WRITE_ERROR, e);
            return false;
        }
        if (file.length() == 0) {
            try {
                if (bw != null) {
                    bw.write("#EXTM3U");
                    bw.newLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for (Item o : list) {
            MediaMetadataRetriever retriver = new MediaMetadataRetriever();
            try {
                retriver.setDataSource(o.getPath());
            } catch (RuntimeException e){
                FirebaseEngine.logEvent(context, "ADD_ITEMS_TO_PLAYLIST_RUNTIME", null);
                continue;
            }
            String path = o.getPath();
            String name = null;
            String artist = null;
            if (Settings.METADATA) {
                 name = retriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                 artist = retriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            }
            String duration = retriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

            try {
                duration = String.valueOf(Integer.valueOf(duration) / 1000);
            } catch (NumberFormatException ignored){
                continue;
            }

            StringBuilder builder = new StringBuilder();
            if (name == null || name.equals("")){
                String[] strs = o.getName().split("\\-");
                if (strs.length > 1){
                    name = strs[1];
                } else {
                    name = o.getName();
                }
            }
            if (artist == null || artist.equals("")) {
                String[] strs = o.getName().split("-");
                if (strs.length > 1){
                    artist = strs[0];
                } else {
                    artist = o.getName();
                }
            }

            if (!Settings.METADATA) {
                Pattern pattern;
                Matcher matcher;
                pattern = Pattern.compile(Settings.FORMATS_PATTERN);
                matcher = pattern.matcher(name);
                if (matcher.matches()) {
                    String[] strs = name.split("\\.");
                    StringBuilder strBuilder = new StringBuilder();
                    for (int i = 0; i<strs.length-1; i++)
                        strBuilder.append(strs[i]);
                    name = strBuilder.toString();
                }
            }
            if (name.length() != 0 && name.substring(0, 1).equals(" ")) name = name.replaceFirst(" ", "");
            if (name.length() != 0 && name.substring(name.length() - 1, name.length()).equals(" "))
                name = name.substring(0, name.length() - 1);

            if (artist.length() != 0 && artist.substring(0, 1).equals(" ")) artist = artist.replaceFirst(" ", "");
            if (artist.length() != 0 && artist.substring(artist.length() - 1, artist.length()).equals(" "))
                    artist = artist.substring(0, artist.length() - 1);

            builder.append("#EXTINF:" + duration  + ",ARTIST:" + artist + ",TRACK:" + name + ",RATING:0,START_POINT:null,END_POINT:null,CROSSFADE:0,FADING:0," +
                    "INCREASE:0,COMMENT:null,DATE:" + Settings.getDayOfYear() + ",TRANSITION:-1" + ",TRANS_DUR:0");
            String str1 = builder.toString();
            String str2 = path;

            try {
                assert bw != null;
                bw.write(str1);
                bw.newLine();
                bw.write(str2);
                bw.newLine();
            } catch (IOException ignored) {}
        }
        try {
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        list.clear();
        return true;
    }

    public boolean deleteTrack(Track o) {

        BufferedReader reader;
        BufferedWriter writer;

        List<String> list = new ArrayList<>();

        Boolean found = false;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line = null;

            String PATTERN_COMPILE = o.getName();
            PATTERN_COMPILE = PATTERN_COMPILE.replace("(", "\\(");
            PATTERN_COMPILE = PATTERN_COMPILE.replace(")", "\\)");
            PATTERN_COMPILE = PATTERN_COMPILE.replace(".", "\\.");
            PATTERN_COMPILE = PATTERN_COMPILE.replace("+", "\\+");
            PATTERN_COMPILE = PATTERN_COMPILE.replace("[", "\\[");
            PATTERN_COMPILE = PATTERN_COMPILE.replace("]", "\\]");

            Pattern pattern;
            Matcher matcher;

            pattern = Pattern.compile(PATTERN_COMPILE);
            while((line = reader.readLine())  != null) {
                if (!found){
                    matcher = pattern.matcher(line);
                    if (matcher.find()){
                        String duration = o.getDuration();
                        Pattern durPat = Pattern.compile(duration);
                        matcher = durPat.matcher(line);
                        if (matcher.find())
                        {
                            found = true;
                            reader.readLine();
                            continue;
                        }
                    }
                }
                list.add(line);
            }
            reader.close();

            if (!found) {
                setLastErr(CANT_FIND_MATCHES_ERROR, null);
                return false;
            }

            RandomAccessFile f = new RandomAccessFile(file, "rw");
            f.setLength(0);
            f.close();

            writer = new BufferedWriter(new FileWriter(file));
            Iterator<String> iterator = list.iterator();
            while(iterator.hasNext()){
                writer.write(iterator.next());
                writer.newLine();
            }
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
            setLastErr(DEFAULT_ERROR, e);
            return false;
        } catch (NullPointerException ignored){}
        return true;
    }

    private void setLastErr(String value, Exception e){
        if (e != null) lastErr = value + " " + e.toString();
        else lastErr = value;
        error = true;
    }

    public String getLastErr() {
        String error_str = lastErr;
        lastErr = NO_ERROR;
        error = false;
        return error_str;
    }

    public boolean isErrored(){
        return error;
    }

    public class SortFileName implements Comparator<File> {
        @Override
        public int compare(File f1, File f2) {
            return f1.getName().compareTo(f2.getName());
        }
    }
}

