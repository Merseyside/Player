/**
 * Copyright 2014 Magnus Woxblom
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.merseyside.admin.player.AdaptersAndItems;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.support.v4.util.ArrayMap;
import android.support.v4.util.Pair;
import android.support.v4.util.SparseArrayCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.merseyside.admin.player.R;
import com.merseyside.admin.player.Utilities.PrintString;
import com.merseyside.admin.player.Utilities.Settings;
import com.woxthebox.draglistview.DragItemAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ItemAdapter extends DragItemAdapter<Pair<Long, Track>, ItemAdapter.ViewHolder> {

    private int mLayoutId;
    private int mGrabHandleId;
    private boolean mDragOnLongPress;
    private int highlight = -1;
    private ItemClickListener myClickInterface;
    private boolean dragging;
    private ArrayMap<Integer, Bitmap> covers;
    private boolean orderMode = false, isListAdapter = true;
    private static int size;
    private GridLayoutManager gridLayoutManager;
    private LinearLayoutManager linearLayoutManager;
    private ArrayMap<Integer, MyTask> tasks;


    private Settings settings;

    private Context context;

    public ItemAdapter(Context context, ArrayList<Pair<Long, Track>> list, int layoutId, int grabHandleId, boolean dragOnLongPress, boolean isListAdapter) {
        mLayoutId = layoutId;
        mGrabHandleId = grabHandleId;
        mDragOnLongPress = dragOnLongPress;
        setHasStableIds(true);
        setItemList(list);
        this.context = context;
        settings = new Settings(context);
        highlight = -1;
        covers = new ArrayMap<>();
        tasks = new ArrayMap<>();
        this.isListAdapter = isListAdapter;
    }

    public void setLayoutManager(RecyclerView.LayoutManager layoutManager){
        if (!isListAdapter) this.gridLayoutManager = (GridLayoutManager) layoutManager;
        else this.linearLayoutManager = (LinearLayoutManager) layoutManager;
    }

    public void setOnItemClickListener(ItemClickListener icl){
        myClickInterface = icl;
    }

    public void deleteOnItemCliickListener(){
        myClickInterface = null;
    }

    public ItemAdapter(Context context, ArrayList<Pair<Long, Track>> list, int layoutId, int grabHandleId, boolean dragOnLongPress, int position) {
        mLayoutId = layoutId;
        mGrabHandleId = grabHandleId;
        mDragOnLongPress = dragOnLongPress;
        setHasStableIds(true);
        setItemList(list);
        orderMode = true;
        highlight = position;
        this.context = context;
        settings = new Settings(context);
    }

    public  interface ItemClickListener{
        void itemClicked(int position);
        void itemLongClicked(int position);
    }

    private void itemClicked(int position){
        if (myClickInterface != null) myClickInterface.itemClicked(position);
    }

    private void itemLongClicked(int position){
        myClickInterface.itemLongClicked(position);
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(mLayoutId, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        holder.view.setTag(R.string.tag, position);
        Track o = mItemList.get(position).second;
        if (o.getName()==null || o.getName().equals("")) {
            mItemList.get(position).second.setName("Track");
            o.setName("Track");
        }
        holder.name.setText(o.getName());
        holder.artist.setText(o.getArtist());

            PrintString.printLog("POS", o.getName());

        int total = 0;
        if (o.isMegamixTrack()){
            total = (int)o.getDurationLong();
            total/=1000;
        }
        else if(!o.getDuration().equals("0")) {
            total = Integer.valueOf(o.getDuration());

        }
        if (total>0) {
            String timeString = Settings.getUserInterfaceDuration(total, false, false);
            holder.duration.setText(timeString);
            PrintString.printLog("Duration", timeString);
        }
        if (!orderMode) {
            try {
                if (dragging) {
                    holder.drag_image.setVisibility(View.VISIBLE);
                } else {
                    holder.drag_image.setVisibility(View.GONE);
                }
            } catch (NullPointerException ignored) {}
        }

        if (o.isMegamixTrack()) {
            //holder.megamix.setText(context.getResources().getString(R.string.megamix_track));
            holder.megamix.setText("M");
            settings.setTextViewFont(holder.megamix, Settings.LAYS_FONT);
        } else if (isListAdapter && !o.getComment().equals("null")){
            holder.megamix.setText(o.getComment());
        } else holder.megamix.setText(R.string.unknown_playlist);
        holder.itemView.setTag(o.getName());
        if (highlight != -1) {
            holder.name.setTextColor(settings.getColor(settings.getAttributeId(settings.getThemeByString(), R.attr.theme_dependent_tint_color)));
            holder.artist.setTextColor(settings.getColor(settings.getAttributeId(settings.getThemeByString(), R.attr.theme_dependent_tint_color)));
            holder.megamix.setTextColor(settings.getColor(settings.getAttributeId(settings.getThemeByString(), R.attr.theme_dependent_tint_color)));
            holder.duration.setTextColor(settings.getColor(settings.getAttributeId(settings.getThemeByString(), R.attr.theme_dependent_tint_color)));
            int color = settings.getAttributeId(settings.getThemeByString(), R.attr.theme_dependent_accent_color);
            if (holder.mItemId == highlight) {
                if (color == settings.getAttributeId(settings.getThemeByString(), R.attr.theme_dependent_accent_color)) {
                    holder.name.setTextColor(settings.getColor(R.color.white));
                    holder.artist.setTextColor(settings.getColor(R.color.white));
                    holder.megamix.setTextColor(settings.getColor(R.color.white));
                    holder.duration.setTextColor(settings.getColor(R.color.white));
                }
                holder.view.setBackgroundColor(settings.getColor(color));
            }
            else holder.view.setBackgroundColor(settings.getColor(settings.getAttributeId(settings.getThemeByString(), R.attr.theme_dependent_order_color)));
        }
    }

    public void itemAttached(View view){
        int position = (int)view.getTag(R.string.tag);
        ImageView image = (ImageView) view.findViewById(R.id.cover_image);
        if (!covers.containsKey(position)) loadCover(image, position, getItem(position));
        else image.setImageBitmap(covers.get(position));
    }

    public void itemDetach(View view){
        int position = (int)view.getTag(R.string.tag);
        if (tasks.containsKey(position)) {
            tasks.get(position).cancel(true);
            tasks.remove(position);
        }
    }

    private void loadCover(ImageView cover_image, int position, Track o){
        MyTask task = new MyTask(cover_image, position);
        task.execute(o);
        tasks.put(position, task);
    }

    @Override
    public long getItemId(int position) {
        return mItemList.get(position).first;
    }

    public Track getItem(int position) {
        return mItemList.get(position).second;
    }

    public void orderChanged(){
        covers.clear();
    }

    public ArrayList<Track> getAll(){
        ArrayList<Track> tracks = new ArrayList<>();
        for (int i = 0; i<mItemList.size(); i++){
            tracks.add(mItemList.get(i).second);
        }
        return tracks;
    }

    public void updateVisibility(boolean dragging){
        this.dragging = dragging;
    }


    public int getCount(){
        return mItemList.size();
    }

    public class ViewHolder extends DragItemAdapter.ViewHolder {
        public TextView name;
        public TextView artist;
        public TextView duration;
        public TextView megamix;
        public ImageView drag_image, cover_image;
        public View view;
        public int id;


        public ViewHolder(final View itemView) {
            super(itemView, mGrabHandleId, mDragOnLongPress);
            view = itemView;
            name = (TextView) itemView.findViewById(R.id.Playlist_songName);
            artist = (TextView) itemView.findViewById(R.id.Playlist_artist);
            duration = (TextView) itemView.findViewById(R.id.Playlist_Duration);
            megamix = (TextView) itemView.findViewById(R.id.Playlist_Megamix);
            drag_image = (ImageView) itemView.findViewById(R.id.drag_image);
            cover_image =(ImageView) itemView.findViewById(R.id.cover_image);
        }

        @Override
        public void onItemClicked(View view) {
            for (int i = 0; i<mItemList.size(); i++){
                if (mItemList.get(i).first == mItemId)
                    itemClicked(i);
            }
        }

        @Override
        public boolean onItemLongClicked(View view) {
            for (int i = 0; i<mItemList.size(); i++){
                if (mItemList.get(i).first == mItemId)
                    itemLongClicked(i);
            }
            return true;
        }
    }

    class MyTask extends AsyncTask<Track, Void, Bitmap> {

        private final ImageView view;
        private int position;
        public MyTask(final ImageView view1, int position){
            this.view = view1;

            this.position = position;
            if (size == 0) {
                ViewTreeObserver vto = view.getViewTreeObserver();
                vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    public boolean onPreDraw() {
                        view.getViewTreeObserver().removeOnPreDrawListener(this);
                        size = view.getMeasuredWidth();
                        return true;
                    }
                });
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(Track... track) {
            for (Track o : track) {
                try {
                    MediaMetadataRetriever retriver = new MediaMetadataRetriever();
                    retriver.setDataSource(o.getPath());
                    byte[] data = retriver.getEmbeddedPicture();
                    if (size != 0) {
                        boolean isVisible;
                        if (isListAdapter) isVisible = position >= linearLayoutManager.findFirstVisibleItemPosition() && position<=linearLayoutManager.findLastVisibleItemPosition();
                        else isVisible = position >= gridLayoutManager.findFirstVisibleItemPosition() && position<=gridLayoutManager.findLastVisibleItemPosition();
                        if (isVisible) return Settings.decodeSampledBitmapFromData(data, size, size, data.length);
                    } else {
                        PrintString.printLog("Adapter", "tut");
                        return BitmapFactory.decodeByteArray(data, 0, data.length);
                    }
                } catch (NullPointerException ignored) {
                    return Settings.track;
                } catch (RuntimeException ignored) {
                    return Settings.track;
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (bitmap != null)imageViewAnimatedChange(context, view, bitmap);
        }

        public void imageViewAnimatedChange(Context c, final ImageView v, final Bitmap new_image) {
            final Animation anim_in  = AnimationUtils.loadAnimation(c, android.R.anim.fade_in);
            v.setImageBitmap(new_image);
            covers.put(position, new_image);
            anim_in.setAnimationListener(new Animation.AnimationListener() {
                @Override public void onAnimationStart(Animation animation) {}
                @Override public void onAnimationRepeat(Animation animation) {}
                @Override public void onAnimationEnd(Animation animation) {}
            });
            v.startAnimation(anim_in);
            if (tasks.containsKey(position)) {
                tasks.remove(position);
            }
        }
    }
}
