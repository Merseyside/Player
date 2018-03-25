package com.merseyside.admin.player.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;

import com.merseyside.admin.player.AdaptersAndItems.Track;
import com.merseyside.admin.player.R;
import com.merseyside.admin.player.Utilities.ShareEngine;

/**
 * Created by Admin on 29.01.2017.
 */

public class ShareDialog extends Dialog implements View.OnClickListener {

    Context context;
    Track track;
    RelativeLayout first, second, third;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.share_dialog);
        first = (RelativeLayout) findViewById(R.id.share_layout1);
        first.setOnClickListener(this);

        second = (RelativeLayout) findViewById(R.id.share_layout2);
        second.setOnClickListener(this);

        third = (RelativeLayout) findViewById(R.id.share_layout3);
        third.setOnClickListener(this);
    }

    public ShareDialog(Context context, Track track) {
        super(context);
        this.context = context;
        this.track = track;
    }

    @Override
    public void onClick(View view) {
        ShareEngine shareEngine = new ShareEngine(context, track);
        switch(view.getId()){
            case R.id.share_layout1:
            {
                shareEngine.shareNameAndArtist();
                dismiss();
                break;
            }
            case R.id.share_layout2:
            {
                shareEngine.shareFile();
                dismiss();
                break;
            }
            case R.id.share_layout3:
            {
                shareEngine.shareAll();
                dismiss();
                break;
            }
        }
    }
}
