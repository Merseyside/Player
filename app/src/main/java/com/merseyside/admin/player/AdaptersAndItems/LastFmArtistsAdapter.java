package com.merseyside.admin.player.AdaptersAndItems;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.merseyside.admin.player.R;
import com.merseyside.admin.player.Utilities.Settings;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import de.umass.lastfm.*;

/**
 * Created by Admin on 17.03.2017.
 */

public class LastFmArtistsAdapter extends ArrayAdapter<Artist> {
    private Context context;
    private int id;
    private ArrayList<Artist> items;
    private Settings settings;

    public LastFmArtistsAdapter(Context context, int resource, ArrayList<Artist> items) {
        super(context, resource, items);
        this.context = context;
        this.id = resource;
        this.items = items;
        settings = new Settings(context);
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public Artist getItem(int position)
    {
        return items.get(position);
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            convertView = inflater.inflate(id, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
            holder.image.setImageBitmap(null);
        }

        final Artist o = items.get(position);

        if (o != null) {
            holder.name.setText(o.getName());

            String listeners = context.getResources().getString(R.string.playcount) + o.getPlaycount();
            holder.listeners.setText(listeners);

            if ((!Settings.WIFI || settings.checkWifiOnAndConnected()) && o.getImageURL(ImageSize.SMALL) != null && !o.getImageURL(ImageSize.SMALL).equals("")) {
                MyTask task = new MyTask(holder.image);
                task.execute(o);
            } else holder.image.setImageResource(R.drawable.nav_lastfm);
        }
        return convertView;
    }

    class MyTask extends AsyncTask<Artist, Void, Bitmap> {

        ImageView view;
        public MyTask(ImageView view){
            this.view = view;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(Artist... sqlItemses) {
            for (Artist o : sqlItemses) {
                try {
                    return getBitmapFromURL(o.getImageURL(ImageSize.LARGE));
                } catch (RuntimeException ignored){return null;}
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            imageViewAnimatedChange(context, view, bitmap);
        }

        public void imageViewAnimatedChange(Context c, final ImageView v, final Bitmap new_image) {
            final Animation anim_in  = AnimationUtils.loadAnimation(c, android.R.anim.fade_in);
            v.setImageBitmap(new_image);
            anim_in.setAnimationListener(new Animation.AnimationListener() {
                @Override public void onAnimationStart(Animation animation) {}
                @Override public void onAnimationRepeat(Animation animation) {}
                @Override public void onAnimationEnd(Animation animation) {}
            });
            v.startAnimation(anim_in);
        }

        public Bitmap getBitmapFromURL(String src) {
            try {
                URL url = null;
                try {
                    url = new URL(src);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                HttpURLConnection connection = null;
                try {
                    connection = (HttpURLConnection) url.openConnection();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return myBitmap;
            } catch (IOException e) {
                // Log exception
                return null;
            }
        }
    }

    private class ViewHolder {
        public final ImageView image;
        public final TextView name;
        public final TextView listeners;
        public int position;

        public ViewHolder(View row) {
            image = (ImageView) row.findViewById(R.id.lastfm_cover);
            name = (TextView) row.findViewById(R.id.lastfm_title);
            listeners = (TextView) row.findViewById(R.id.lastfm_listeners);
        }
    }
}
