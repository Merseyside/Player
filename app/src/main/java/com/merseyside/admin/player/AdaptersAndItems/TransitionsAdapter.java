package com.merseyside.admin.player.AdaptersAndItems;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.merseyside.admin.player.R;
import com.merseyside.admin.player.Utilities.PlaybackManager;
import com.merseyside.admin.player.Utilities.TransitionPlayer;

import java.util.ArrayList;
import java.util.List;

import de.umass.lastfm.*;
import de.umass.lastfm.Track;

/**
 * Created by Admin on 24.05.2017.
 */

public class TransitionsAdapter extends ArrayAdapter<Transition>{

    private Context context;
    private int id;
    private List<Transition> items;
    private TransitionPlayer transitionPlayer;

    public TransitionsAdapter(@NonNull Context context, @LayoutRes int resource, ArrayList<Transition> list) {
        super(context, resource, list);
        this.context = context;
        this.id = resource;
        this.items = list;
        transitionPlayer = new TransitionPlayer(context);
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(id, parent, false);
        }

        String name = items.get(position).getName();
        TextView name_tw= (TextView) v.findViewById(R.id.transition_name);
        name_tw.setText(name);

        ImageButton play = (ImageButton) v.findViewById(R.id.transition_play);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!transitionPlayer.isPlaying())
                    transitionPlayer.playTransition(items.get(position), 0);
                else {
                    transitionPlayer.onClick(PlaybackManager.Action.PLAY);
                }
            }
        });
        return v;
    }

    public void stopPlayer(){
        if (transitionPlayer.isPlaying()) transitionPlayer.onClick(PlaybackManager.Action.PLAY);
    }
}
