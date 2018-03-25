package com.merseyside.admin.player.Dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;

import com.merseyside.admin.player.R;

/**
 * Created by Admin on 07.01.2017.
 */

public class PlayerTrackDialog extends Dialog implements View.OnClickListener{
    Activity activity;
    ImageButton info, delete, playlist, share, megamix, comment, similar, love, artists_tracks;
    private PlayerTrackDialogListener playerTrackDialogListener;

    public PlayerTrackDialog(Activity activity, PlayerTrackDialogListener playerTrackDialogListener) {
        super(activity);
        this.activity = activity;
        this.playerTrackDialogListener = playerTrackDialogListener;
    }

    public static interface PlayerTrackDialogListener
    {
        void userSelectedDelete();
        void userSelectedInfo();
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
        setContentView(R.layout.player_track_dialog);

        info = (ImageButton) findViewById(R.id.info1);
        info.setOnClickListener(this);

        delete = (ImageButton) findViewById(R.id.delete);
        delete.setOnClickListener(this);

        playlist = (ImageButton) findViewById(R.id.in_to_playlist);
        playlist.setOnClickListener(this);

        share = (ImageButton) findViewById(R.id.cd_share);
        share.setOnClickListener(this);

        megamix = (ImageButton) findViewById(R.id.cd_megamix);
        megamix.setOnClickListener(this);

        comment = (ImageButton) findViewById(R.id.cd_comment);
        comment.setOnClickListener(this);

        similar = (ImageButton) findViewById(R.id.cd_similar);
        similar.setOnClickListener(this);

        love = (ImageButton) findViewById(R.id.cd_love);
        love.setOnClickListener(this);

        artists_tracks = (ImageButton) findViewById(R.id.cd_artists_tracks);
        artists_tracks.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.delete:
                playerTrackDialogListener.userSelectedDelete();
                dismiss();
                break;

            case R.id.info1:
                playerTrackDialogListener.userSelectedInfo();
                dismiss();
                break;

            case R.id.in_to_playlist:
                playerTrackDialogListener.userSelectedToPlaylist();
                dismiss();
                break;
            case R.id.cd_share:
                playerTrackDialogListener.userSelectedShare();
                dismiss();
                break;
            case R.id.cd_megamix:
                playerTrackDialogListener.userSelectedMegamixCreator();
                dismiss();
                break;
            case R.id.cd_comment:
                playerTrackDialogListener.userSelectedCommentTrack();
                dismiss();
                break;
            case R.id.cd_similar:
                playerTrackDialogListener.userSelectedSimilarTracks();
                dismiss();
                break;
            case R.id.cd_love:
                playerTrackDialogListener.userSelectedLoveTrack();
                dismiss();
                break;
            case R.id.cd_artists_tracks:
                playerTrackDialogListener.userSelectedArtistsTracks();
                dismiss();
                break;
        }
    }
}
