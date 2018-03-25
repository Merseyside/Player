package com.merseyside.admin.player.Dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.merseyside.admin.player.R;

/**
 * Created by Admin on 11.08.2016.
 */
public class TracksDialog extends Dialog implements View.OnClickListener {

    private Activity activity;
    private ImageButton info, delete, order, play, playlist, share, megamix, more, comment, similar, love, artists_tracks;
    private MyDialogListener myDialogListener;
    private RelativeLayout relativeLayout;

    public TracksDialog(Activity activity, MyDialogListener myDialogListener) {
        super(activity);
        this.activity = activity;
        this.myDialogListener = myDialogListener;
    }

    public interface MyDialogListener
    {
        void userSelectedDelete();
        void userSelectedInfo();
        void userSelectedOrder();
        void userSelectedPlay();
        void userSelectedToPlaylist();
        void userSelectedShare();
        void userSelectedMegamixCreator();
        void userSelectedCommentTrack();
        void userSelectedSimilarTracks();
        void userSelectedLoveTrack();
        void userSelectedArtistsTracks();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.tracks_customdialog);

        info = (ImageButton) findViewById(R.id.info1);
        info.setOnClickListener(this);

        order = (ImageButton) findViewById(R.id.in_order);
        order.setOnClickListener(this);

        delete = (ImageButton) findViewById(R.id.delete);
        delete.setOnClickListener(this);

        play = (ImageButton) findViewById(R.id.in_play);
        play.setOnClickListener(this);

        playlist = (ImageButton) findViewById(R.id.in_to_playlist);
        playlist.setOnClickListener(this);

        share = (ImageButton) findViewById(R.id.cd_share);
        share.setOnClickListener(this);

        megamix = (ImageButton) findViewById(R.id.cd_megamix);
        megamix.setOnClickListener(this);

        more = (ImageButton) findViewById(R.id.cd_more);
        more.setOnClickListener(this);

        comment = (ImageButton) findViewById(R.id.cd_comment);
        comment.setOnClickListener(this);

        similar = (ImageButton) findViewById(R.id.cd_similar);
        similar.setOnClickListener(this);

        love = (ImageButton) findViewById(R.id.cd_love);
        love.setOnClickListener(this);

        artists_tracks = (ImageButton) findViewById(R.id.cd_artists_tracks);
        artists_tracks.setOnClickListener(this);

        relativeLayout = (RelativeLayout) findViewById(R.id.more_layout);
        relativeLayout.setVisibility(View.GONE);

    }

    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.delete:
                myDialogListener.userSelectedDelete();
                dismiss();
                break;
            case R.id.info1:
                myDialogListener.userSelectedInfo();
                dismiss();
                break;
            case R.id.in_order:
                myDialogListener.userSelectedOrder();
                dismiss();
                break;
            case R.id.in_play:
                myDialogListener.userSelectedPlay();
                dismiss();
                break;
            case R.id.in_to_playlist:
                myDialogListener.userSelectedToPlaylist();
                dismiss();
                break;
            case R.id.cd_share:
                myDialogListener.userSelectedShare();
                dismiss();
                break;
            case R.id.cd_megamix:
                myDialogListener.userSelectedMegamixCreator();
                dismiss();
                break;
            case R.id.cd_comment:
                myDialogListener.userSelectedCommentTrack();
                dismiss();
                break;
            case R.id.cd_similar:
                myDialogListener.userSelectedSimilarTracks();
                dismiss();
                break;
            case R.id.cd_love:
                myDialogListener.userSelectedLoveTrack();
                dismiss();
                break;
            case R.id.cd_artists_tracks:
                myDialogListener.userSelectedArtistsTracks();
                dismiss();
                break;
            case R.id.cd_more:
                if (relativeLayout.getVisibility() == View.GONE) relativeLayout.setVisibility(View.VISIBLE);
                else relativeLayout.setVisibility(View.GONE);
        }
    }
}
