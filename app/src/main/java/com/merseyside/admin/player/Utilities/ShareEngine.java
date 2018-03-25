package com.merseyside.admin.player.Utilities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.merseyside.admin.player.AdaptersAndItems.Track;
import com.merseyside.admin.player.R;

/**
 * Created by Admin on 29.01.2017.
 */

public class ShareEngine {
    Context context;
    Track track;
    public ShareEngine(Context context, Track track){
        this.context = context;
        this.track = track;
        FirebaseEngine.logEvent(context, "SHARE", null);
    }

    public void shareNameAndArtist(){
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/*");
        shareIntent.putExtra(Intent.EXTRA_TEXT, track.getName() + " - " + track.getArtist() + "\n" + context.getResources().getString(R.string.shared_by));
        context.startActivity(Intent.createChooser(shareIntent, context.getResources().getString(R.string.send_with)));
    }

    public void shareFile(){
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        if (track.getPath().startsWith("http")) {
            shareIntent.setType("text/*");
            shareIntent.putExtra(Intent.EXTRA_TEXT, track.getPath() + "\n" + context.getResources().getString(R.string.shared_by));
        }
        else {
            Uri uri = Uri.parse("file://"+ track.getPath());
            shareIntent.setType("audio/*");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        context.startActivity(Intent.createChooser(shareIntent, context.getResources().getString(R.string.send_with)));
    }

    public void shareAll(){
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        if (track.getPath().startsWith("http")) {
            shareIntent.setType("text/*");
            shareIntent.putExtra(Intent.EXTRA_TEXT, track.getName() + " - " + track.getArtist() + "\n" + context.getResources().getString(R.string.shared_by));
        } else {
            Uri uri = Uri.parse("file://"+ track.getPath());
            shareIntent.setType("*/*");
            shareIntent.putExtra(Intent.EXTRA_TEXT, track.getName() + " - " + track.getArtist() + "\n" + context.getResources().getString(R.string.shared_by));
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        context.startActivity(Intent.createChooser(shareIntent, context.getResources().getString(R.string.send_with)));
    }
}
