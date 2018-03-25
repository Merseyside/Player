package com.merseyside.admin.player.AdaptersAndItems;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.merseyside.admin.player.ActivitesAndFragments.Player_Fragment;
import com.merseyside.admin.player.R;
import com.merseyside.admin.player.Utilities.Point;
import com.merseyside.admin.player.Utilities.PrintString;
import com.merseyside.admin.player.Utilities.Settings;

import java.util.List;

/**
 * Адаптер для выбора плейлистов и стримов
 */
public class PlaylistItemsAdapter extends ArrayAdapter<SQLItem> {
    private Context c;
    private int id;
    private List<SQLItem> items;
    private Settings settings;
    private int size;
    private ArrayMap<Integer, Bitmap> covers;


    public PlaylistItemsAdapter(Context context, int resource, List<SQLItem> list) {
        super(context, resource, list);
        c = context;
        id = resource;
        items = list;
        settings = new Settings(context);
        if (id == R.layout.playlist_grid_view) size = Settings.GRID_SIZE;
        else size = Settings.LIST_SIZE;
        covers = new ArrayMap<>();
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(id, parent, false);
        }
        final SQLItem o = items.get(position);

        if (o != null) {
            final TextView name = (TextView) v.findViewById(R.id.playlistTitle);
            final ImageView image = (ImageView) v.findViewById(R.id.playlistCover);

            settings.setTextViewFont(name, null);

            name.setText(o.getName());

            if (o.getType() == Player_Fragment.Type.STREAM)
                image.setImageResource(R.drawable.internet);
            else if (o.getType() == Player_Fragment.Type.PLAYLIST)
                image.setImageResource(settings.getAttributeId(settings.getThemeByString(), R.attr.theme_dependent_playlist_cover));
            else if (o.getType() == Player_Fragment.Type.MEMORY) {
                if (!covers.containsKey(position)) {
                    if (!o.getPic().equals("")) {
                        MyTask task = new MyTask(image, position);
                        task.execute(o);
                    } else
                        image.setImageResource(settings.getAttributeId(settings.getThemeByString(), R.attr.theme_dependent_folder_icon));
                } else image.setImageBitmap(covers.get(position));
            }
        }
        return v;
    }

    @Override
    public SQLItem getItem(int position)
    {
        return items.get(position);
    }

    public void removeItem(int position) {
        items.remove(position);
    }

    class MyTask extends AsyncTask<SQLItem, Void, Bitmap> {

        private ImageView view;
        private int position;
        public MyTask(ImageView view, int position){
            this.position = position;
            this.view = view;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(SQLItem... sqlItemses) {
            for (SQLItem o : sqlItemses) {
                try {
                    MediaMetadataRetriever retriver = new MediaMetadataRetriever();
                    retriver.setDataSource(o.getPic());
                    byte[] data = retriver.getEmbeddedPicture();
                    if (size!=0){
                        return Settings.decodeSampledBitmapFromData(data, size, size, data.length);
                    } else{
                        PrintString.printLog("Adapter", "tut");
                        return BitmapFactory.decodeByteArray(data, 0, data.length);
                    }
                } catch (RuntimeException ignored){return null;}
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            imageViewAnimatedChange(c, view, bitmap);
            if (view.getHeight()!=0){
                if (id == R.layout.playlist_grid_view){
                    if (Settings.GRID_SIZE == 0) settings.setGridSize(view.getHeight());
                }
                else {
                    if (Settings.LIST_SIZE == 0) settings.setListSize(view.getHeight());
                }
            }
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
        }
    }
}