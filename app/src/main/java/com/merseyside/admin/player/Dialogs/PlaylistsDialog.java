package com.merseyside.admin.player.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.merseyside.admin.player.R;

/**
 * Created by Admin on 15.08.2016.
 */
public class PlaylistsDialog extends Dialog implements View.OnClickListener{

    private ImageButton edit, delete, play, order, megamix, refresh;
    private MyDialogListener myDialogListener;
    RelativeLayout addOpt;

    public PlaylistsDialog(Context context, MyDialogListener myDialogListener) {
        super(context);
        this.myDialogListener = myDialogListener;
     }

    public interface MyDialogListener {
        void userSelectedDelete();
        void userSelectedEdit();
        void userSelectedPlay();
        void userSelectedOrder();
        void userSelectedMegamixCreator();
        void userSelectedRefresh();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.playlists_streams_customdialog);

        play = (ImageButton) findViewById(R.id.cd_play);
        play.setOnClickListener(this);
        delete = (ImageButton) findViewById(R.id.cd_delete);
        delete.setOnClickListener(this);
        edit = (ImageButton) findViewById(R.id.cd_edit);
        edit.setOnClickListener(this);
        order = (ImageButton) findViewById(R.id.cd_order);
        order.setOnClickListener(this);
        megamix = (ImageButton) findViewById(R.id.cd_megamix);
        megamix.setOnClickListener(this);
        refresh = (ImageButton) findViewById(R.id.cd_refresh);
        refresh.setOnClickListener(this);
        addOpt = (RelativeLayout) findViewById(R.id.additional_options);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.cd_delete:
                myDialogListener.userSelectedDelete();
                dismiss();
                break;
            case R.id.cd_edit:
                myDialogListener.userSelectedEdit();
                dismiss();
                break;
            case R.id.cd_play:
                myDialogListener.userSelectedPlay();
                dismiss();
                break;
            case R.id.cd_order:
                myDialogListener.userSelectedOrder();
                dismiss();
                break;
            case R.id.cd_megamix:
                myDialogListener.userSelectedMegamixCreator();
                dismiss();
                break;
            case R.id.cd_refresh:
                myDialogListener.userSelectedRefresh();
                dismiss();
                break;

        }
    }
}
