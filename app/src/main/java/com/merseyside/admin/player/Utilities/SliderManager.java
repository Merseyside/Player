package com.merseyside.admin.player.Utilities;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.util.Pair;
import android.view.View;

import com.daimajia.slider.library.SliderLayout;
import com.merseyside.admin.player.ActivitesAndFragments.Player_Fragment;
import com.merseyside.admin.player.AdaptersAndItems.ImageSliderView;
import com.merseyside.admin.player.AdaptersAndItems.Track;
import com.merseyside.admin.player.R;

import java.util.ArrayList;


/**
 * Created by Admin on 14.01.2017.
 * This class full of fucking sheet cause author of slider library is complete asshole. Do not blame me please.
 */

public class SliderManager {
    private static final int SLIDER_COUNT = 30;
    private SliderLayout slider;
    private Context context;
    private Bitmap default_cover;
    private Resources resources;
    private int count;
    private ArrayList<Pair<Boolean, Bitmap>> mas;
    private int currentPosition;
    private SliderManagerListener sliderManagerListener;
    private MyTask task;
    private ArrayList<MyTask> tasks;
    private boolean isPrepared = false;

    private boolean onPause;
    private Activity activity;
    private int slidersSkiped;
    private int lastSliderPosition;
    private int realPosition;
    private int realCount;
    private int sliderState = 0;
    private ArrayList<Pair<Slider, Integer>> readyToSet;
    private View slider_view;
    private int height, width;
    private final int NEEDS_TO_DELETE_COUNT = 7;


    public interface SliderManagerListener{
        void getOneMoreTrack(int lastPosition);
        void sliderPrepared();
    }

    class Slider{
        private Bitmap bmp;
        private Bitmap background;

        Slider(Bitmap bmp, Bitmap background){
            this.bmp = bmp;
            this.background = background;
        }

        public Bitmap getBmp() {
            return bmp;
        }

        public Bitmap getBackground() {
            return background;
        }
    }

    class MyTask extends AsyncTask<Track, Void, Slider[]> {

        int pos;
        MyTask(int position){
            this.pos = position;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Slider[] doInBackground(Track... array) {
            Bitmap[] bmps = new Bitmap[1];
            for (Track o : array) {
                if (o != null) {
                    try {
                        MediaMetadataRetriever retriver = new MediaMetadataRetriever();
                        retriver.setDataSource(o.getPath());
                        byte[] data = retriver.getEmbeddedPicture();
                        bmps[0] = Settings.decodeSampledBitmapFromData(data, width, height, data.length);
                        Slider slider = new Slider(bmps[0], null);
                        return new Slider[]{slider};
                    } catch (NullPointerException e){
                        PrintString.printLog("Slider", "error get embedded picture1");
                    } catch (RuntimeException e){
                        PrintString.printLog("Slider", "error get embedded picture2");
                    }
                }
            }
            return new Slider[]{null};
        }

        @Override
        protected void onPostExecute(Slider... slides) {
            super.onPostExecute(slides);
            handleResult(slides[0]);
        }

        private void handleResult(Slider slider1){
            tasks.remove(this);
            if (slider1 != null){
                PrintString.printLog("Slider", "Slider Manager handleResult " + pos + "tasksLeft " + tasks.size());
                if (sliderState == 0 && tasks.size() == 0) {
                    readyToSet.add(new Pair<>(slider1, pos));
                    setReadySliders();
                } else readyToSet.add(new Pair<>(slider1, pos));
            } else {
                mas.set(pos, new Pair<Boolean, Bitmap>(true, null));
                if (sliderState == 0 && tasks.size() == 0) setReadySliders();
            }
        }
    }

    public SliderManager(Context context, Resources resources){
        this.context = context;
        this.resources = resources;
    }

    public boolean isPrepared(){
        return isPrepared;
    }

    public void setSliderManagerListener(SliderManagerListener sliderManagerListener){
        this.sliderManagerListener = sliderManagerListener;
    }

    public void bind(SliderLayout slider, Activity activity, View slider_view){
        PrintString.printLog("Slider", "Slider Manager bind()");
        this.slider = slider;
        this.activity = activity;
        this.slider_view = slider_view;
        setPrepared(false, false);
    }

    public void setInfo(final int count, final int position, Player_Fragment.Type type){
        PrintString.printLog("Slider", "setInfo");
        if (type == Player_Fragment.Type.STREAM) default_cover = BitmapFactory.decodeResource(this.resources, R.drawable.internet);
        else default_cover = Settings.track;
        setPrepared(false, false);
        resetSettings();
        realCount = count;
        if (count >= SLIDER_COUNT)
            this.count = SLIDER_COUNT;
        else {
            if (count == 0) this.count = 1;
            else this.count = count;
        }
        mas = new ArrayList<>();
        for (int i = 0; i<count; i++) mas.add(new Pair<Boolean, Bitmap>(false, null));
        currentPosition = position % count;
        defaultSetup();
    }

    private void defaultSetup(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (Settings.ANIMATION)Thread.sleep(1100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        slider.removeAllSliders();
                        slider.setPresetTransformer(Settings.SLIDER_ANIMATION);
                        for (int i = 0; i<count; i++){
                            addSlider(default_cover);
                        }

                    }
                });
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setPrepared(true,true);
                    }
                });
            }
        }).start();

    }

    public void resetSettings(){
        readyToSet = new ArrayList<>();
        if (tasks != null && !tasks.isEmpty()){
            for (MyTask task : tasks){
                task.cancel(true);
            }
        }
        tasks = new ArrayList<>();
        mas = null;
        currentPosition = 0;
        count = 0;
        onPause = false;
        slidersSkiped = 0;
        lastSliderPosition = -1;
        realCount = 0;
        setPrepared(false, false);
    }

    private void resetPage(){
        setPrepared(false, false);
        for (int i = 0; i<count; i++){
            slider.replaceSliderAt(i, getSlider(default_cover));
            readyToSet.clear();
            mas.set(i, new Pair<Boolean, Bitmap>(false, null));
        }
        setPrepared(true, true);
    }

    public void setSliderPosition(int position){
        int newPosition = position%count;
        if (newPosition != slider.getCurrentPosition()) {
            setPrepared(false, false);
            if (newPosition < slider.getCurrentPosition() - 1) {
                slider.setCurrentPosition(count - 1);
                slider.moveNextPosition();
                slider.setCurrentPosition(newPosition);
            } else {
                slider.setCurrentPosition(newPosition);
            }
            setPrepared(true, false);
        }
    }

    public void setTrack(int position){
        PrintString.printLog("Slider", "SetTrack()");
        height = slider.getHeight();
        width = slider.getWidth();
        if (realPosition/count != position/count) resetPage();

        realPosition = position;
        final int newPosition = position%count;
        slidersSkiped = 0;
        lastSliderPosition = -1;

        currentPosition = newPosition;
        new Thread(new Runnable() {
            @Override
            public void run() {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setSliderPosition(newPosition);
                    }
                });
            }
        }).start();

        for (int i = tasks.size() - 1; i >= 0; i--) {
            tasks.get(i).cancel(true);
            tasks.remove(i);
        }
        isNeedsToExecute(realPosition);
        isNeedsToDelete();
    }

    private boolean isNeedsToExecute(int position){
        PrintString.printLog("Slider", "mas size = " + mas.size() + " position = " + position);
        if (!mas.get((position + 2) % count).first || realPosition >= realCount-2) {
            for (int i = position; i < position + Settings.SLIDER_BUFFER; i++) {
                if (i < realCount && !mas.get(i % count).first) {
                    PrintString.printLog("Slider", "NeedsToExecute()");
                    sliderManagerListener.getOneMoreTrack(i);
                }
            }
        }
        return false;
    }

    private void isNeedsToDelete(){
        if (isPrepared && sliderState == 0 && currentPosition - Settings.SLIDER_BUFFER >= 0){
            PrintString.printLog("Slider", "NeedsToDelete");

            ArrayList<Integer> ints =new ArrayList<>();
            for (int i = 0; i<count; i++){
                if (i < currentPosition - Settings.SLIDER_BUFFER ||  i > currentPosition + Settings.SLIDER_BUFFER){
                    if (mas.get(i).first) {
                        ints.add(i);
                    }
                }
            }
            if (ints.size() >= NEEDS_TO_DELETE_COUNT){
                setPrepared(false, false);
                for (Integer i : ints){
                    mas.set(i, new Pair<Boolean, Bitmap>(false, null));
                    slider.replaceSliderAt(i, getSlider(default_cover));
                    PrintString.printLog("Slider", "Deleted at " + i);
                }
                setPrepared(true, false);
            }

        }
    }

    public void setOneMoreTrack(Track track, int position){
        if (track !=null) {
            position%=count;
            mas.set(position, new Pair<Boolean, Bitmap>(false, null));
            start(position, track);
        }
    }

    public void setOnPause(boolean pause){
        onPause = pause;
    }

    public boolean isOnPause(){
        return onPause;
    }

    private void setPrepared(boolean prepared, boolean delay){
        if (prepared && delay){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(600);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    isPrepared = true;
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!onPause){
                                sliderManagerListener.sliderPrepared();
                                slider_view.setVisibility(View.GONE);
                                PrintString.printLog("setPrepared", "INVisible");
                                PrintString.printLog("setPrepared", "Slider Manager setPrepared " + isPrepared);
                            }
                        }
                    });

                }
            }).start();
        }
        else {
            if (prepared) {
                slider_view.setVisibility(View.GONE);
                PrintString.printLog("setPrepared", "InVisible");
            }
            else {
                slider_view.setVisibility(View.VISIBLE);
                PrintString.printLog("setPrepared", "Visible");
            }
            isPrepared = prepared;
            PrintString.printLog("setPrepared", "Slider Manager setPrepared " + prepared);
        }
    }

    private boolean start(int position, Track track){
        task = new MyTask(position);
        tasks.add(task);
        task.execute(track);
        return true;
    }

    private void addSlider(Bitmap bmp){
        ImageSliderView imageSliderView = new ImageSliderView(context);
        imageSliderView.description("");
        imageSliderView.image(bmp);
        slider.addSlider(imageSliderView);
    }

    private ImageSliderView getSlider(Bitmap bmp){
        ImageSliderView imageSliderView = new ImageSliderView(context);
        imageSliderView.description("");
        imageSliderView.image(bmp);
        return imageSliderView;
    }

    public int move(int position){
        PrintString.printLog("Slider", "Slider Manager " + position);
        if (position != realPosition || position == 0 || position == realCount-1) {
            int lastPos = lastSliderPosition == -1 ? currentPosition : lastSliderPosition;
            slidersSkiped+=sliderSkippedDecider(position, lastPos);
            PrintString.printLog("Slider1", "slidersSkipped = " + slidersSkiped + " lastPos = " + lastPos + "realPosition " + realPosition);
            setPrepared(false,false);
            if (slidersSkiped + realPosition >= realCount) {
                slider.movePrevPosition();
                slidersSkiped--;
            }
            else if (slidersSkiped + realPosition < 0){
                slider.moveNextPosition();
                slidersSkiped++;
            }
            else lastSliderPosition = position;
            setPrepared(true, false);
            return slidersSkiped;
        }
        slidersSkiped = 0;
        lastSliderPosition = -1;
        return 0;
    }

    private int sliderSkippedDecider(int newPos, int lastPos){
        if (count!=2) {
            if (newPos == 0 && lastPos == count - 1) return 1;
            else if (newPos == count - 1 && lastPos == 0) return -1;
            else if (newPos > lastPos) return 1;
            else return -1;
        } else {
            if (newPos < lastPos) return -1;
            else return 1;
        }
    }

    public void setVisibility(int visibility){
        slider.setVisibility(visibility);
    }

    public void delete(int position){
        setPrepared(false, false);
        realCount--;
        if (realCount<SLIDER_COUNT){
            count = realCount;
            if (count != 0)position %= count;
            mas.remove(position);
            slider.removeSliderAt(position);
        }
        else{
            position %= count;
            mas.set(position, new Pair<Boolean, Bitmap>(false, null));
            slider.removeSliderAt(position);
            addSlider(default_cover);
        }
        setPrepared(true, false);
    }

    public void orderChanged(int newCurrentPos){
        PrintString.printLog("Slider", "newCurrentPos = " + newCurrentPos);
        setPrepared(false, false);
        boolean track_flag = false;
        if (newCurrentPos != realPosition) track_flag = true;
        for (int i = 0; i < count; i++) {
            if (i == slider.getCurrentPosition()){
                if (!track_flag) continue;
            }
            else {
                slider.replaceSliderAt(i, getSlider(default_cover));
                mas.set(i, new Pair<Boolean, Bitmap>(false, null));
            }
        }
        if (newCurrentPos != realPosition) setTrack(newCurrentPos);
        setPrepared(true, true);

    }

    public void setState(int state){
        sliderState = state;
        if (sliderState == 0){
            setReadySliders();
        }
    }

    private void setReadySliders(){
        if (readyToSet.size() != 0 && tasks!=null && tasks.size()==0) {
            PrintString.printLog("Slider","setReadySliders()");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(600);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (sliderState == 0) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setPrepared(false, false);
                                for (int i = 0; i < readyToSet.size(); i++) {
                                    if (currentPosition + Settings.SLIDER_BUFFER > readyToSet.get(i).second) {
                                        slider.replaceSliderAt(readyToSet.get(i).second, getSlider(readyToSet.get(i).first.getBmp()));
                                        mas.set(readyToSet.get(i).second, new Pair<Boolean, Bitmap>(true, null));
                                    }
                                }
                                readyToSet.clear();
                                setPrepared(true, true);
                            }
                        });
                    }
                }
            }).start();
        }
    }
}
